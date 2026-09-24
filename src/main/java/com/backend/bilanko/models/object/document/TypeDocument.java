package com.backend.bilanko.models.object.document;

/**
 * Types de documents générables. Les codes front (pret_bancaire, dsf_smt, …)
 * sont mappés via {@link #getFrontCode()}.
 */
public enum TypeDocument {
    DOCUMENT_PRET("pret_bancaire", "Demande de prêt bancaire"),
    DOCUMENT_FISCAL("dsf_smt", "Dossier fiscal (DSF / SMT)"),
    FACTURE("facture", "Facture"),
    LISTE_PRODUIT("liste_produit", "Liste de produits"),
    LISTE_CHARGE("liste_charge", "Liste de charges"),
    LISTE_VENTE("liste_vente", "Liste de ventes");

    private final String frontCode;
    private final String label;

    TypeDocument(String frontCode, String label) {
        this.frontCode = frontCode;
        this.label = label;
    }

    public String getFrontCode() {
        return frontCode;
    }

    public String getLabel() {
        return label;
    }

    public static TypeDocument fromFrontCode(String frontCode) {
        if (frontCode == null || frontCode.isBlank()) {
            throw new IllegalArgumentException("Code front de type document manquant");
        }
        for (TypeDocument type : values()) {
            if (type.frontCode.equalsIgnoreCase(frontCode.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Type de document inconnu : " + frontCode);
    }
}
