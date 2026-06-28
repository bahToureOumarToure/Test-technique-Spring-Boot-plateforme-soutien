package com.soutien.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Une matière scolaire (Mathématiques, Français, Physique...).
 */
@Entity
@Table(name = "subjects")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)   // pas deux fois la même matière
    private String name;

    @Column(length = 500)
    private String description;
}
