package com.backend.bilanko.mapper;

import com.backend.bilanko.DTO.object.document.*;
import com.backend.bilanko.DTO.object.document.admin.AdminDocumentResponseDTO;
import com.backend.bilanko.models.object.document.*;
import com.backend.bilanko.models.person.user.User;

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

    public static InfosCommercantDTO toCommercantDto(User user) {
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

    public static AdminDocumentResponseDTO mapDocumentToAdminDocumentDTO(Document document) {
        AdminDocumentResponseDTO.AdminDocumentResponseDTOBuilder builder = AdminDocumentResponseDTO.builder()
                .id(document.getId())
                .nom(document.getNom())
                .type(document.getType())
                .frontCode(document.getType().getFrontCode())
                .objet(document.getObjet())
                .dateDeGeneration(document.getDateDeGeneration())
                .userId(document.getUser().getId())
                .userName(document.getUser().getName())
                .userSubname(document.getUser().getSubname());

        if (document instanceof AdministrativeDocument admin) {
            builder.raisonSociale(admin.getRaisonSociale());
        }

        if (document instanceof DocumentPret pret) {
            builder.pret(AdminDocumentResponseDTO.PretDetails.builder()
                    .capitalPropre(pret.getCapitalPropre())
                    .banque(pret.getBanque())
                    .agence(pret.getAgence())
                    .montantDemande(pret.getMontantDemande())
                    .dureeMois(pret.getDureeMois())
                    .garanties(pret.getGaranties())
                    .build());
        }

        if (document instanceof DocumentFiscaux fiscal) {
            builder.fiscal(AdminDocumentResponseDTO.FiscalDetails.builder()
                    .regimeFiscal(fiscal.getRegimeFiscal())
                    .regimeFiscalFrontCode(fiscal.getRegimeFiscal().getFrontCode())
                    .exerciceFiscal(fiscal.getExerciceFiscal())
                    .centreImpots(fiscal.getCentreImpots())
                    .natureImpot(fiscal.getNatureImpot())
                    .debutPeriodeDeclaration(fiscal.getDebutPeriodeDeclaration())
                    .finPeriodeDeclaration(fiscal.getFinPeriodeDeclaration())
                    .montantImpot(fiscal.getMontantImpot())
                    .datePaiement(fiscal.getDatePaiement())
                    .moyenPaiement(fiscal.getMoyenPaiement())
                    .referencePaiement(fiscal.getReferencePaiement())
                    .chiffreAffairesPeriode(fiscal.getChiffreAffairesPeriode())
                    .build());
        }

        return builder.build();
    }
    private static String blankToEmpty(String value) {
        return value == null ? "" : value;
    }

}
