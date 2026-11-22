package com.driveeasy.vehicule.dto;

import com.driveeasy.vehicule.model.*;

import java.math.BigDecimal;

/**
 * What the API returns for a vehicle.
 * Includes the category coefficient so location-service can compute the price.
 */
public record VehiculeResponse(
        Long id,
        String immatriculation,
        String marque,
        String modele,
        int annee,
        String couleur,
        Carburant carburant,
        Boite boite,
        int nbPlaces,
        int kilometrage,
        BigDecimal prixJournalier,
        Categorie categorie,
        double coefficientCategorie,
        StatutVehicule statut,
        String imageUrl
) {
    /**
     * Converts an entity into a response DTO.
     * If an image was uploaded, imageUrl = "/vehicules/{id}/image" (relative path:
     * the frontend prefixes it with the gateway URL http://localhost:8080/api).
     */
    public static VehiculeResponse from(Vehicule v) {
        return new VehiculeResponse(
                v.getId(), v.getImmatriculation(), v.getMarque(), v.getModele(),
                v.getAnnee(), v.getCouleur(), v.getCarburant(), v.getBoite(),
                v.getNbPlaces(), v.getKilometrage(), v.getPrixJournalier(),
                v.getCategorie(), v.getCategorie().getCoefficient(),
                v.getStatut(),
                v.hasImageUploadee() ? "/vehicules/" + v.getId() + "/image" : v.getImageUrl());
    }
}
