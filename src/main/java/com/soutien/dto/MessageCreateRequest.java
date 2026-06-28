package com.soutien.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO d'entrée : envoi d'un message dans le fil d'une demande.
 * (L'auteur et la demande sont déduits du contexte : utilisateur
 * connecté + id de la demande dans l'URL.)
 */
public record MessageCreateRequest(

        @NotBlank(message = "Le contenu du message est obligatoire")
        @Size(max = 2000, message = "Le message ne peut pas dépasser 2000 caractères")
        String content

) {}
