package com.driveeasy.client.dto;

import com.driveeasy.client.model.CategorieClient;
import jakarta.validation.constraints.NotNull;

/** Body of PATCH /clients/{id}/categorie (called by location-service). */
public record CategorieRequest(
        @NotNull(message = "La categorie est obligatoire")
        CategorieClient categorie
) {}
