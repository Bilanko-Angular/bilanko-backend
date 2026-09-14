package com.backend.bilanko.models.document;

import com.backend.bilanko.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "info_cles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_info_cle_slug", columnNames = "slug")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfoCle extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TypeInfoCle type;

    @Column(nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String information;

    @ManyToMany(mappedBy = "infoCles")
    @Builder.Default
    private Set<Document> documents = new HashSet<>();
}
