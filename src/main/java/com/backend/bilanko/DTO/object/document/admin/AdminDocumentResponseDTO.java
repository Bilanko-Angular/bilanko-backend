package com.backend.bilanko.DTO.object.document.admin;

import com.backend.bilanko.models.object.document.RegimeFiscal;
import com.backend.bilanko.models.object.document.TypeDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDocumentResponseDTO {
    private long id;
    private String nom;
    private TypeDocument type;
    private String frontCode;
    private String objet;
    private Instant dateDeGeneration;
    private String raisonSociale;

    private Long userId;
    private String userName;
    private String userSubname;

    private PretDetails pret;
    private FiscalDetails fiscal;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PretDetails {
        private Integer capitalPropre;
        private String banque;
        private String agence;
        private Integer montantDemande;
        private Integer dureeMois;
        private String garanties;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FiscalDetails {
        private RegimeFiscal regimeFiscal;
        private String regimeFiscalFrontCode;
        private String exerciceFiscal;
        private String centreImpots;
        private String natureImpot;
        private String debutPeriodeDeclaration;
        private String finPeriodeDeclaration;
        private String montantImpot;
        private String datePaiement;
        private String moyenPaiement;
        private String referencePaiement;
        private Double chiffreAffairesPeriode;
    }
}
