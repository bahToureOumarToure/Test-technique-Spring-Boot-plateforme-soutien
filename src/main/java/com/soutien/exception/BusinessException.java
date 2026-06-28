package com.soutien.exception;

/**
 * Levée quand une RÈGLE MÉTIER est violée
 * (ex: une matière du même nom existe déjà, ou on tente une transition
 * de statut interdite comme s'affecter à une demande déjà terminée).
 * Sera transformée en réponse HTTP 400 par le gestionnaire global d'erreurs.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
