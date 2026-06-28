package com.soutien.service;

import com.soutien.dto.SubjectRequest;
import com.soutien.dto.SubjectResponse;
import com.soutien.entity.Subject;
import com.soutien.exception.BusinessException;
import com.soutien.exception.ResourceNotFoundException;
import com.soutien.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Couche métier des matières.
 *
 * @Service : indique à Spring que c'est un composant de service.
 *            Spring en crée une instance unique (un "bean") et l'injecte
 *            là où on en a besoin (dans le controller).
 */
@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    /**
     * Injection de dépendance par constructeur (la bonne pratique).
     * Spring fournit automatiquement le SubjectRepository ici.
     */
    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    /**
     * Crée une nouvelle matière.
     * Règle métier : pas deux matières du même nom.
     */
    @Transactional
    public SubjectResponse create(SubjectRequest request) {
        if (subjectRepository.existsByName(request.name())) {
            throw new BusinessException("Une matière nommée '" + request.name() + "' existe déjà");
        }

        Subject subject = Subject.builder()
                .name(request.name())
                .description(request.description())
                .build();

        Subject saved = subjectRepository.save(subject);
        return SubjectResponse.fromEntity(saved);
    }

    /**
     * Liste toutes les matières.
     * readOnly = true : optimisation, on ne fait que lire.
     */
    @Transactional(readOnly = true)
    public List<SubjectResponse> getAll() {
        return subjectRepository.findAll()
                .stream()
                .map(SubjectResponse::fromEntity)   // convertit chaque entité en DTO
                .toList();
    }

    /**
     * Récupère une matière par son id, ou lève 404 si elle n'existe pas.
     */
    @Transactional(readOnly = true)
    public SubjectResponse getById(Long id) {
        Subject subject = findSubjectOrThrow(id);
        return SubjectResponse.fromEntity(subject);
    }

    /**
     * Met à jour une matière existante.
     */
    @Transactional
    public SubjectResponse update(Long id, SubjectRequest request) {
        Subject subject = findSubjectOrThrow(id);

        // Si on change le nom, vérifier qu'il n'entre pas en collision avec un autre
        if (!subject.getName().equals(request.name())
                && subjectRepository.existsByName(request.name())) {
            throw new BusinessException("Une matière nommée '" + request.name() + "' existe déjà");
        }

        subject.setName(request.name());
        subject.setDescription(request.description());
        // Pas besoin d'appeler save() explicitement : dans une transaction,
        // Hibernate détecte le changement et met à jour automatiquement (dirty checking).
        return SubjectResponse.fromEntity(subject);
    }

    /**
     * Supprime une matière.
     */
    @Transactional
    public void delete(Long id) {
        Subject subject = findSubjectOrThrow(id);
        subjectRepository.delete(subject);
    }

    /**
     * Méthode privée réutilisable : récupère une matière ou lève 404.
     * Évite de répéter la même logique dans getById/update/delete.
     */
    private Subject findSubjectOrThrow(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Matière introuvable avec l'id " + id));
    }
}
