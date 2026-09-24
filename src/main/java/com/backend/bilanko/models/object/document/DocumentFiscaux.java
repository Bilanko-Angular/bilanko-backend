package com.backend.bilanko.models.object.document;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "document_fiscaux")
@Getter
@Setter
public class DocumentFiscaux extends AdministrativeDocument {

    @Enumerated(EnumType.STRING)
    @Column(name = "regime_fiscal", nullable = false, length = 40)
    private RegimeFiscal regimeFiscal;

    @Column(name = "exercice_fiscal", nullable = false)
    private String exerciceFiscal;

    @Column(name = "centre_impots", nullable = false)
    private String centreImpots;

    @Column(name = "nature_impot", nullable = false)
    private String natureImpot;

    @Column(name = "debut_periode_declaration", nullable = false)
    private String debutPeriodeDeclaration;

    @Column(name = "fin_periode_declaration", nullable = false)
    private String finPeriodeDeclaration;

    @Column(name = "montant_impot", nullable = false)
    private String montantImpot;

    @Column(name = "reference_paiement")
    private String referencePaiement;

    @Column(name = "date_paiement")
    private String datePaiement;

    @Column(name = "moyen_paiement")
    private String moyenPaiement;

    @Column(name = "chiffre_affaires_periode")
    private Double chiffreAffairesPeriode;
}
