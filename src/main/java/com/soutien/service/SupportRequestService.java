package com.soutien.service;

import com.soutien.dto.SupportRequestCreateRequest;
import com.soutien.dto.SupportRequestResponse;
import com.soutien.entity.*;
import com.soutien.exception.BusinessException;
import com.soutien.exception.ResourceNotFoundException;
import com.soutien.repository.SubjectRepository;
import com.soutien.repository.SupportRequestRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logique métier des demandes de soutien.
 * Gère le cycle de vie (CREATED -> IN_PROGRESS -> COMPLETED / CANCELLED)
 * et les droits (qui a le droit de faire quoi).
 */
@Service
public class SupportRequestService {

    private final SupportRequestRepository requestRepository;
    private final SubjectRepository subjectRepository;
    private final UserService userService;

    public SupportRequestService(SupportRequestRepository requestRepository,
                                 SubjectRepository subjectRepository,
                                 UserService userService) {
        this.requestRepository = requestRepository;
        this.subjectRepository = subjectRepository;
        this.userService = userService;
    }

    /**
     * Un ÉLÈVE crée une demande.
     * Statut initial = CREATED, aucun enseignant affecté.
     */
    @Transactional
    public SupportRequestResponse create(SupportRequestCreateRequest request) {
        User student = userService.getCurrentUser();

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Matière introuvable avec l'id " + request.subjectId()));

        SupportRequest supportRequest = SupportRequest.builder()
                .description(request.description())
                .status(RequestStatus.CREATED)
                .student(student)
                .subject(subject)
                .teacher(null)               // pas encore d'enseignant
                .build();

        return SupportRequestResponse.fromEntity(requestRepository.save(supportRequest));
    }

    /**
     * Un ENSEIGNANT (ou admin) consulte les demandes DISPONIBLES
     * (celles au statut CREATED, sans enseignant).
     */
    @Transactional(readOnly = true)
    public List<SupportRequestResponse> getAvailableRequests() {
        return requestRepository.findByStatus(RequestStatus.CREATED)
                .stream()
                .map(SupportRequestResponse::fromEntity)
                .toList();
    }

    /**
     * "Mes demandes" : dépend du rôle de l'utilisateur connecté.
     *  - élève     -> les demandes qu'il a créées
     *  - enseignant -> les demandes qui lui sont affectées
     *  - admin     -> toutes les demandes
     */
    @Transactional(readOnly = true)
    public List<SupportRequestResponse> getMyRequests() {
        User current = userService.getCurrentUser();

        List<SupportRequest> result = switch (current.getRole()) {
            case STUDENT -> requestRepository.findByStudent(current);
            case TEACHER -> requestRepository.findByTeacher(current);
            case ADMIN   -> requestRepository.findAll();
        };

        return result.stream().map(SupportRequestResponse::fromEntity).toList();
    }

    /**
     * Détail d'une demande, avec contrôle d'accès :
     * seuls l'élève propriétaire, l'enseignant affecté ou un admin peuvent la voir.
     */
    @Transactional(readOnly = true)
    public SupportRequestResponse getById(Long id) {
        SupportRequest request = findRequestOrThrow(id);
        User current = userService.getCurrentUser();
        ensureCanView(request, current);
        return SupportRequestResponse.fromEntity(request);
    }

    /**
     * Un ENSEIGNANT s'affecte à une demande disponible.
     * Règle : la demande doit être au statut CREATED.
     * Effet : teacher = enseignant courant, statut -> IN_PROGRESS.
     */
    @Transactional
    public SupportRequestResponse assignToCurrentTeacher(Long id) {
        SupportRequest request = findRequestOrThrow(id);
        User teacher = userService.getCurrentUser();

        if (request.getStatus() != RequestStatus.CREATED) {
            throw new BusinessException(
                    "Cette demande n'est plus disponible (statut actuel : "
                            + request.getStatus() + ")");
        }

        request.setTeacher(teacher);
        request.setStatus(RequestStatus.IN_PROGRESS);
        return SupportRequestResponse.fromEntity(request);
    }

    /**
     * Terminer une demande.
     * Règle métier validée : seul l'ÉLÈVE propriétaire ou un ADMIN peut le faire
     * (l'enseignant ne peut pas s'auto-valider).
     * La demande doit être IN_PROGRESS.
     */
    @Transactional
    public SupportRequestResponse complete(Long id) {
        SupportRequest request = findRequestOrThrow(id);
        User current = userService.getCurrentUser();

        boolean isOwner = request.getStudent().getId().equals(current.getId());
        boolean isAdmin = current.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException(
                    "Seul l'élève propriétaire ou un admin peut terminer la demande");
        }

        if (request.getStatus() != RequestStatus.IN_PROGRESS) {
            throw new BusinessException(
                    "Seule une demande EN COURS peut être terminée (statut actuel : "
                            + request.getStatus() + ")");
        }

        request.setStatus(RequestStatus.COMPLETED);
        return SupportRequestResponse.fromEntity(request);
    }

    /**
     * Annuler une demande.
     * Autorisé : l'élève propriétaire ou un admin.
     * Possible tant que la demande n'est pas déjà terminée ou annulée.
     */
    @Transactional
    public SupportRequestResponse cancel(Long id) {
        SupportRequest request = findRequestOrThrow(id);
        User current = userService.getCurrentUser();

        boolean isOwner = request.getStudent().getId().equals(current.getId());
        boolean isAdmin = current.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException(
                    "Seul l'élève propriétaire ou un admin peut annuler la demande");
        }

        if (request.getStatus() == RequestStatus.COMPLETED
                || request.getStatus() == RequestStatus.CANCELLED) {
            throw new BusinessException(
                    "Impossible d'annuler une demande déjà " + request.getStatus());
        }

        request.setStatus(RequestStatus.CANCELLED);
        return SupportRequestResponse.fromEntity(request);
    }

    // ----------------- méthodes internes -----------------

    private SupportRequest findRequestOrThrow(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Demande introuvable avec l'id " + id));
    }

    /**
     * Vérifie que l'utilisateur a le droit de VOIR cette demande.
     * (propriétaire, enseignant affecté, ou admin)
     */
    private void ensureCanView(SupportRequest request, User user) {
        if (user.getRole() == Role.ADMIN) return;

        boolean isOwner = request.getStudent().getId().equals(user.getId());
        boolean isAssignedTeacher = request.getTeacher() != null
                && request.getTeacher().getId().equals(user.getId());

        if (!isOwner && !isAssignedTeacher) {
            throw new AccessDeniedException(
                    "Vous n'avez pas accès à cette demande");
        }
    }
}
