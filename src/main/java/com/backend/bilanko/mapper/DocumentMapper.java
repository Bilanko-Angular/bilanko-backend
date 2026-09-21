package com.backend.bilanko.mapper;

import com.backend.bilanko.DTO.document.*;
import com.backend.bilanko.models.document.*;

import java.util.Comparator;
import java.util.List;

public final class DocumentMapper {

    private DocumentMapper() {}

    public static InfoCleDTO toInfoCleDto(InfoCle infoCle) {
        return new InfoCleDTO(
                infoCle.getId(),
                infoCle.getSlug(),
                infoCle.getType(),
                infoCle.getNom(),
                infoCle.getInformation()
        );
    }

    public static TypeDocumentDTO toTypeDocumentDto(TypeDocument type) {
        return new TypeDocumentDTO(type, type.getFrontCode(), type.getLabel());
    }

    public static RegimeFiscalDTO toRegimeFiscalDto(RegimeFiscal regime) {
        return new RegimeFiscalDTO(regime, regime.getFrontCode(), regime.getLabel());
    }

    public static InfosCommercantDTO toCommercantDto(com.backend.bilanko.models.person.User user) {
        return new InfosCommercantDTO(
                blankToEmpty(user.getCompanyName()),
                user.getActivite(),
                user.getAdresse(),
                user.getNiu(),
                user.getDateDeCreationActivite()
        );
    }

    public static DocumentResponseDTO toResponse(Document document) {
        String raisonSociale = null;
        DocumentResponseDTO.DocumentPretDetails pret = null;
        DocumentResponseDTO.DocumentFiscalDetails fiscal = null;

        if (document instanceof AdministrativeDocument admin) {
            raisonSociale = admin.getRaisonSociale();
        }
        if (document instanceof DocumentPret pretDoc) {
            pret = new DocumentResponseDTO.DocumentPretDetails(
                    pretDoc.getCapitalPropre(),
                    pretDoc.getBanque(),
                    pretDoc.getAgence(),
                    pretDoc.getMontantDemande(),
                    pretDoc.getDureeMois(),
                    pretDoc.getGaranties()
            );
        }
        if (document instanceof DocumentFiscaux fiscalDoc) {
            fiscal = new DocumentResponseDTO.DocumentFiscalDetails(
                    fiscalDoc.getRegimeFiscal(),
                    fiscalDoc.getRegimeFiscal().getFrontCode(),
                    fiscalDoc.getExerciceFiscal(),
                    fiscalDoc.getCentreImpots(),
                    fiscalDoc.getNatureImpot(),
                    fiscalDoc.getDebutPeriodeDeclaration(),
                    fiscalDoc.getFinPeriodeDeclaration(),
                    fiscalDoc.getMontantImpot(),
                    fiscalDoc.getDatePaiement(),
                    fiscalDoc.getMoyenPaiement(),
                    fiscalDoc.getReferencePaiement(),
                    fiscalDoc.getChiffreAffairesPeriode()
            );
        }

        List<InfoCleDTO> infoCles = document.getInfoCles().stream()
                .sorted(Comparator.comparing(InfoCle::getNom, String.CASE_INSENSITIVE_ORDER))
                .map(DocumentMapper::toInfoCleDto)
                .toList();

        return new DocumentResponseDTO(
                document.getId(),
                document.getNom(),
                document.getType(),
                document.getType().getFrontCode(),
                document.getObjet(),
                document.getDateDeGeneration(),
                raisonSociale,
                infoCles,
                pret,
                fiscal
        );
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
