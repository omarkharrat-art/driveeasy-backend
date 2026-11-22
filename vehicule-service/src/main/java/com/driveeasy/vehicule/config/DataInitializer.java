package com.driveeasy.vehicule.config;

import com.driveeasy.vehicule.model.*;
import com.driveeasy.vehicule.repository.VehiculeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Test data loaded at startup (memory is empty after each restart).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final VehiculeRepository repository;

    public DataInitializer(VehiculeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        add("123 TU 4567", "Peugeot", "208", 2022, "Blanc", Carburant.ESSENCE, Boite.MANUELLE, 5, 35000, "90", Categorie.ECONOMIQUE, StatutVehicule.DISPONIBLE);
        add("145 TU 1020", "Renault", "Clio 5", 2023, "Gris", Carburant.DIESEL, Boite.MANUELLE, 5, 22000, "95", Categorie.ECONOMIQUE, StatutVehicule.DISPONIBLE);
        add("201 TU 3344", "Volkswagen", "Golf 8", 2023, "Noir", Carburant.ESSENCE, Boite.AUTOMATIQUE, 5, 18000, "130", Categorie.COMPACTE, StatutVehicule.DISPONIBLE);
        add("210 TU 7788", "Hyundai", "Tucson", 2024, "Bleu", Carburant.HYBRIDE, Boite.AUTOMATIQUE, 5, 9000, "180", Categorie.SUV, StatutVehicule.DISPONIBLE);
        add("220 TU 9001", "Mercedes", "Classe C", 2024, "Noir", Carburant.DIESEL, Boite.AUTOMATIQUE, 5, 12000, "300", Categorie.LUXE, StatutVehicule.DISPONIBLE);
        add("198 TU 5566", "Tesla", "Model 3", 2023, "Rouge", Carburant.ELECTRIQUE, Boite.AUTOMATIQUE, 5, 27000, "250", Categorie.LUXE, StatutVehicule.MAINTENANCE);
        add("187 TU 2233", "Renault", "Trafic", 2021, "Blanc", Carburant.DIESEL, Boite.MANUELLE, 9, 80000, "160", Categorie.UTILITAIRE, StatutVehicule.DISPONIBLE);
    }

    private void add(String immat, String marque, String modele, int annee, String couleur,
                     Carburant carburant, Boite boite, int places, int km, String prix,
                     Categorie categorie, StatutVehicule statut) {
        Vehicule v = new Vehicule();
        v.setImmatriculation(immat);
        v.setMarque(marque);
        v.setModele(modele);
        v.setAnnee(annee);
        v.setCouleur(couleur);
        v.setCarburant(carburant);
        v.setBoite(boite);
        v.setNbPlaces(places);
        v.setKilometrage(km);
        v.setPrixJournalier(new BigDecimal(prix));
        v.setCategorie(categorie);
        v.setStatut(statut);
        v.setImageUrl(null);
        repository.save(v);
    }
}
