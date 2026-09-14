package com.backend.bilanko.DTO.document;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Création d'un document fiscal. Periode = debut + fin (plus une seule chaîne).
 */
public record CreateDocumentFiscalRequest(
        String nom,
        @Valid @NotNull InfosCommercantPayload commercant,
        boolean updateProfil,
        @NotBlank String regimeFiscal,
        @NotBlank String exerciceFiscal,
        @NotBlank String centreImpots,
        @NotBlank String natureImpot,
        @NotBlank String debutPeriodeDeclaration,
        @NotBlank String finPeriodeDeclaration,
        @NotBlank String montantImpot,
        String datePaiement,
        String moyenPaiement,
        String referencePaiement,
        Double chiffreAffairesPeriode,
        Integer dureeHistorique
) {
    public record InfosCommercantPayload(
            @NotBlank String raisonSociale,
            @NotBlank String activite,
            @NotBlank String adresse,
            @NotBlank String niu,
            @NotBlank String dateCreationActivite
    ) {}
}
