package com.driveeasy.location.dto;

/**
 * GET /locations/{id}: the rental "enriched" with the customer and the vehicle
 * fetched from the two other microservices (aggregation).
 * If a service is down, its part is null and a warning explains why.
 */
public record LocationDetailResponse(
        LocationResponse location,
        ClientDto client,
        VehiculeDto vehicule,
        java.util.List<String> avertissements
) {}
