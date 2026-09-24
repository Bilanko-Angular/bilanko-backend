package com.backend.bilanko.models.object.document;

import com.backend.bilanko.models.BaseEntity;
import com.backend.bilanko.models.person.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "documents")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class Document extends BaseEntity {

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TypeDocument type;

    @Column
    private String objet;

    @Column(name = "date_de_generation", nullable = false)
    private Instant dateDeGeneration;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany
    @JoinTable(
            name = "document_info_cles",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "info_cle_id")
    )
    private Set<InfoCle> infoCles = new HashSet<>();
}
