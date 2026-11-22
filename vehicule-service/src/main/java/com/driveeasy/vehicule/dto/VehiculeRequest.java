package com.driveeasy.vehicule.dto;

import com.driveeasy.vehicule.model.Boite;
import com.driveeasy.vehicule.model.Carburant;
import com.driveeasy.vehicule.model.Categorie;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Body received for POST and PUT /vehicules.
 * The annotations are checked automatically thanks to @Valid in the controller
 * (failure -> 400 Bad Request with the list of invalid fields).
 * id and statut are NOT here: the server decides them.
 */
public record VehiculeRequest(
        @NotBlank(message = "L'immatriculation est obligatoire")
        String immatriculation,

        @NotBlank(message = "La marque est obligatoire")
        String marque,

        @NotBlank(message = "Le modele est obligatoire")
        String modele,

        @NotNull(message = "L'annee est obligatoire")
        @Min(value = 1990, message = "L'annee doit etre >= 1990")
        Integer annee,          // <= current year is checked in the service

        String couleur,

        @NotNull(message = "Le carburant est obligatoire")
        Carburant carburant,

        @NotNull(message = "La boite est obligatoire")
        Boite boite,

        @NotNull(message = "Le nombre de places est obligatoire")
        @Min(value = 1, message = "Au moins 1 place")
        @Max(value = 9, message = "Au plus 9 places")
        Integer nbPlaces,

        @NotNull(message = "Le kilometrage est obligatoire")
        @PositiveOrZero(message = "Le kilometrage ne peut pas etre negatif")
        Integer kilometrage,

        @NotNull(message = "Le prix journalier est obligatoire")
        @Positive(message = "Le prix journalier doit etre strictement positif")
        BigDecimal prixJournalier,

        @NotNull(message = "La categorie est obligatoire")
        Categorie categorie,

        String imageUrl
) {}
