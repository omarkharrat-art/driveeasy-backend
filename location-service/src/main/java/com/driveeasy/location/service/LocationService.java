package com.driveeasy.location.service;

import com.driveeasy.location.client.ClientServiceClient;
import com.driveeasy.location.client.VehiculeServiceClient;
import com.driveeasy.location.config.PricingProperties;
import com.driveeasy.location.dto.*;
import com.driveeasy.location.exception.BusinessException;
import com.driveeasy.location.exception.ConflictException;
import com.driveeasy.location.exception.ResourceNotFoundException;
import com.driveeasy.location.model.Location;
import com.driveeasy.location.model.StatutLocation;
import com.driveeasy.location.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Rental lifecycle + orchestration of vehicule-service and client-service.
 *
 * Consistency strategy (no distributed transaction across microservices):
 *  1. check everything that can fail BEFORE modifying anything;
 *  2. call the remote service first, then update our own data;
 *  3. if our update fails after a successful remote call -> COMPENSATION
 *     (we call the remote service again to undo its change).
 */
@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository repository;
    private final VehiculeServiceClient vehiculeClient;
    private final ClientServiceClient clientClient;
    private final PricingProperties pricing;

    public LocationService(LocationRepository repository, VehiculeServiceClient vehiculeClient,
                           ClientServiceClient clientClient, PricingProperties pricing) {
        this.repository = repository;
        this.vehiculeClient = vehiculeClient;
        this.clientClient = clientClient;
        this.pricing = pricing;
    }

    // =====================================================================
    //  Read
    // =====================================================================

    /** GET /locations : filters (all optional) + pagination, most recent first. */
    public PageResponse<LocationResponse> rechercher(StatutLocation statut, Long clientId, Long vehiculeId,
                                                     LocalDate dateDebut, LocalDate dateFin,
                                                     int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException("Pagination invalide : page >= 0 et 1 <= size <= 100");
        }
        LocalDate debut = dateDebut != null ? dateDebut : LocalDate.MIN;
        LocalDate fin = dateFin != null ? dateFin : LocalDate.MAX;

        List<LocationResponse> resultats = repository.findAll().stream()
                .filter(l -> statut == null || l.getStatut() == statut)
                .filter(l -> clientId == null || l.getClientId().equals(clientId))
                .filter(l -> vehiculeId == null || l.getVehiculeId().equals(vehiculeId))
                .filter(l -> l.chevauche(debut, fin))   // period filter
                .sorted(Comparator.comparing(Location::getId).reversed())
                .map(LocationResponse::from)
                .toList();
        return PageResponse.of(resultats, page, size);
    }

    /** GET /locations/{id} : rental + customer + vehicle (aggregation of 3 services). */
    public LocationDetailResponse detail(Long id) {
        Location l = getOrThrow(id);
        List<String> avertissements = new ArrayList<>();

        ClientDto client = null;
        try {
            client = clientClient.getClient(l.getClientId());
        } catch (RuntimeException ex) {
            avertissements.add("Client non disponible : " + ex.getMessage());
        }
        VehiculeDto vehicule = null;
        try {
            vehicule = vehiculeClient.getVehicule(l.getVehiculeId());
        } catch (RuntimeException ex) {
            avertissements.add("Vehicule non disponible : " + ex.getMessage());
        }
        return new LocationDetailResponse(LocationResponse.from(l), client, vehicule, avertissements);
    }

    /** GET /locations/clients/{clientId}/actives : used by client-service before deleting a customer. */
    public boolean clientADesLocationsActives(Long clientId) {
        return repository.findByClientId(clientId).stream().anyMatch(Location::estActive);
    }

    // =====================================================================
    //  Price
    // =====================================================================

    /** POST /locations/simulation : price without creating anything. */
    public SimulationResponse simuler(LocationRequest req) {
        verifierDates(req.dateDebut(), req.dateFinPrevue());
        VehiculeDto vehicule = vehiculeClient.getVehicule(req.vehiculeId());
        ClientDto client = clientClient.getClient(req.clientId());
        return calculerPrix(req, vehicule, client);
    }

    /**
     * Prix = nbJours x prixJournalier x coefficientCategorie - remise fidelite
     * (FIDELE : 5 %, VIP : 10 %, values from the config server).
     */
    private SimulationResponse calculerPrix(LocationRequest req, VehiculeDto vehicule, ClientDto client) {
        long nbJours = nombreDeJours(req.dateDebut(), req.dateFinPrevue());
        BigDecimal tauxRemise = tauxRemise(client.categorieClient());

        BigDecimal montantBrut = vehicule.prixJournalier()
                .multiply(BigDecimal.valueOf(nbJours))
                .multiply(BigDecimal.valueOf(vehicule.coefficientCategorie()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal remise = montantBrut.multiply(tauxRemise).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = montantBrut.subtract(remise);

        return new SimulationResponse(req.clientId(), req.vehiculeId(), req.dateDebut(), req.dateFinPrevue(),
                nbJours, vehicule.prixJournalier(), vehicule.coefficientCategorie(), client.categorieClient(),
                tauxRemise, montantBrut, remise, total);
    }

    private BigDecimal tauxRemise(String categorieClient) {
        return switch (categorieClient) {
            case "FIDELE" -> pricing.remiseFidele();
            case "VIP" -> pricing.remiseVip();
            default -> BigDecimal.ZERO;
        };
    }

    /** From the 1st to the 5th = 4 days; same day = 1 day minimum. */
    private long nombreDeJours(LocalDate debut, LocalDate fin) {
        return Math.max(1, ChronoUnit.DAYS.between(debut, fin));
    }

    // =====================================================================
    //  Lifecycle
    // =====================================================================

    /**
     * POST /locations : reservation.
     * Checks (inter-service calls): customer eligible, vehicle neither LOUE nor MAINTENANCE,
     * no other active rental of this vehicle on the period.
     * Nothing is modified in the other services at this step -> no compensation needed.
     */
    public LocationResponse creer(LocationRequest req) {
        verifierDates(req.dateDebut(), req.dateFinPrevue());

        // 1. Customer eligibility (client-service)
        EligibiliteDto eligibilite = clientClient.eligibilite(req.clientId(), req.dateFinPrevue());
        if (!eligibilite.eligible()) {
            throw new BusinessException("Client non eligible : " + String.join(" ; ", eligibilite.motifs()));
        }

        // 2. Vehicle statut (vehicule-service)
        VehiculeDto vehicule = vehiculeClient.getVehicule(req.vehiculeId());
        if (!"DISPONIBLE".equals(vehicule.statut())) {
            throw new ConflictException("Le vehicule " + vehicule.id() + " n'est pas disponible (statut "
                    + vehicule.statut() + ")");
        }

        // 3. No overlapping active rental of the same vehicle (our own data)
        boolean dejaReserve = repository.findByVehiculeId(req.vehiculeId()).stream()
                .anyMatch(l -> l.estActive() && l.chevauche(req.dateDebut(), req.dateFinPrevue()));
        if (dejaReserve) {
            throw new ConflictException("Le vehicule " + vehicule.id() + " est deja reserve sur cette periode");
        }

        // 4. Price, then save
        ClientDto client = clientClient.getClient(req.clientId());
        SimulationResponse prix = calculerPrix(req, vehicule, client);

        Location l = new Location();
        l.setClientId(req.clientId());
        l.setVehiculeId(req.vehiculeId());
        l.setDateDebut(req.dateDebut());
        l.setDateFinPrevue(req.dateFinPrevue());
        l.setPrixJournalierApplique(vehicule.prixJournalier());
        l.setCoefficientCategorie(vehicule.coefficientCategorie());
        l.setTauxRemise(prix.tauxRemise());
        l.setMontantTotal(prix.montantTotal());
        l.setStatut(StatutLocation.RESERVEE);
        l.setDateCreation(LocalDateTime.now());
        return LocationResponse.from(repository.save(l));
    }

    /** PATCH /locations/{id}/depart : RESERVEE -> EN_COURS, vehicle -> LOUE. */
    public LocationResponse depart(Long id) {
        Location l = getOrThrow(id);
        if (l.getStatut() != StatutLocation.RESERVEE) {
            throw new ConflictException("Depart impossible : la location est " + l.getStatut());
        }
        VehiculeDto vehicule = vehiculeClient.getVehicule(l.getVehiculeId());

        // Remote change first: if it fails, nothing has been modified here (still consistent)
        vehiculeClient.changerStatut(l.getVehiculeId(), "LOUE", null);
        try {
            l.setKmDepart(vehicule.kilometrage());
            l.setStatut(StatutLocation.EN_COURS);
            return LocationResponse.from(repository.save(l));
        } catch (RuntimeException ex) {
            // COMPENSATION: undo the remote change
            log.error("Echec du depart de la location {}, compensation : vehicule remis DISPONIBLE", id, ex);
            vehiculeClient.changerStatut(l.getVehiculeId(), "DISPONIBLE", null);
            l.setStatut(StatutLocation.RESERVEE);
            l.setKmDepart(null);
            throw ex;
        }
    }

    /**
     * PATCH /locations/{id}/retour : EN_COURS -> TERMINEE, vehicle -> DISPONIBLE (+ new km),
     * late penalty = 1.5 x prixJournalier x days late, then loyalty update.
     */
    public LocationResponse retour(Long id, RetourRequest req) {
        Location l = getOrThrow(id);
        if (l.getStatut() != StatutLocation.EN_COURS) {
            throw new ConflictException("Retour impossible : la location est " + l.getStatut());
        }
        LocalDate dateRetour = req.dateRetour() != null ? req.dateRetour() : LocalDate.now();
        if (dateRetour.isBefore(l.getDateDebut())) {
            throw new BusinessException("La date de retour ne peut pas etre avant la date de debut");
        }
        if (req.kmRetour() < l.getKmDepart()) {
            throw new BusinessException("kmRetour (" + req.kmRetour() + ") inferieur a kmDepart (" + l.getKmDepart() + ")");
        }

        // Late penalty
        long joursRetard = Math.max(0, ChronoUnit.DAYS.between(l.getDateFinPrevue(), dateRetour));
        BigDecimal penalite = l.getPrixJournalierApplique()
                .multiply(pricing.coefficientPenalite())
                .multiply(BigDecimal.valueOf(joursRetard))
                .setScale(2, RoundingMode.HALF_UP);

        // Remote change first
        vehiculeClient.changerStatut(l.getVehiculeId(), "DISPONIBLE", req.kmRetour());
        try {
            l.setDateRetourEffectif(dateRetour);
            l.setKmRetour(req.kmRetour());
            l.setPenalite(penalite);
            l.setStatut(StatutLocation.TERMINEE);
            repository.save(l);
        } catch (RuntimeException ex) {
            // COMPENSATION: the vehicle goes back to LOUE
            log.error("Echec du retour de la location {}, compensation : vehicule remis LOUE", id, ex);
            vehiculeClient.changerStatut(l.getVehiculeId(), "LOUE", null);
            l.setStatut(StatutLocation.EN_COURS);
            throw ex;
        }

        mettreAJourFidelite(l.getClientId());
        return LocationResponse.from(l);
    }

    /** PATCH /locations/{id}/annulation : only a RESERVEE rental (vehicle was never LOUE). */
    public LocationResponse annuler(Long id) {
        Location l = getOrThrow(id);
        if (l.getStatut() != StatutLocation.RESERVEE) {
            throw new ConflictException("Annulation impossible : la location est " + l.getStatut());
        }
        l.setStatut(StatutLocation.ANNULEE);
        return LocationResponse.from(repository.save(l));
    }

    /**
     * FIDELE after 5 completed rentals, VIP after 15 (only upgrades, never downgrades).
     * Not critical: if client-service is down, the return stays valid, we only log it.
     */
    private void mettreAJourFidelite(Long clientId) {
        long terminees = repository.findByClientId(clientId).stream()
                .filter(l -> l.getStatut() == StatutLocation.TERMINEE).count();
        String cible = terminees >= pricing.seuilVip() ? "VIP"
                : terminees >= pricing.seuilFidele() ? "FIDELE" : "STANDARD";
        try {
            String actuelle = clientClient.getClient(clientId).categorieClient();
            if (rang(cible) > rang(actuelle)) {
                clientClient.changerCategorie(clientId, cible);
                log.info("Client {} passe de {} a {} ({} locations terminees)", clientId, actuelle, cible, terminees);
            }
        } catch (RuntimeException ex) {
            log.warn("Mise a jour fidelite du client {} non effectuee : {}", clientId, ex.getMessage());
        }
    }

    private int rang(String categorie) {
        return switch (categorie) {
            case "VIP" -> 2;
            case "FIDELE" -> 1;
            default -> 0;
        };
    }

    // =====================================================================
    //  Statistics
    // =====================================================================

    /** GET /locations/statistiques : revenue, occupancy rate, most rented vehicles. */
    public StatistiquesResponse statistiques() {
        List<Location> toutes = repository.findAll();
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);

        List<Location> terminees = toutes.stream().filter(l -> l.getStatut() == StatutLocation.TERMINEE).toList();
        BigDecimal caTotal = somme(terminees);
        BigDecimal caMois = somme(terminees.stream()
                .filter(l -> !l.getDateRetourEffectif().isBefore(debutMois)).toList());

        Map<String, Long> parStatut = new LinkedHashMap<>();
        for (StatutLocation s : StatutLocation.values()) {
            parStatut.put(s.name(), toutes.stream().filter(l -> l.getStatut() == s).count());
        }

        // Fleet data from vehicule-service (the stats still work if it is down)
        Map<Long, VehiculeDto> flotte = new HashMap<>();
        Double tauxOccupation = null;
        try {
            List<VehiculeDto> vehicules = vehiculeClient.lister();
            vehicules.forEach(v -> flotte.put(v.id(), v));
            long loues = vehicules.stream().filter(v -> "LOUE".equals(v.statut())).count();
            tauxOccupation = vehicules.isEmpty() ? 0.0
                    : Math.round(loues * 1000.0 / vehicules.size()) / 10.0;   // 1 decimal
        } catch (RuntimeException ex) {
            log.warn("Statistiques sans donnees vehicules : {}", ex.getMessage());
        }

        // Top 5 most rented vehicles (cancelled rentals excluded)
        List<StatistiquesResponse.TopVehicule> top = toutes.stream()
                .filter(l -> l.getStatut() != StatutLocation.ANNULEE)
                .collect(Collectors.groupingBy(Location::getVehiculeId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    VehiculeDto v = flotte.get(e.getKey());
                    return new StatistiquesResponse.TopVehicule(e.getKey(),
                            v != null ? v.marque() : null, v != null ? v.modele() : null, e.getValue());
                })
                .toList();

        return new StatistiquesResponse(caTotal, caMois, parStatut, tauxOccupation, top);
    }

    private BigDecimal somme(List<Location> locations) {
        return locations.stream()
                .map(l -> l.getMontantTotal().add(l.getPenalite()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // =====================================================================
    //  Helpers
    // =====================================================================

    private Location getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location " + id + " introuvable"));
    }

    private void verifierDates(LocalDate debut, LocalDate fin) {
        if (fin.isBefore(debut)) {
            throw new BusinessException("La date de fin prevue doit etre posterieure ou egale a la date de debut");
        }
    }
}
