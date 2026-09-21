package com.backend.bilanko.DTO.document;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Création d'un document de prêt. Les infos commerçant peuvent mettre à jour le profil.
 */
public record CreateDocumentPretRequest(
        String nom,
        @Valid @NotNull InfosCommercantPayload commercant,
        boolean updateProfil,
        @NotBlank String objetPretSlug,
        @NotBlank String banque,
        @NotBlank String agence,
        Integer capitalPropre,
        @NotNull @Min(1) Integer montantDemande,
        @NotNull @Min(1) Integer dureeMois,
        String garanties,
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
