package com.soutien.dto;

import com.soutien.entity.RequestStatus;
import com.soutien.entity.SupportRequest;

import java.time.LocalDateTime;

/**
 * DTO de sortie d'une demande de soutien.
 * On "aplatit" les entités liées (élève, enseignant, matière) en
 * id + nom, pour un JSON simple et sans exposer les entités complètes.
 */
public record SupportRequestResponse(
        Long id,
        String description,
        RequestStatus status,
        Long subjectId,
        String subjectName,
        Long studentId,
        String studentName,
        Long teacherId,      // null tant qu'aucun enseignant n'est affecté
        String teacherName,  // null également
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SupportRequestResponse fromEntity(SupportRequest r) {
        return new SupportRequestResponse(
                r.getId(),
                r.getDescription(),
                r.getStatus(),
                r.getSubject().getId(),
                r.getSubject().getName(),
                r.getStudent().getId(),
                r.getStudent().getFullName(),
                r.getTeacher() != null ? r.getTeacher().getId() : null,
                r.getTeacher() != null ? r.getTeacher().getFullName() : null,
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
