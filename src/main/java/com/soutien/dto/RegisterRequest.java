package com.soutien.dto;

import com.soutien.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO d'inscription : données envoyées pour créer un compte.
 *
 * NB sécurité : ici on autorise à choisir son rôle (pratique pour tester
 * les 3 rôles). Dans une vraie application, on restreindrait la création
 * d'ADMIN (réservée à un admin existant). On l'assume comme simplification.
 */
public record RegisterRequest(

        @NotBlank(message = "Le nom complet est obligatoire")
        String fullName,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email doit être valide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 6, message = "Le mot de passe doit faire au moins 6 caractères")
        String password,

        @NotNull(message = "Le rôle est obligatoire (STUDENT, TEACHER ou ADMIN)")
        Role role

) {}
