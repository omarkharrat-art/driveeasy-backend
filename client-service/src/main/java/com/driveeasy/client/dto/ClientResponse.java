package com.driveeasy.client.dto;

import com.driveeasy.client.model.CategorieClient;
import com.driveeasy.client.model.Client;

import java.time.LocalDate;
import java.time.Period;

/**
 * What the API returns for a customer.
 * age and permisExpire are computed (handy for the frontend "permis expire" alert).
 */
public record ClientResponse(
        Long id,
        String cin,
        String nom,
        String prenom,
        LocalDate dateNaissance,
        int age,
        String email,
        String telephone,
        String adresse,
        String numeroPermis,
        LocalDate dateExpirationPermis,
        boolean permisExpire,
        CategorieClient categorieClient,
        LocalDate dateInscription
) {
    public static ClientResponse from(Client c) {
        LocalDate today = LocalDate.now();
        return new ClientResponse(
                c.getId(), c.getCin(), c.getNom(), c.getPrenom(), c.getDateNaissance(),
                Period.between(c.getDateNaissance(), today).getYears(),
                c.getEmail(), c.getTelephone(), c.getAdresse(), c.getNumeroPermis(),
                c.getDateExpirationPermis(), c.getDateExpirationPermis().isBefore(today),
                c.getCategorieClient(), c.getDateInscription());
    }
}
