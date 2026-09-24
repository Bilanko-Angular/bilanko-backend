package com.backend.bilanko.DTO.object.document;

import com.backend.bilanko.models.object.document.RegimeFiscal;
import com.backend.bilanko.models.object.document.TypeDocument;

import java.time.Instant;
import java.util.List;

public record DocumentResponseDTO(
        long id,
        String nom,
        TypeDocument type,
        String frontCode,
        String objet,
        Instant dateDeGeneration,
        String raisonSociale,
        List<InfoCleDTO> infoCles,
        DocumentPretDetails pret,
        DocumentFiscalDetails fiscal
) {
    public record DocumentPretDetails(
            Integer capitalPropre,
            String banque,
            String agence,
            Integer montantDemande,
            Integer dureeMois,
            String garanties
    ) {}

    public record DocumentFiscalDetails(
            RegimeFiscal regimeFiscal,
            String regimeFiscalFrontCode,
            String exerciceFiscal,
            String centreImpots,
            String natureImpot,
            String debutPeriodeDeclaration,
            String finPeriodeDeclaration,
            String montantImpot,
            String datePaiement,
            String moyenPaiement,
            String referencePaiement,
            Double chiffreAffairesPeriode
    ) {}
}
