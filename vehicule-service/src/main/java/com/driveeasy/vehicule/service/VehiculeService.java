package com.driveeasy.vehicule.service;

import com.driveeasy.vehicule.dto.StatutRequest;
import com.driveeasy.vehicule.dto.VehiculeRequest;
import com.driveeasy.vehicule.dto.VehiculeResponse;
import com.driveeasy.vehicule.exception.BusinessException;
import com.driveeasy.vehicule.exception.ConflictException;
import com.driveeasy.vehicule.exception.ResourceNotFoundException;
import com.driveeasy.vehicule.model.*;
import com.driveeasy.vehicule.repository.VehiculeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.Comparator;
import java.util.List;

/**
 * Business rules of the fleet. The controller only handles HTTP;
 * every rule (uniqueness, statut, price...) lives here.
 */
@Service
public class VehiculeService {

    private final VehiculeRepository repository;

    // Constructor injection: Spring gives us the repository
    public VehiculeService(VehiculeRepository repository) {
        this.repository = repository;
    }

    /** GET /vehicules with optional filters (null = no filter). */
    public List<VehiculeResponse> rechercher(Categorie categorie, Carburant carburant,
                                             StatutVehicule statut, BigDecimal prixMax) {
        return repository.findAll().stream()
                .filter(v -> categorie == null || v.getCategorie() == categorie)
                .filter(v -> carburant == null || v.getCarburant() == carburant)
                .filter(v -> statut == null || v.getStatut() == statut)
                .filter(v -> prixMax == null || v.getPrixJournalier().compareTo(prixMax) <= 0)
                .sorted(Comparator.comparing(Vehicule::getId))
                .map(VehiculeResponse::from)
                .toList();
    }

    public VehiculeResponse trouverParId(Long id) {
        return VehiculeResponse.from(getOrThrow(id));
    }

    public VehiculeResponse creer(VehiculeRequest req) {
        verifierAnnee(req.annee());
        // Rule: immatriculation is unique -> 409
        repository.findByImmatriculation(req.immatriculation()).ifPresent(v -> {
            throw new ConflictException("L'immatriculation " + req.immatriculation() + " existe deja");
        });

        Vehicule v = new Vehicule();
        copier(req, v);
        v.setStatut(StatutVehicule.DISPONIBLE);   // a new vehicle is always available
        return VehiculeResponse.from(repository.save(v));
    }

    public VehiculeResponse modifier(Long id, VehiculeRequest req) {
        Vehicule v = getOrThrow(id);
        verifierAnnee(req.annee());
        // Uniqueness, ignoring the vehicle itself
        repository.findByImmatriculation(req.immatriculation())
                .filter(autre -> !autre.getId().equals(id))
                .ifPresent(autre -> {
                    throw new ConflictException("L'immatriculation " + req.immatriculation() + " existe deja");
                });

        copier(req, v);   // statut is NOT changed here: use PATCH /statut
        return VehiculeResponse.from(repository.save(v));
    }

    public void supprimer(Long id) {
        Vehicule v = getOrThrow(id);
        // Rule: a rented vehicle cannot be deleted -> 409
        if (v.getStatut() == StatutVehicule.LOUE) {
            throw new ConflictException("Impossible de supprimer un vehicule actuellement loue");
        }
        repository.deleteById(id);
    }

    /**
     * PATCH /vehicules/{id}/statut, called by location-service:
     * departure -> LOUE, return -> DISPONIBLE (+ new kilometrage).
     */
    public VehiculeResponse changerStatut(Long id, StatutRequest req) {
        Vehicule v = getOrThrow(id);

        // Rule: a vehicle in MAINTENANCE cannot go directly to LOUE
        if (req.statut() == StatutVehicule.LOUE && v.getStatut() == StatutVehicule.MAINTENANCE) {
            throw new ConflictException("Un vehicule en MAINTENANCE ne peut pas etre loue");
        }
        // Odometer can only go up
        if (req.kilometrage() != null) {
            if (req.kilometrage() < v.getKilometrage()) {
                throw new BusinessException("Le kilometrage (" + req.kilometrage()
                        + ") ne peut pas etre inferieur au kilometrage actuel (" + v.getKilometrage() + ")");
            }
            v.setKilometrage(req.kilometrage());
        }
        v.setStatut(req.statut());
        return VehiculeResponse.from(repository.save(v));
    }

    /**
     * GET /vehicules/disponibles?dateDebut=..&dateFin=..
     * Returns the vehicles whose statut is DISPONIBLE (not LOUE, not MAINTENANCE).
     * Overlaps with future reservations are checked by location-service,
     * which owns the reservations.
     */
    public List<VehiculeResponse> disponibles(LocalDate dateDebut, LocalDate dateFin) {
        if (dateFin.isBefore(dateDebut)) {
            throw new BusinessException("dateFin doit etre posterieure ou egale a dateDebut");
        }
        if (dateDebut.isBefore(LocalDate.now())) {
            throw new BusinessException("dateDebut ne peut pas etre dans le passe");
        }
        return rechercher(null, null, StatutVehicule.DISPONIBLE, null);
    }

    // ---------------- image ----------------

    /** PUT /vehicules/{id}/image : stores the uploaded file in memory (replaces the previous one). */
    public VehiculeResponse enregistrerImage(Long id, MultipartFile fichier) {
        Vehicule v = getOrThrow(id);
        if (fichier == null || fichier.isEmpty()) {
            throw new BusinessException("Aucun fichier recu");
        }
        String type = fichier.getContentType();
        if (type == null || !type.startsWith("image/")) {
            throw new BusinessException("Le fichier doit etre une image (jpeg, png, webp...)");
        }
        try {
            v.setImageData(fichier.getBytes());
        } catch (IOException e) {
            throw new BusinessException("Lecture du fichier impossible");
        }
        v.setImageContentType(type);
        return VehiculeResponse.from(repository.save(v));
    }

    /** GET /vehicules/{id}/image : the uploaded image, or 404 if there is none. */
    public Vehicule imageDe(Long id) {
        Vehicule v = getOrThrow(id);
        if (!v.hasImageUploadee()) {
            throw new ResourceNotFoundException("Le vehicule " + id + " n'a pas d'image");
        }
        return v;
    }

    /** DELETE /vehicules/{id}/image */
    public void supprimerImage(Long id) {
        Vehicule v = getOrThrow(id);
        v.setImageData(null);
        v.setImageContentType(null);
        repository.save(v);
    }

    // ---------------- helpers ----------------

    private Vehicule getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule " + id + " introuvable"));
    }

    /** Rule: the year must be <= current year. */
    private void verifierAnnee(int annee) {
        int anneeCourante = Year.now().getValue();
        if (annee > anneeCourante) {
            throw new BusinessException("L'annee doit etre inferieure ou egale a " + anneeCourante);
        }
    }

    /** Copies the request fields into the entity (shared by create and update). */
    private void copier(VehiculeRequest req, Vehicule v) {
        v.setImmatriculation(req.immatriculation().trim().toUpperCase());
        v.setMarque(req.marque());
        v.setModele(req.modele());
        v.setAnnee(req.annee());
        v.setCouleur(req.couleur());
        v.setCarburant(req.carburant());
        v.setBoite(req.boite());
        v.setNbPlaces(req.nbPlaces());
        v.setKilometrage(req.kilometrage());
        v.setPrixJournalier(req.prixJournalier());
        v.setCategorie(req.categorie());
        // External URL only; an uploaded image is kept separately (imageData)
        String url = req.imageUrl();
        v.setImageUrl(url != null && url.startsWith("http") ? url : null);
    }
}
