package com.soutien.service;

import com.soutien.dto.MessageCreateRequest;
import com.soutien.dto.MessageResponse;
import com.soutien.entity.Message;
import com.soutien.entity.RequestStatus;
import com.soutien.entity.Role;
import com.soutien.entity.SupportRequest;
import com.soutien.entity.User;
import com.soutien.exception.BusinessException;
import com.soutien.exception.ResourceNotFoundException;
import com.soutien.repository.MessageRepository;
import com.soutien.repository.SupportRequestRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logique métier de la messagerie.
 * Les messages sont rattachés à une demande (le "fil de discussion").
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final SupportRequestRepository requestRepository;
    private final UserService userService;

    public MessageService(MessageRepository messageRepository,
                          SupportRequestRepository requestRepository,
                          UserService userService) {
        this.messageRepository = messageRepository;
        this.requestRepository = requestRepository;
        this.userService = userService;
    }

    /**
     * Envoie un message dans le fil d'une demande.
     * Règles :
     *  - l'auteur doit être l'élève propriétaire OU l'enseignant affecté ;
     *  - un enseignant doit être affecté (sinon il n'y a personne à qui parler) ;
     *  - la demande ne doit pas être annulée.
     */
    @Transactional
    public MessageResponse sendMessage(Long requestId, MessageCreateRequest dto) {
        SupportRequest request = findRequestOrThrow(requestId);
        User sender = userService.getCurrentUser();

        if (request.getStatus() == RequestStatus.CANCELLED) {
            throw new BusinessException(
                    "Impossible d'envoyer un message : la demande est annulée");
        }
        if (request.getTeacher() == null) {
            throw new BusinessException(
                    "Aucun enseignant n'est encore affecté à cette demande");
        }
        ensureParticipant(request, sender);

        Message message = Message.builder()
                .content(dto.content())
                .sender(sender)
                .request(request)
                .build();

        return MessageResponse.fromEntity(messageRepository.save(message));
    }

    /**
     * Historique des messages d'une demande, du plus ancien au plus récent.
     * Lecture autorisée : élève propriétaire, enseignant affecté, ou admin.
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getHistory(Long requestId) {
        SupportRequest request = findRequestOrThrow(requestId);
        User current = userService.getCurrentUser();
        ensureCanRead(request, current);

        return messageRepository.findByRequestOrderBySentAtAsc(request)
                .stream()
                .map(MessageResponse::fromEntity)
                .toList();
    }

    // ----------------- méthodes internes -----------------

    private SupportRequest findRequestOrThrow(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Demande introuvable avec l'id " + id));
    }

    /**
     * Pour ÉCRIRE : l'utilisateur doit être un participant du fil
     * (l'élève propriétaire ou l'enseignant affecté).
     */
    private void ensureParticipant(SupportRequest request, User user) {
        boolean isOwner = request.getStudent().getId().equals(user.getId());
        boolean isAssignedTeacher = request.getTeacher() != null
                && request.getTeacher().getId().equals(user.getId());

        if (!isOwner && !isAssignedTeacher) {
            throw new AccessDeniedException(
                    "Vous ne participez pas à cette discussion");
        }
    }

    /**
     * Pour LIRE : participants + admin (supervision).
     */
    private void ensureCanRead(SupportRequest request, User user) {
        if (user.getRole() == Role.ADMIN) return;
        ensureParticipant(request, user);
    }
}
