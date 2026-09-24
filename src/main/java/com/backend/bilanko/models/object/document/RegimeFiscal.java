package com.backend.bilanko.models.object.document;

import lombok.Getter;

@Getter
public enum RegimeFiscal {
    CONTRIBUTION_LIBERATOIRE(
            "contribution_liberatoire",
            "Contribution Libératoire (CA < 10M FCFA/an)"
    ),
    REEL_SIMPLIFIE(
            "reel_simplifie",
            "Régime Simplifié — Système Minimal de Trésorerie (SMT)"
    ),
    REEL(
            "reel",
            "Régime du Réel"
    );

    private final String frontCode;
    private final String label;

    RegimeFiscal(String frontCode, String label) {
        this.frontCode = frontCode;
        this.label = label;
    }

    public static RegimeFiscal fromFrontCode(String frontCode) {
        if (frontCode == null || frontCode.isBlank()) {
            throw new IllegalArgumentException("Code front de régime fiscal manquant");
        }
        for (RegimeFiscal regime : values()) {
            if (regime.frontCode.equalsIgnoreCase(frontCode.trim())
                    || regime.name().equalsIgnoreCase(frontCode.trim())) {
                return regime;
            }
        }
        throw new IllegalArgumentException("Régime fiscal inconnu : " + frontCode);
    }
}
