package com.driveeasy.location.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity: a rental. clientId / vehiculeId are only references:
 * customers and vehicles live in their own microservices.
 * Prices are copied at reservation time ("applique") so a later price change
 * does not modify an existing rental.
 */
public class Location {

    private Long id;
    private Long clientId;
    private Long vehiculeId;
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;
    private LocalDate dateRetourEffectif;
    private Integer kmDepart;
    private Integer kmRetour;
    private BigDecimal prixJournalierApplique;
    private double coefficientCategorie;
    private BigDecimal tauxRemise;
    private BigDecimal montantTotal;
    private BigDecimal penalite = BigDecimal.ZERO;
    private StatutLocation statut;
    private LocalDateTime dateCreation;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(Long vehiculeId) { this.vehiculeId = vehiculeId; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }

    public LocalDate getDateRetourEffectif() { return dateRetourEffectif; }
    public void setDateRetourEffectif(LocalDate dateRetourEffectif) { this.dateRetourEffectif = dateRetourEffectif; }

    public Integer getKmDepart() { return kmDepart; }
    public void setKmDepart(Integer kmDepart) { this.kmDepart = kmDepart; }

    public Integer getKmRetour() { return kmRetour; }
    public void setKmRetour(Integer kmRetour) { this.kmRetour = kmRetour; }

    public BigDecimal getPrixJournalierApplique() { return prixJournalierApplique; }
    public void setPrixJournalierApplique(BigDecimal prixJournalierApplique) { this.prixJournalierApplique = prixJournalierApplique; }

    public double getCoefficientCategorie() { return coefficientCategorie; }
    public void setCoefficientCategorie(double coefficientCategorie) { this.coefficientCategorie = coefficientCategorie; }

    public BigDecimal getTauxRemise() { return tauxRemise; }
    public void setTauxRemise(BigDecimal tauxRemise) { this.tauxRemise = tauxRemise; }

    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }

    public BigDecimal getPenalite() { return penalite; }
    public void setPenalite(BigDecimal penalite) { this.penalite = penalite; }

    public StatutLocation getStatut() { return statut; }
    public void setStatut(StatutLocation statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    /** True for RESERVEE and EN_COURS (the vehicle is "taken" for the period). */
    public boolean estActive() {
        return statut == StatutLocation.RESERVEE || statut == StatutLocation.EN_COURS;
    }

    /** Inclusive period overlap: [dateDebut, dateFinPrevue] intersects [debut, fin]. */
    public boolean chevauche(LocalDate debut, LocalDate fin) {
        return !dateDebut.isAfter(fin) && !dateFinPrevue.isBefore(debut);
    }
}
