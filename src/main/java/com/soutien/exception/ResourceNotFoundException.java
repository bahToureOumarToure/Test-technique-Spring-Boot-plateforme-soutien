package com.soutien.exception;

/**
 * Levée quand une ressource demandée n'existe pas en base
 * (ex: une matière, une demande ou un utilisateur introuvable par son id).
 * Sera transformée en réponse HTTP 404 par le gestionnaire global d'erreurs.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
