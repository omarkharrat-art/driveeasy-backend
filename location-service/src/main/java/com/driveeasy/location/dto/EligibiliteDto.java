package com.driveeasy.location.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Answer of GET /clients/{id}/eligibilite. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EligibiliteDto(
        Long clientId,
        boolean eligible,
        int age,
        boolean permisValide,
        List<String> motifs
) {}
