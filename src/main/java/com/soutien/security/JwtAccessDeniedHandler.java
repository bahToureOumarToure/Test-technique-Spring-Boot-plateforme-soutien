package com.soutien.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Appelé quand un utilisateur AUTHENTIFIÉ tente une action pour laquelle
 * il n'a PAS le rôle requis (ex: un élève qui veut créer une matière).
 *
 * On écrit directement la réponse 403 (au lieu de response.sendError(),
 * ce qui éviterait une redirection interne vers /error). Résultat :
 * un vrai 403 "Forbidden", distinct du 401 "Unauthorized".
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"status\":403,\"error\":\"Forbidden\","
                        + "\"message\":\"Accès refusé : vous n'avez pas le rôle requis pour cette action\"}"
        );
    }
}
