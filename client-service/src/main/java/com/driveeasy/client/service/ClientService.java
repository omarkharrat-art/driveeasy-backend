package com.driveeasy.client.service;

import com.driveeasy.client.client.LocationServiceClient;
import com.driveeasy.client.dto.*;
import com.driveeasy.client.exception.ConflictException;
import com.driveeasy.client.exception.ResourceNotFoundException;
import com.driveeasy.client.model.CategorieClient;
import com.driveeasy.client.model.Client;
import com.driveeasy.client.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Business rules of the customers. */
@Service
public class ClientService {

    private final ClientRepository repository;
    private final LocationServiceClient locationClient;
    private final int ageMinimum;

    public ClientService(ClientRepository repository,
                         LocationServiceClient locationClient,
                         // business property externalized in config-repo/client-service.yml
                         @Value("${driveeasy.client.age-minimum:21}") int ageMinimum) {
        this.repository = repository;
        this.locationClient = locationClient;
        this.ageMinimum = ageMinimum;
    }

    /** GET /clients?nom=..&cin=..&email=.. (partial, case-insensitive; null = no filter). */
    public List<ClientResponse> rechercher(String nom, String cin, String email) {
        return repository.findAll().stream()
                .filter(c -> contient(c.getNom() + " " + c.getPrenom(), nom))
                .filter(c -> contient(c.getCin(), cin))
                .filter(c -> contient(c.getEmail(), email))
                .sorted(Comparator.comparing(Client::getId))
                .map(ClientResponse::from)
                .toList();
    }

    public ClientResponse trouverParId(Long id) {
        return ClientResponse.from(getOrThrow(id));
    }

    public ClientResponse creer(ClientRequest req) {
        verifierUnicite(req, null);
        Client c = new Client();
        copier(req, c);
        c.setCategorieClient(CategorieClient.STANDARD);   // every new customer starts STANDARD
        c.setDateInscription(LocalDate.now());
        return ClientResponse.from(repository.save(c));
    }

    public ClientResponse modifier(Long id, ClientRequest req) {
        Client c = getOrThrow(id);
        verifierUnicite(req, id);
        copier(req, c);   // categorie and dateInscription are not changed here
        return ClientResponse.from(repository.save(c));
    }

    /** Rule: refused (409) if the customer has an active rental -> asks location-service. */
    public void supprimer(Long id) {
        getOrThrow(id);
        if (locationClient.aDesLocationsActives(id)) {
            throw new ConflictException("Impossible de supprimer le client " + id + " : il a une location active");
        }
        repository.deleteById(id);
    }

    /**
     * GET /clients/{id}/eligibilite?dateFin=..
     * Eligible = age >= 21 AND licence not expired (until dateFin if given, else today).
     */
    public EligibiliteResponse eligibilite(Long id, LocalDate dateFin) {
        Client c = getOrThrow(id);
        LocalDate today = LocalDate.now();
        LocalDate dateReference = (dateFin != null && dateFin.isAfter(today)) ? dateFin : today;

        int age = Period.between(c.getDateNaissance(), today).getYears();
        boolean ageOk = age >= ageMinimum;
        boolean permisValide = !c.getDateExpirationPermis().isBefore(dateReference);

        List<String> motifs = new ArrayList<>();
        if (!ageOk) {
            motifs.add("Age insuffisant : " + age + " ans (minimum " + ageMinimum + ")");
        }
        if (!permisValide) {
            motifs.add("Permis expire ou expirant avant le " + dateReference
                    + " (expiration : " + c.getDateExpirationPermis() + ")");
        }
        return new EligibiliteResponse(id, ageOk && permisValide, age, permisValide, motifs);
    }

    /** PATCH /clients/{id}/categorie : called by location-service after a completed rental. */
    public ClientResponse changerCategorie(Long id, CategorieClient categorie) {
        Client c = getOrThrow(id);
        c.setCategorieClient(categorie);
        return ClientResponse.from(repository.save(c));
    }

    // ---------------- helpers ----------------

    private Client getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client " + id + " introuvable"));
    }

    /** Rule: CIN, email and licence number are unique (409). idCourant = null on creation. */
    private void verifierUnicite(ClientRequest req, Long idCourant) {
        // Compared trimmed, the way copier() stores them
        String email = req.email().trim();
        String permis = req.numeroPermis().trim();
        repository.findByCin(req.cin())
                .filter(c -> !c.getId().equals(idCourant))
                .ifPresent(c -> { throw new ConflictException("Le CIN " + req.cin() + " existe deja"); });
        repository.findByEmail(email)
                .filter(c -> !c.getId().equals(idCourant))
                .ifPresent(c -> { throw new ConflictException("L'email " + email + " existe deja"); });
        repository.findByNumeroPermis(permis)
                .filter(c -> !c.getId().equals(idCourant))
                .ifPresent(c -> { throw new ConflictException("Le permis " + permis + " existe deja"); });
    }

    private boolean contient(String valeur, String recherche) {
        return recherche == null || recherche.isBlank()
                || valeur.toLowerCase().contains(recherche.trim().toLowerCase());
    }

    private void copier(ClientRequest req, Client c) {
        c.setCin(req.cin());
        c.setNom(req.nom());
        c.setPrenom(req.prenom());
        c.setDateNaissance(req.dateNaissance());
        c.setEmail(req.email().trim().toLowerCase());
        c.setTelephone(req.telephone());
        c.setAdresse(req.adresse());
        c.setNumeroPermis(req.numeroPermis().trim().toUpperCase());
        c.setDateExpirationPermis(req.dateExpirationPermis());
    }
}
