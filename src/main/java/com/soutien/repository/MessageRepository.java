package com.soutien.repository;

import com.soutien.entity.Message;
import com.soutien.entity.SupportRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository pour les messages.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Tous les messages d'une demande, triés du plus ancien au plus récent.
     *
     * Décomposition du nom de la méthode :
     *   findBy + Request          -> WHERE request = ?
     *   OrderBy + SentAt + Asc    -> ORDER BY sent_at ASC
     *
     * Usage : afficher l'historique d'une conversation dans l'ordre chronologique.
     */
    List<Message> findByRequestOrderBySentAtAsc(SupportRequest request);
}
