package com.driveeasy.location.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Body of POST /locations and POST /locations/simulation. */
public record LocationRequest(
        @NotNull(message = "Le client est obligatoire")
        Long clientId,

        @NotNull(message = "Le vehicule est obligatoire")
        Long vehiculeId,

        @NotNull(message = "La date de debut est obligatoire")
        @FutureOrPresent(message = "La date de debut ne peut pas etre dans le passe")
        LocalDate dateDebut,

        @NotNull(message = "La date de fin prevue est obligatoire")
        LocalDate dateFinPrevue
) {}
