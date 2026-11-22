package com.driveeasy.client.config;

import com.driveeasy.client.model.CategorieClient;
import com.driveeasy.client.model.Client;
import com.driveeasy.client.repository.ClientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Test data loaded at startup. Covers every eligibility case:
 * 1-2 eligible, 3 FIDELE, 4 VIP, 5 too young (< 21), 6 expired licence.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final ClientRepository repository;

    public DataInitializer(ClientRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        LocalDate today = LocalDate.now();
        add("09123456", "Ben Ali", "Ahmed", today.minusYears(35), "ahmed.benali@mail.tn", "21698123456", "Tunis", "TN-100001", today.plusYears(5), CategorieClient.STANDARD);
        add("08765432", "Trabelsi", "Sarra", today.minusYears(28), "sarra.trabelsi@mail.tn", "21655123456", "Sousse", "TN-100002", today.plusYears(3), CategorieClient.STANDARD);
        add("07654321", "Gharbi", "Youssef", today.minusYears(42), "youssef.gharbi@mail.tn", "21622123456", "Sfax", "TN-100003", today.plusYears(7), CategorieClient.FIDELE);
        add("06543210", "Mejri", "Leila", today.minusYears(50), "leila.mejri@mail.tn", "21697123456", "Nabeul", "TN-100004", today.plusYears(2), CategorieClient.VIP);
        add("11223344", "Jlassi", "Amine", today.minusYears(19), "amine.jlassi@mail.tn", "21650123456", "Bizerte", "TN-100005", today.plusYears(9), CategorieClient.STANDARD);
        add("05432109", "Hammami", "Karim", today.minusYears(31), "karim.hammami@mail.tn", "21699123456", "Monastir", "TN-100006", today.minusMonths(2), CategorieClient.STANDARD);
    }

    private void add(String cin, String nom, String prenom, LocalDate naissance, String email, String tel,
                     String adresse, String permis, LocalDate expiration, CategorieClient categorie) {
        Client c = new Client();
        c.setCin(cin);
        c.setNom(nom);
        c.setPrenom(prenom);
        c.setDateNaissance(naissance);
        c.setEmail(email);
        c.setTelephone(tel);
        c.setAdresse(adresse);
        c.setNumeroPermis(permis);
        c.setDateExpirationPermis(expiration);
        c.setCategorieClient(categorie);
        c.setDateInscription(LocalDate.now().minusMonths(6));
        repository.save(c);
    }
}
