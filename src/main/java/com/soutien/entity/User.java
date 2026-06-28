package com.soutien.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Un utilisateur de la plateforme (élève, enseignant OU admin).
 * Une seule table pour les 3 rôles, différenciés par le champ 'role'.
 */
@Entity                          // dit à JPA : "cette classe = une table en base"
@Table(name = "users")           // nom de la table (on évite "user", mot réservé SQL)
@Getter @Setter                  // Lombok génère les getters/setters automatiquement
@NoArgsConstructor               // constructeur vide (exigé par JPA)
@AllArgsConstructor              // constructeur avec tous les champs
@Builder                         // permet d'écrire User.builder().email(...).build()
public class User {

    @Id                                              // clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-incrément par la BDD
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)   // pas deux comptes avec le même email
    private String email;

    @Column(nullable = false)
    private String password;                   // sera STOCKÉ HACHÉ (jamais en clair)

    @Enumerated(EnumType.STRING)   // stocke "STUDENT" en base, pas un chiffre (0,1,2)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * @PrePersist : méthode appelée automatiquement par JPA
     * JUSTE AVANT le premier enregistrement en base.
     * On y fixe la date de création.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
