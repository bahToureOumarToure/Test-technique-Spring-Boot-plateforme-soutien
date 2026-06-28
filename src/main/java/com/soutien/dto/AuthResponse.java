package com.soutien.dto;

import com.soutien.entity.Role;

/**
 * DTO renvoyé après une connexion (ou inscription) réussie.
 * Le client devra renvoyer ce 'token' dans le header
 * Authorization: Bearer <token> pour accéder aux ressources protégées.
 */
public record AuthResponse(
        String token,
        String tokenType,   // toujours "Bearer"
        String email,
        Role role
) {
    public static AuthResponse of(String token, String email, Role role) {
        return new AuthResponse(token, "Bearer", email, role);
    }
}
