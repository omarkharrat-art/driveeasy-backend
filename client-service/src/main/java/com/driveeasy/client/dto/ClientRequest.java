package com.driveeasy.client.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Body of POST / PUT /clients.
 * id, categorieClient and dateInscription are decided by the server.
 */
public record ClientRequest(
        @NotBlank(message = "Le CIN est obligatoire")
        @Pattern(regexp = "\\d{8}", message = "Le CIN doit contenir exactement 8 chiffres")
        String cin,

        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotBlank(message = "Le prenom est obligatoire")
        String prenom,

        @NotNull(message = "La date de naissance est obligatoire")
        @Past(message = "La date de naissance doit etre dans le passe")
        LocalDate dateNaissance,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide")
        String email,

        @Pattern(regexp = "^$|^\\+?\\d{8,15}$", message = "Telephone invalide (8 a 15 chiffres)")
        String telephone,

        String adresse,

        @NotBlank(message = "Le numero de permis est obligatoire")
        String numeroPermis,

        @NotNull(message = "La date d'expiration du permis est obligatoire")
        LocalDate dateExpirationPermis
) {}
