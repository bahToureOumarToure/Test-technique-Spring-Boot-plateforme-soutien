package com.soutien.service;

import com.soutien.dto.AuthResponse;
import com.soutien.dto.LoginRequest;
import com.soutien.dto.RegisterRequest;
import com.soutien.entity.User;
import com.soutien.exception.BusinessException;
import com.soutien.repository.UserRepository;
import com.soutien.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logique métier de l'authentification : inscription et connexion.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Inscription : crée l'utilisateur avec un mot de passe HACHÉ,
     * puis renvoie directement un token (l'utilisateur est connecté d'emblée).
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Un compte existe déjà avec l'email " + request.email());
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                // On NE stocke JAMAIS le mot de passe en clair : on le hache.
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return AuthResponse.of(token, user.getEmail(), user.getRole());
    }

    /**
     * Connexion : vérifie email + mot de passe via l'AuthenticationManager,
     * puis renvoie un token si tout est bon.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Déclenche la vérification : si le couple email/mot de passe est faux,
        // une AuthenticationException est levée (gérée par le handler global).
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        // Si on arrive ici, l'authentification a réussi.
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(); // ne devrait jamais arriver après authenticate()

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return AuthResponse.of(token, user.getEmail(), user.getRole());
    }
}
