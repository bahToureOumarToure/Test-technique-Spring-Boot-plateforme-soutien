package com.soutien.repository;

import com.soutien.entity.RequestStatus;
import com.soutien.entity.SupportRequest;
import com.soutien.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository pour les demandes de soutien.
 */
public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {

    /**
     * Toutes les demandes ayant un statut donné.
     * Usage : l'enseignant consulte les demandes "disponibles"
     *         => findByStatus(RequestStatus.CREATED)
     */
    List<SupportRequest> findByStatus(RequestStatus status);

    /**
     * Toutes les demandes créées par un élève donné.
     * Usage : "voir MES demandes".
     */
    List<SupportRequest> findByStudent(User student);

    /**
     * Toutes les demandes affectées à un enseignant donné.
     * Usage : "voir les demandes dont je m'occupe".
     */
    List<SupportRequest> findByTeacher(User teacher);
}
