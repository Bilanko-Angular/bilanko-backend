package com.backend.bilanko.DTO.document;

public record LigneHistoriqueDTO(
        String cle,
        String mois,
        double chiffreAffaires,
        double achatsCharges
) {}
