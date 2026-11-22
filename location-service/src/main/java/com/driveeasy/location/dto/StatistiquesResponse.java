package com.driveeasy.location.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Indicators for the dashboard. */
public record StatistiquesResponse(
        BigDecimal chiffreAffairesTotal,     // completed rentals (price + penalties)
        BigDecimal chiffreAffairesMois,      // completed this month
        Map<String, Long> locationsParStatut,
        Double tauxOccupation,               // % of vehicles currently LOUE (null if vehicule-service down)
        List<TopVehicule> topVehicules
) {
    public record TopVehicule(Long vehiculeId, String marque, String modele, long nbLocations) {}
}
