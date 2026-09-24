package com.backend.bilanko.models.object.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "document_prets")
@Getter
@Setter
public class DocumentPret extends AdministrativeDocument {

    @Column(name = "capital_propre")
    private Integer capitalPropre;

    @Column(nullable = false)
    private String banque;

    @Column(nullable = false)
    private String agence;

    @Column(name = "montant_demande", nullable = false)
    private Integer montantDemande;

    @Column(name = "duree_mois", nullable = false)
    private Integer dureeMois;

    @Column(columnDefinition = "TEXT")
    private String garanties;
}
