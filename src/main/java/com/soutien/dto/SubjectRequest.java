package com.soutien.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO d'ENTRÉE : ce que le client envoie pour créer/modifier une matière.
 *
 * Les annotations de validation (@NotBlank, @Size) sont vérifiées
 * automatiquement quand le controller reçoit ce DTO avec @Valid.
 * Si une règle est violée -> erreur 400 renvoyée au client.
 *
 * On utilise un 'record' Java : une classe immuable, parfaite pour un DTO.
 * (équivalent d'une classe avec champs finals + constructeur + getters)
 */
public record SubjectRequest(

        @NotBlank(message = "Le nom de la matière est obligatoire")
        @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
        String name,

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        String description

) {}
