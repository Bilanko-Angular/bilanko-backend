package com.backend.bilanko.config.document;

import com.backend.bilanko.models.object.document.ConstanceMetier;
import com.backend.bilanko.models.object.document.InfoCle;
import com.backend.bilanko.models.object.document.TypeConstanceMetier;
import com.backend.bilanko.models.object.document.TypeInfoCle;
import com.backend.bilanko.repository.document.ConstanceMetierRepository;
import com.backend.bilanko.repository.document.InfoCleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Initialise les catalogues InfoCle / ConstanceMetier (idempotent).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentCatalogSeeder implements ApplicationRunner {

    private final InfoCleRepository infoCleRepository;
    private final ConstanceMetierRepository constanceMetierRepository;

    @Override
    public void run(ApplicationArguments args) {
        seedObjetsPret();
        seedPiecesPret();
        seedPiecesFiscal();
        seedCentresImpots();
        seedNaturesImpot();
        seedConstances();
        log.info("Catalogues documents prêts (InfoCle / ConstanceMetier)");
    }

    private void seedObjetsPret() {
        List<String> objets = List.of(
                "Fonds de roulement",
                "Achat de marchandises / stock",
                "Achat de matériel / équipement",
                "Extension / nouveau point de vente",
                "Rénovation du local",
                "Autre besoin"
        );
        for (String nom : objets) {
            upsertInfoCle(TypeInfoCle.OBJET_PRET, nom, "Objet du prêt bancaire");
        }
    }

    private void seedPiecesPret() {
        List<String> pieces = List.of(
                "Carte Nationale d'Identité (CNI) du commerçant, en cours de validité",
                "Registre de Commerce (RCCM), si vous en disposez",
                "Relevés de compte bancaire des 3 à 6 derniers mois",
                "Devis ou facture pro forma si le prêt finance un achat précis (matériel, marchandises...)",
                "Justificatif de localisation du commerce"
        );
        for (String nom : pieces) {
            upsertInfoCle(TypeInfoCle.PIECE_A_JOINDRE_PRET, nom, "Checklist prêt — affichage écran uniquement");
        }
    }

    private void seedPiecesFiscal() {
        List<String> pieces = List.of(
                "Carte Nationale d'Identité (CNI) du commerçant, en cours de validité",
                "Registre de Commerce (RCCM), si vous en disposez",
                "Justificatif de localisation du commerce",
                "Numéro Contribuable / NIU (déjà indiqué dans ce dossier)"
        );
        for (String nom : pieces) {
            upsertInfoCle(TypeInfoCle.PIECE_A_JOINDRE_FISCAL, nom, "Checklist fiscal — affichage écran uniquement");
        }
    }

    private void seedCentresImpots() {
        List<String> centres = List.of(
                "Centre des Impôts de Yaoundé",
                "Centre des Impôts de Douala",
                "Centre des Impôts de Bafoussam",
                "Centre des Impôts de Garoua",
                "Autre centre des impôts"
        );
        for (String nom : centres) {
            upsertInfoCle(TypeInfoCle.CENTRE_IMPOTS, nom, "Centre des impôts");
        }
    }

    private void seedNaturesImpot() {
        List<String> natures = List.of(
                "Contribution Libératoire",
                "Impôt sur le Revenu (IR)",
                "Taxe sur la Valeur Ajoutée (TVA)",
                "Patente",
                "Autre nature d'impôt"
        );
        for (String nom : natures) {
            upsertInfoCle(TypeInfoCle.NATURE_IMPOT, nom, "Nature de l'impôt");
        }
    }

    private void seedConstances() {
        if (!constanceMetierRepository.existsByType(TypeConstanceMetier.MONTANT_IMPOT_DEFAUT)) {
            constanceMetierRepository.save(ConstanceMetier.builder()
                    .type(TypeConstanceMetier.MONTANT_IMPOT_DEFAUT)
                    .valeur("0")
                    .build());
        }
    }

    private void upsertInfoCle(TypeInfoCle type, String nom, String information) {
        String slug = slugify(type.name() + "-" + nom);
        if (infoCleRepository.existsBySlug(slug)) {
            return;
        }
        infoCleRepository.save(InfoCle.builder()
                .slug(slug)
                .type(type)
                .nom(nom)
                .information(information)
                .build());
    }

    static String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
