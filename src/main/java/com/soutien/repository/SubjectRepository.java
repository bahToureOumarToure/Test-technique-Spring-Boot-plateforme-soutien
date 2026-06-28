package com.soutien.repository;

import com.soutien.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository pour les matières scolaires.
 */
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /**
     * Vérifie qu'une matière du même nom n'existe pas déjà
     * (avant d'en créer une nouvelle).
     */
    boolean existsByName(String name);
}
