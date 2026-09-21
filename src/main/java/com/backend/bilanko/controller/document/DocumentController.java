package com.backend.bilanko.controller.document;

import com.backend.bilanko.DTO.document.*;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.services.document.DocumentService;
import com.backend.bilanko.utils.routes.DocumentApiRoutes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(DocumentApiRoutes.BASE)
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Infos minimales pour démarrer le wizard documents côté front
     * (catalogues, profil commerçant, stock, historique CA).
     */
    @GetMapping(DocumentApiRoutes.BOOTSTRAP)
    public ResponseEntity<DocumentBootstrapDTO> bootstrap(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "6") int dureeHistorique
    ) {
        return ResponseEntity.ok(documentService.getBootstrap(currentUser, dureeHistorique));
    }

    @GetMapping(DocumentApiRoutes.HISTORIQUE)
    public ResponseEntity<List<LigneHistoriqueDTO>> historique(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "6") int dureeHistorique
    ) {
        return ResponseEntity.ok(documentService.getHistorique(currentUser, dureeHistorique));
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> listMine(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(documentService.listMine(currentUser));
    }

    @GetMapping(DocumentApiRoutes.BY_ID)
    public ResponseEntity<DocumentResponseDTO> getById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable long id
    ) {
        return ResponseEntity.ok(documentService.getById(currentUser, id));
    }

    @PostMapping(DocumentApiRoutes.PRET)
    public ResponseEntity<DocumentResponseDTO> createPret(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateDocumentPretRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.createPret(currentUser, request));
    }

    @PostMapping(DocumentApiRoutes.FISCAL)
    public ResponseEntity<DocumentResponseDTO> createFiscal(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateDocumentFiscalRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.createFiscal(currentUser, request));
    }
}
