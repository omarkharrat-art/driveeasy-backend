package com.driveeasy.location.config;

import com.driveeasy.location.model.Location;
import com.driveeasy.location.model.StatutLocation;
import com.driveeasy.location.repository.LocationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Rental history loaded at startup, consistent with the seed data of the other services:
 *  - client 1 (STANDARD) : 4 completed -> his next return makes him FIDELE (demo!)
 *  - client 3 (FIDELE)   : 6 completed
 *  - client 4 (VIP)      : 2 completed
 *  - client 2            : 1 future reservation -> deleting client 2 is refused (409)
 * Prices / coefficients copy those of vehicule-service (id -> price, coefficient).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final LocationRepository repository;

    public DataInitializer(LocationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        LocalDate t = LocalDate.now();
        // client 1 : 4 completed
        terminee(1L, 1L, "90", 1.0, "0", t.minusMonths(5), 3, 0);
        terminee(1L, 2L, "95", 1.0, "0", t.minusMonths(4), 2, 0);
        terminee(1L, 3L, "130", 1.1, "0", t.minusMonths(2), 4, 1);   // returned 1 day late
        terminee(1L, 1L, "90", 1.0, "0", t.minusDays(12), 3, 0);
        // client 3 (FIDELE, 5 %) : 6 completed
        terminee(3L, 4L, "180", 1.3, "0.05", t.minusMonths(6), 5, 0);
        terminee(3L, 4L, "180", 1.3, "0.05", t.minusMonths(5), 2, 0);
        terminee(3L, 5L, "300", 1.5, "0.05", t.minusMonths(3), 3, 0);
        terminee(3L, 3L, "130", 1.1, "0.05", t.minusMonths(2), 4, 0);
        terminee(3L, 4L, "180", 1.3, "0.05", t.minusMonths(1), 7, 2);
        terminee(3L, 7L, "160", 1.2, "0.05", t.minusDays(9), 2, 0);
        // client 4 (VIP, 10 %) : 2 completed
        terminee(4L, 5L, "300", 1.5, "0.10", t.minusMonths(2), 4, 0);
        terminee(4L, 5L, "300", 1.5, "0.10", t.minusDays(15), 3, 0);
        // client 2 : future reservation on vehicle 3
        reservee(2L, 3L, "130", 1.1, t.plusDays(10), 5);
    }

    private void terminee(Long clientId, Long vehiculeId, String prix, double coef, String remise,
                          LocalDate debut, int jours, int joursRetard) {
        Location l = base(clientId, vehiculeId, prix, coef, remise, debut, jours);
        BigDecimal p = new BigDecimal(prix);
        l.setDateRetourEffectif(debut.plusDays(jours + joursRetard));
        l.setKmDepart(10000);
        l.setKmRetour(10000 + 250 * (jours + joursRetard));
        l.setPenalite(p.multiply(new BigDecimal("1.5")).multiply(BigDecimal.valueOf(joursRetard))
                .setScale(2, RoundingMode.HALF_UP));
        l.setStatut(StatutLocation.TERMINEE);
        repository.save(l);
    }

    private void reservee(Long clientId, Long vehiculeId, String prix, double coef, LocalDate debut, int jours) {
        Location l = base(clientId, vehiculeId, prix, coef, "0", debut, jours);
        l.setStatut(StatutLocation.RESERVEE);
        repository.save(l);
    }

    private Location base(Long clientId, Long vehiculeId, String prix, double coef, String remise,
                          LocalDate debut, int jours) {
        BigDecimal p = new BigDecimal(prix);
        BigDecimal taux = new BigDecimal(remise);
        BigDecimal brut = p.multiply(BigDecimal.valueOf(jours)).multiply(BigDecimal.valueOf(coef))
                .setScale(2, RoundingMode.HALF_UP);
        Location l = new Location();
        l.setClientId(clientId);
        l.setVehiculeId(vehiculeId);
        l.setDateDebut(debut);
        l.setDateFinPrevue(debut.plusDays(jours));
        l.setPrixJournalierApplique(p);
        l.setCoefficientCategorie(coef);
        l.setTauxRemise(taux);
        l.setMontantTotal(brut.subtract(brut.multiply(taux).setScale(2, RoundingMode.HALF_UP)));
        l.setDateCreation(debut.minusDays(3).atStartOfDay());
        return l;
    }
}
