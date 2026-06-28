package com.soutien.dto;

import com.soutien.entity.Subject;

/**
 * DTO de SORTIE : ce que l'API renvoie au client pour une matière.
 *
 * Ici on expose tout, mais le principe est qu'on CHOISIT ce qu'on montre
 * (pour une entité User, par exemple, on n'exposerait jamais le password).
 */
public record SubjectResponse(
        Long id,
        String name,
        String description
) {
    /**
     * Méthode utilitaire (fabrique) : convertit une entité Subject
     * en SubjectResponse. Centralise la conversion en un seul endroit.
     */
    public static SubjectResponse fromEntity(Subject subject) {
        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getDescription()
        );
    }
}
