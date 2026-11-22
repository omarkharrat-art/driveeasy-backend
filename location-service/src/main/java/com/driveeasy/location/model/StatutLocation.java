package com.driveeasy.location.model;

/**
 * Lifecycle:  RESERVEE --depart--> EN_COURS --retour--> TERMINEE
 *                 \--annulation--> ANNULEE
 */
public enum StatutLocation {
    RESERVEE, EN_COURS, TERMINEE, ANNULEE
}
