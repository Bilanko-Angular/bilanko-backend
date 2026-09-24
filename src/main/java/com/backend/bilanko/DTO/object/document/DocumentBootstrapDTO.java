package com.backend.bilanko.DTO.object.document;

import java.util.List;
import java.util.Map;

/**
 * Payload minimal pour démarrer le wizard documents côté front.
 */
public record DocumentBootstrapDTO(
        List<TypeDocumentDTO> typesDocument,
        List<InfoCleDTO> objetsPret,
        List<RegimeFiscalDTO> regimesFiscaux,
        List<InfoCleDTO> centresImpots,
        List<InfoCleDTO> naturesImpot,
        Map<String, List<InfoCleDTO>> piecesAJoindre,
        double montantImpotDefaut,
        InfosCommercantDTO commercant,
        double stockDisponible,
        int dureeHistoriqueDefaut,
        List<LigneHistoriqueDTO> historique
) {}
