package com.soutien.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Une demande de soutien créée par un élève dans une matière.
 * C'est aussi le "fil de discussion" auquel sont rattachés les messages.
 */
@Entity
@Table(name = "support_requests")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String description;     // ce que l'élève demande / son besoin

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    /**
     * L'élève qui a créé la demande.
     * @ManyToOne : PLUSIEURS demandes peuvent appartenir à UN élève.
     * fetch = LAZY : on ne charge l'élève que si on en a besoin (performance).
     * @JoinColumn : crée une colonne "student_id" en base (clé étrangère).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /**
     * L'enseignant affecté. NULL au début (optional = true par défaut),
     * rempli seulement lors de l'affectation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    /**
     * La matière concernée par la demande.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate                     // appelé automatiquement avant chaque mise à jour
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
