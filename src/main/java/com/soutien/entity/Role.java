package com.soutien.entity;

/**
 * Les trois rôles possibles d'un utilisateur sur la plateforme.
 * Un enum garantit qu'on ne peut PAS avoir une valeur invalide
 * (ex: "eleve2" ou une faute de frappe) : seules ces 3 valeurs existent.
 */
public enum Role {
    STUDENT,   // élève
    TEACHER,   // enseignant
    ADMIN      // administrateur
}
