package com.soutien.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Format unifié de TOUTES les réponses d'erreur de l'API.
 *
 * @JsonInclude(NON_NULL) : les champs null (ex: fieldErrors quand ce
 * n'est pas une erreur de validation) ne sont pas affichés dans le JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors  // détail des champs invalides (validation)
) {
    public static ApiError of(int status, String error, String message) {
        return new ApiError(LocalDateTime.now(), status, error, message, null);
    }

    public static ApiError validation(int status, String error, String message,
                                      Map<String, String> fieldErrors) {
        return new ApiError(LocalDateTime.now(), status, error, message, fieldErrors);
    }
}
