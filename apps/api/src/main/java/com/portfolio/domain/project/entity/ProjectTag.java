package com.portfolio.domain.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Projeye ait etiket.
 * Örn: "Ödüllü", "Sürdürülebilir", "2024 Trendi"
 *
 * Basit value object — sadece proje ile birlikte anlam taşır.
 */
@Entity
@Table(name = "project_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 100)
    private String name;

    public static ProjectTag of(Project project, String name) {
        ProjectTag tag = new ProjectTag();
        tag.project = project;
        tag.name = name;
        return tag;
    }
}
