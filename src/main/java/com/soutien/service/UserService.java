package com.soutien.service;

import com.soutien.dto.UserResponse;
import com.soutien.entity.User;
import com.soutien.exception.ResourceNotFoundException;
import com.soutien.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logique métier liée aux utilisateurs.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Récupère l'entité User de l'utilisateur ACTUELLEMENT CONNECTÉ.
     *
     * Comment ? Notre filtre JWT a placé un UserDetails dans le
     * SecurityContext. On en lit l'email (username), puis on retrouve
     * l'entité complète en base.
     *
     * Cette méthode sera très utilisée : "qui fait l'action ?"
     * (créer une demande, envoyer un message, etc.)
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        String email = (principal instanceof UserDetails ud)
                ? ud.getUsername()
                : principal.toString();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur connecté introuvable : " + email));
    }

    /**
     * Liste tous les utilisateurs (réservé à l'admin, contrôlé au controller).
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    /**
     * Détail d'un utilisateur par son id.
     */
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur introuvable avec l'id " + id));
        return UserResponse.fromEntity(user);
    }
}
