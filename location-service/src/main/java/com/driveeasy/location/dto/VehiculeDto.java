package com.driveeasy.location.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** Vehicle as returned by vehicule-service (unknown fields are ignored). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VehiculeDto(
        Long id,
        String immatriculation,
        String marque,
        String modele,
        int kilometrage,
        BigDecimal prixJournalier,
        String categorie,
        double coefficientCategorie,
        String statut
) {}
