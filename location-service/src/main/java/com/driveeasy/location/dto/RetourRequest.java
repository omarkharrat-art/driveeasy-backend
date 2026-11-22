package com.driveeasy.location.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

/**
 * Body of PATCH /locations/{id}/retour.
 * dateRetour is optional (default = today); useful to demo a late return.
 */
public record RetourRequest(
        @NotNull(message = "Le kilometrage de retour est obligatoire")
        @PositiveOrZero(message = "Le kilometrage ne peut pas etre negatif")
        Integer kmRetour,

        LocalDate dateRetour
) {}
