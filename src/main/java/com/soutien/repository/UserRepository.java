package com.soutien.repository;

import com.soutien.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository pour les utilisateurs.
 *
 * En héritant de JpaRepository<User, Long> on obtient GRATUITEMENT :
 *   save(user), findById(id), findAll(), deleteById(id), count()
 * (User = le type de l'entité, Long = le type de sa clé primaire 'id')
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Spring génère automatiquement la requête :
     *   SELECT * FROM users WHERE email = ?
     * juste à partir du nom de la méthode "findByEmail".
     *
     * Optional = "peut-être un User, peut-être rien" alrs  évite les NullPointerException.
     * Sert au login : retrouver l'utilisateur par son email.
     */
    Optional<User> findByEmail(String email);

    /**
     * SELECT COUNT(*) > 0 FROM users WHERE email = ?
     * Sert à vérifier, à l'inscription, qu'un email n'est pas déjà pris.
     * evite des doublons users et tout !
     *
     */
    boolean existsByEmail(String email);
}
