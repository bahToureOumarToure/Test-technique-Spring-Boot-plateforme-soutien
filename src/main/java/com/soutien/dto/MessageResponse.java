package com.soutien.dto;

import com.soutien.entity.Message;
import com.soutien.entity.Role;

import java.time.LocalDateTime;

/**
 * DTO de sortie d'un message.
 * On inclut le rôle de l'auteur pour distinguer facilement
 * qui parle (élève ou enseignant) dans l'historique.
 */
public record MessageResponse(
        Long id,
        String content,
        Long senderId,
        String senderName,
        Role senderRole,
        LocalDateTime sentAt
) {
    public static MessageResponse fromEntity(Message m) {
        return new MessageResponse(
                m.getId(),
                m.getContent(),
                m.getSender().getId(),
                m.getSender().getFullName(),
                m.getSender().getRole(),
                m.getSentAt()
        );
    }
}
