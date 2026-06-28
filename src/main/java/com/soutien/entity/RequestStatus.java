package com.soutien.entity;

/**
 * Le cycle de vie d'une demande de soutien.
 *
 *   CREATED ──► IN_PROGRESS ──► COMPLETED
 *      │
 *      └──────► CANCELLED
 */
public enum RequestStatus {
    CREATED,      // créée par l'élève, aucun enseignant affecté
    IN_PROGRESS,  // un enseignant s'est affecté, accompagnement en cours
    COMPLETED,    // terminée (validée par l'élève ou l'admin)
    CANCELLED     // annulée
}
