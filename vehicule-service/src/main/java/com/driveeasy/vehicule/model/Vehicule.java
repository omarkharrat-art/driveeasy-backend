package com.driveeasy.vehicule.model;

import java.math.BigDecimal;

/**
 * Entity: how a vehicle is stored internally.
 * It is never sent as-is to clients: the controller uses DTOs (dto package).
 */
public class Vehicule {

    private Long id;
    private String immatriculation;
    private String marque;
    private String modele;
    private int annee;
    private String couleur;
    private Carburant carburant;
    private Boite boite;
    private int nbPlaces;
    private int kilometrage;
    private BigDecimal prixJournalier;
    private Categorie categorie;
    private StatutVehicule statut;
    private String imageUrl;            // external image (optional)
    private byte[] imageData;           // uploaded image, kept in memory (no database)
    private String imageContentType;    // ex: image/jpeg

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }

    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }

    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }

    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }

    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }

    public Carburant getCarburant() { return carburant; }
    public void setCarburant(Carburant carburant) { this.carburant = carburant; }

    public Boite getBoite() { return boite; }
    public void setBoite(Boite boite) { this.boite = boite; }

    public int getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(int nbPlaces) { this.nbPlaces = nbPlaces; }

    public int getKilometrage() { return kilometrage; }
    public void setKilometrage(int kilometrage) { this.kilometrage = kilometrage; }

    public BigDecimal getPrixJournalier() { return prixJournalier; }
    public void setPrixJournalier(BigDecimal prixJournalier) { this.prixJournalier = prixJournalier; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public StatutVehicule getStatut() { return statut; }
    public void setStatut(StatutVehicule statut) { this.statut = statut; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }

    public String getImageContentType() { return imageContentType; }
    public void setImageContentType(String imageContentType) { this.imageContentType = imageContentType; }

    public boolean hasImageUploadee() { return imageData != null && imageData.length > 0; }
}
