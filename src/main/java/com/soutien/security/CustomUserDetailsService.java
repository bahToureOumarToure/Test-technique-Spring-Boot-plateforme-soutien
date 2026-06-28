package com.soutien.security;

import com.soutien.entity.User;
import com.soutien.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security ne connaît pas notre classe User : il travaille avec
 * une interface standard "UserDetails". Ce service fait la traduction.
 *
 * Spring l'appelle automatiquement pour retrouver un utilisateur par son
 * "username" (chez nous = l'email).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur introuvable : " + email));

        // On convertit notre User en UserDetails standard de Spring Security.
        // IMPORTANT : Spring attend les rôles préfixés par "ROLE_".
        // Ainsi hasRole('ADMIN') correspond à l'autorité "ROLE_ADMIN".
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),   // le mot de passe HACHÉ stocké en base
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
