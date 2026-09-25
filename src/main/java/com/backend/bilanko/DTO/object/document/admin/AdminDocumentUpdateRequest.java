package com.backend.bilanko.DTO.object.document.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDocumentUpdateRequest {

    private String nom;

    @NotBlank(message = "La raison sociale est obligatoire")
    private String raisonSociale;

    @Valid
    private PretPayload pret;

    @Valid
    private FiscalPayload fiscal;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PretPayload {
        private String objetPretSlug;

        @NotBlank(message = "La banque est obligatoire")
        private String banque;

        @NotBlank(message = "L'agence est obligatoire")
        private String agence;

        private Integer capitalPropre;

        @Min(value = 1, message = "Le montant demandé doit être positif")
        private Integer montantDemande;

        @Min(value = 1, message = "La durée doit être positive")
        private Integer dureeMois;

        private String garanties;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FiscalPayload {
        @NotBlank(message = "Le régime fiscal est obligatoire")
        private String regimeFiscal;

        @NotBlank(message = "L'exercice fiscal est obligatoire")
        private String exerciceFiscal;

        @NotBlank(message = "Le centre des impôts est obligatoire")
        private String centreImpots;

        @NotBlank(message = "La nature d'impôt est obligatoire")
        private String natureImpot;

        @NotBlank(message = "Le début de période est obligatoire")
        private String debutPeriodeDeclaration;

        @NotBlank(message = "La fin de période est obligatoire")
        private String finPeriodeDeclaration;

        @NotBlank(message = "Le montant d'impôt est obligatoire")
        private String montantImpot;

        private String datePaiement;
        private String moyenPaiement;
        private String referencePaiement;
        private Double chiffreAffairesPeriode;
    }
}
