package com.backend.bilanko.models.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "administrative_documents")
@Getter
@Setter
public abstract class AdministrativeDocument extends Document {

    @Column(name = "raison_sociale", nullable = false)
    private String raisonSociale;
}
