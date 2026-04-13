package com.portfolio.domain.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Projede kullanılan malzeme.
 * Örn: "Mermer", "Ahşap", "Cam", "Pirinç"
 *
 * Basit value object — sadece proje ile birlikte anlam taşır.
 */
@Entity
@Table(name = "project_materials")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 100)
    private String name;

    public static ProjectMaterial of(Project project, String name) {
        ProjectMaterial material = new ProjectMaterial();
        material.project = project;
        material.name = name;
        return material;
    }
}
