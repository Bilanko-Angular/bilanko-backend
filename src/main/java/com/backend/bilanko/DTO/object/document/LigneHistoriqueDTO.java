package com.backend.bilanko.DTO.object.document;

public record LigneHistoriqueDTO(
        String cle,
        String mois,
        double chiffreAffaires,
        double achatsCharges
) {}
