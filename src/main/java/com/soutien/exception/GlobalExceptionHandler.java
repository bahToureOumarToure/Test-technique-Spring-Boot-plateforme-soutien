package com.soutien.exception;

import com.soutien.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire GLOBAL des erreurs.
 *
 * @RestControllerAdvice : cette classe intercepte les exceptions levées
 * par N'IMPORTE QUEL controller, et renvoie une réponse JSON cohérente
 * avec le bon code HTTP. C'est "la porte unique" pour les erreurs.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Ressource introuvable -> 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(404, "Not Found", ex.getMessage()));
    }

    /**
     * Règle métier violée (ex: transition de statut interdite,
     * email déjà utilisé) -> 400 Bad Request.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(400, "Bad Request", ex.getMessage()));
    }

    /**
     * Accès refusé (propriété/rôle vérifié dans le service) -> 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(403, "Forbidden", ex.getMessage()));
    }

    /**
     * Données entrantes invalides (@Valid sur un DTO) -> 400 Bad Request,
     * avec le détail champ par champ.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.validation(400, "Bad Request",
                        "Données invalides", fieldErrors));
    }

    /**
     * Filet de sécurité : toute autre exception non prévue -> 500.
     * (En production on logguerait la stack trace ici.)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(500, "Internal Server Error",
                        "Une erreur inattendue est survenue"));
    }
}
