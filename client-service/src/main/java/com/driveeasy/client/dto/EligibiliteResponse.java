package com.driveeasy.client.dto;

import java.util.List;

/**
 * Result of GET /clients/{id}/eligibilite (called by location-service before a reservation).
 * motifs lists the reasons when eligible = false.
 */
public record EligibiliteResponse(
        Long clientId,
        boolean eligible,
        int age,
        boolean permisValide,
        List<String> motifs
) {}
