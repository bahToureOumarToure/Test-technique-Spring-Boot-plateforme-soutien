package com.soutien.dto;

import com.soutien.entity.Role;
import com.soutien.entity.User;

import java.time.LocalDateTime;

/**
 * DTO de sortie pour un utilisateur.
 * IMPORTANT : on n'expose JAMAIS le champ 'password' (même haché).
 */
public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        LocalDateTime createdAt
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
