package com.soutien.controller;

import com.soutien.dto.SubjectRequest;
import com.soutien.dto.SubjectResponse;
import com.soutien.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST des matières.
 *
 * @RestController : chaque méthode renvoie directement du JSON (pas une page web).
 * @RequestMapping : toutes les URLs ici commencent par /api/subjects.
 *
 * Le controller ne fait QUE :
 *   - lire la requête HTTP
 *   - déclencher la validation (@Valid)
 *   - appeler le service
 *   - renvoyer la bonne réponse HTTP
 * Aucune logique métier ici.
 */
@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    /**
     * POST /api/subjects -> crée une matière.
     * @Valid : déclenche la validation du SubjectRequest (@NotBlank, etc.)
     * @RequestBody : Spring convertit le JSON reçu en objet SubjectRequest.
     * 201 Created : code HTTP standard quand une ressource est créée.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectResponse> create(@Valid @RequestBody SubjectRequest request) {
        SubjectResponse created = subjectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/subjects -> liste toutes les matières.
     */
    @GetMapping
    public List<SubjectResponse> getAll() {
        return subjectService.getAll();
    }

    /**
     * GET /api/subjects/{id} -> une matière précise.
     * @PathVariable : récupère le {id} dans l'URL.
     */
    @GetMapping("/{id}")
    public SubjectResponse getById(@PathVariable Long id) {
        return subjectService.getById(id);
    }

    /**
     * PUT /api/subjects/{id} -> met à jour une matière.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SubjectResponse update(@PathVariable Long id,
                                  @Valid @RequestBody SubjectRequest request) {
        return subjectService.update(id, request);
    }

    /**
     * DELETE /api/subjects/{id} -> supprime une matière.
     * 204 No Content : succès, rien à renvoyer dans le corps.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subjectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
