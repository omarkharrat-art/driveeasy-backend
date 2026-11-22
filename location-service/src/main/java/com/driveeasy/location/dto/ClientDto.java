package com.driveeasy.location.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Customer as returned by client-service (unknown fields are ignored). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClientDto(
        Long id,
        String cin,
        String nom,
        String prenom,
        String email,
        String categorieClient
) {}
