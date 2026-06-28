package com.soutien.config;

import com.soutien.security.CustomUserDetailsService;
import com.soutien.security.JwtAccessDeniedHandler;
import com.soutien.security.JwtAuthEntryPoint;
import com.soutien.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration centrale de la sécurité.
 *
 * @EnableMethodSecurity : active les annotations @PreAuthorize sur les
 * méthodes des controllers (ex: @PreAuthorize("hasRole('Role_x')")).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomUserDetailsService userDetailsService,
                          JwtAuthEntryPoint jwtAuthEntryPoint,
                          JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    /**
     * L'encodeur de mots de passe : BCrypt (hachage fort, avec "sel" intégré).
     * Utilisé à l'inscription (encode) et à la connexion (comparaison).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Le "fournisseur d'authentification" : relie notre UserDetailsService
     * (pour retrouver l'utilisateur) et le PasswordEncoder (pour vérifier
     * le mot de passe).
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * L'AuthenticationManager : utilisé dans AuthService pour déclencher
     * la vérification email + mot de passe lors du login.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * La chaîne de filtres de sécurité : LE cœur de la config.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. On désactive CSRF : inutile pour une API REST stateless
            //    (pas de cookies de session, donc pas vulnérable au CSRF).
                // dan  le cas contraire ! obliger
            .csrf(AbstractHttpConfigurer::disable)

            // 2. Pas de session : chaque requête s'authentifie via son token.
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 3. Quoi est public, quoi est protégé.
            .authorizeHttpRequests(auth -> auth
                    // Inscription et connexion : accessibles sans token.
                    .requestMatchers("/api/auth/**").permitAll()
                    // (déclenchée par un 403) repasse en "anonyme" et
                    // transforme le 403 en 401. (Voir filtre OncePerRequestFilter.)
                    .requestMatchers("/error").permitAll()
                    // Documentation Swagger : publique (pratique pour l'évaluateur).
                    .requestMatchers(
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**").permitAll()
                    // Tout le reste exige d'être authentifié.
                    .anyRequest().authenticated()
            )

            // 4. Réponses d'erreur propres :
            //    - 401 si non authentifié (entry point)
            //    - 403 si authentifié mais rôle insuffisant (access denied handler)
            .exceptionHandling(eh -> eh
                    .authenticationEntryPoint(jwtAuthEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler))

            // 5. On enregistre notre fournisseur d'authentification.
            .authenticationProvider(authenticationProvider())

            // 6. On insère notre filtre JWT AVANT le filtre standard de Spring.
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
