package com.driveeasy.vehicule.dto;

import com.driveeasy.vehicule.model.StatutVehicule;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Body of PATCH /vehicules/{id}/statut (called by location-service).
 * kilometrage is optional: sent on return to update the odometer.
 */
public record StatutRequest(
        @NotNull(message = "Le statut est obligatoire")
        StatutVehicule statut,

        @PositiveOrZero(message = "Le kilometrage ne peut pas etre negatif")
        Integer kilometrage
) {}
