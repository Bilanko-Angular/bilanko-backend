package com.backend.bilanko.DTO.object.document;

/**
 * Infos commerçant préremplies depuis le profil User (modifiables à la génération).
 */
public record InfosCommercantDTO(
        String raisonSociale,
        String activite,
        String adresse,
        String niu,
        String dateCreationActivite
) {}
