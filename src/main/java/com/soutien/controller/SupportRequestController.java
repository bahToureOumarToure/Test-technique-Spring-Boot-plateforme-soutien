package com.soutien.controller;

import com.soutien.dto.SupportRequestCreateRequest;
import com.soutien.dto.SupportRequestResponse;
import com.soutien.service.SupportRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller des demandes de soutien.
 *
 * Deux niveaux de sécurité se combinent :
 *  - @PreAuthorize ici : filtre par RÔLE (ex: seul un élève crée une demande)
 *  - le service : vérifie la PROPRIÉTÉ (ex: seul le propriétaire annule SA demande)
 */
@RestController
@RequestMapping("/api/requests")
public class SupportRequestController {

    private final SupportRequestService requestService;

    public SupportRequestController(SupportRequestService requestService) {
        this.requestService = requestService;
    }

    /**
     * POST /api/requests -> un élève crée une demande.
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SupportRequestResponse> create(
            @Valid @RequestBody SupportRequestCreateRequest request) {
        SupportRequestResponse created = requestService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/requests/available -> demandes disponibles (statut CREATED).
     * Réservé aux enseignants et admins.
     */
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public List<SupportRequestResponse> getAvailable() {
        return requestService.getAvailableRequests();
    }

    /**
     * GET /api/requests/mine -> mes demandes (selon mon rôle).
     * Accessible à tout utilisateur connecté.
     */
    @GetMapping("/mine")
    public List<SupportRequestResponse> getMine() {
        return requestService.getMyRequests();
    }

    /**
     * GET /api/requests/{id} -> détail d'une demande.
     * Le contrôle de propriété est fait dans le service.
     */
    @GetMapping("/{id}")
    public SupportRequestResponse getById(@PathVariable Long id) {
        return requestService.getById(id);
    }

    /**
     * POST /api/requests/{id}/assign -> un enseignant s'affecte à la demande.
     */
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('TEACHER')")
    public SupportRequestResponse assign(@PathVariable Long id) {
        return requestService.assignToCurrentTeacher(id);
    }

    /**
     * PATCH /api/requests/{id}/complete -> terminer la demande.
     * Rôle : élève ou admin (la propriété est vérifiée dans le service).
     */
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public SupportRequestResponse complete(@PathVariable Long id) {
        return requestService.complete(id);
    }

    /**
     * PATCH /api/requests/{id}/cancel -> annuler la demande.
     * Rôle : élève ou admin (la propriété est vérifiée dans le service).
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public SupportRequestResponse cancel(@PathVariable Long id) {
        return requestService.cancel(id);
    }
}
