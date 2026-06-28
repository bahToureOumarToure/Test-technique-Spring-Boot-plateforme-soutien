package com.soutien.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre exécuté UNE FOIS par requête (OncePerRequestFilter), AVANT
 * d'atteindre les controllers. Son rôle :
 *   1. lire le header Authorization
 *   2. si un token "Bearer ..." est présent et valide,
 *      placer l'utilisateur dans le SecurityContext (= "il est authentifié")
 *
 * Si pas de token / token invalide : on ne fait rien, la requête continue
 * mais restera non authentifiée -> sera bloquée si l'endpoint est protégé.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Pas de token "Bearer" -> on laisse passer sans authentifier.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String token = authHeader.substring(7); // enlève "Bearer "
            final String email = jwtService.extractEmail(token);

            // On authentifie seulement si on a un email ET qu'on ne l'est pas déjà.
            if (email != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(token, userDetails.getUsername())) {
                    // On crée un "jeton d'authentification" Spring avec les rôles.
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // On déclare l'utilisateur comme authentifié pour cette requête.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token expiré, signature invalide, etc. : on ignore -> non authentifié.
            // (On ne casse pas la chaîne, l'accès sera simplement refusé plus loin.)
        }

        filterChain.doFilter(request, response);
    }
}
