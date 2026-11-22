package com.driveeasy.vehicule.model;

/**
 * Vehicle category. The coefficient is used by location-service in the price:
 * prix = jours x prixJournalier x coefficient - remise fidelite
 */
public enum Categorie {
    ECONOMIQUE(1.0),
    COMPACTE(1.1),
    SUV(1.3),
    LUXE(1.5),
    UTILITAIRE(1.2);

    private final double coefficient;

    Categorie(double coefficient) {
        this.coefficient = coefficient;
    }

    public double getCoefficient() {
        return coefficient;
    }
}
