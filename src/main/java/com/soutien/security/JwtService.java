package com.soutien.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Service responsable de TOUT ce qui touche au token JWT :
 *   - le fabriquer (à la connexion)
 *   - en extraire l'email et le rôle
 *   - vérifier qu'il est valide (signature + non expiré)
 *
 * La clé secrète et la durée de validité viennent de application.yml
 * (section app.jwt), injectées via @Value.
 */
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        // On transforme notre texte secret en clé cryptographique pour HMAC-SHA256.
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Fabrique un token pour un utilisateur.
     * - subject  = l'email (l'identifiant principal)
     * - claim "role" = le rôle, pour pouvoir l'utiliser côté autorisation
     * - issuedAt / expiration = dates d'émission et d'expiration
     * - signWith = appose la signature avec notre clé secrète
     */
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extrait l'email (le 'subject') contenu dans le token.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Vérifie qu'un token est valide POUR un email donné :
     *   - l'email du token correspond
     *   - le token n'est pas expiré
     * (la signature, elle, est vérifiée lors du parsing : si elle est
     *  invalide, parseSignedClaims lève une exception interceptée plus haut.)
     */
    public boolean isTokenValid(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return tokenEmail.equals(email) && !isTokenExpired(token);
    }

    // ----------------- méthodes internes -----------------

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Méthode générique : lit toutes les "claims" (données) du token,
     * puis applique la fonction passée pour en extraire une valeur précise.
     * verifyWith(secretKey) -> c'est ICI que la signature est vérifiée.
     */
    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
