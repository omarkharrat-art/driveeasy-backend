package com.driveeasy.location.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Detailed price computation (also used before creating a reservation).
 * montantBrut = nbJours x prixJournalier x coefficientCategorie
 * montantTotal = montantBrut - remise   (remise = montantBrut x tauxRemise)
 */
public record SimulationResponse(
        Long clientId,
        Long vehiculeId,
        LocalDate dateDebut,
        LocalDate dateFinPrevue,
        long nbJours,
        BigDecimal prixJournalier,
        double coefficientCategorie,
        String categorieClient,
        BigDecimal tauxRemise,
        BigDecimal montantBrut,
        BigDecimal remise,
        BigDecimal montantTotal
) {}
