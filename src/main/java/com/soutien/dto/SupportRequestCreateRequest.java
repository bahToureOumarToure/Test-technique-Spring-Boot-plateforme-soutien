package com.soutien.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO d'entrée : un élève crée une demande de soutien.
 * Il fournit la matière concernée et la description de son besoin.
 * (L'élève n'est PAS dans le DTO : on le déduit de l'utilisateur connecté.)
 */
public record SupportRequestCreateRequest(

        @NotNull(message = "L'identifiant de la matière est obligatoire")
        Long subjectId,

        @NotBlank(message = "La description du besoin est obligatoire")
        @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
        String description

) {}
