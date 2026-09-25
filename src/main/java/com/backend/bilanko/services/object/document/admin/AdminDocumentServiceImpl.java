package com.backend.bilanko.services.object.document.admin;

import com.backend.bilanko.DTO.object.document.admin.AdminDocumentCreateRequest;
import com.backend.bilanko.DTO.object.document.admin.AdminDocumentResponseDTO;
import com.backend.bilanko.DTO.object.document.admin.AdminDocumentSummaryDTO;
import com.backend.bilanko.DTO.object.document.admin.AdminDocumentUpdateRequest;
import com.backend.bilanko.models.object.document.*;
import com.backend.bilanko.models.person.user.Role;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.repository.object.document.DocumentRepository;
import com.backend.bilanko.repository.object.document.InfoCleRepository;
import com.backend.bilanko.repository.person.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AdminDocumentServiceImpl implements AdminDocumentService {

    private final DocumentRepository documentRepository;
    private final InfoCleRepository infoCleRepository;
    private final UserRepository userRepository;

    private void verifyAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrateur introuvable"));
        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Accès refusé. Vous n'êtes pas administrateur.");
        }
    }

    private AdminDocumentResponseDTO mapToDTO(Document document) {
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

    private TypeDocument resolveType(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String value = raw.trim();
        try {
            return TypeDocument.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            // fall through to front code
        }
        try {
            return TypeDocument.fromFrontCode(value);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type de document inconnu : " + value);
        }
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void attachMatchingInfoCle(Document document, TypeInfoCle type, String nom) {
        if (nom == null || nom.isBlank()) {
            return;
        }
        infoCleRepository.findByTypeOrderByNomAsc(type).stream()
                .filter(ic -> ic.getNom().equalsIgnoreCase(nom.trim()))
                .findFirst()
                .ifPresent(document.getInfoCles()::add);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDocumentSummaryDTO getSummary(String adminEmail) {
        verifyAdmin(adminEmail);

        return AdminDocumentSummaryDTO.builder()
                .totalCount(documentRepository.count())
                .pretCount(documentRepository.countByType(TypeDocument.DOCUMENT_PRET))
                .fiscalCount(documentRepository.countByType(TypeDocument.DOCUMENT_FISCAL))
                .usersWithDocumentsCount(documentRepository.countDistinctUsers())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminDocumentResponseDTO> getPagedDocuments(String adminEmail, int page, int size) {
        verifyAdmin(adminEmail);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateDeGeneration"));
        return documentRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminDocumentResponseDTO> searchDocuments(
            String adminEmail, String keyword, String type, int page, int size) {
        verifyAdmin(adminEmail);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateDeGeneration"));
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        TypeDocument typeFilter = (type != null && !type.isBlank()) ? resolveType(type) : null;
        return documentRepository.adminSearchDocuments(kw, typeFilter, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDocumentResponseDTO getById(String adminEmail, Long documentId) {
        verifyAdmin(adminEmail);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document introuvable : id=" + documentId));
        return mapToDTO(document);
    }

    @Override
    @Transactional
    public AdminDocumentResponseDTO createDocument(String adminEmail, AdminDocumentCreateRequest request) {
        verifyAdmin(adminEmail);

        TypeDocument type = resolveType(request.getType());
        if (type != TypeDocument.DOCUMENT_PRET && type != TypeDocument.DOCUMENT_FISCAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Seuls les types DOCUMENT_PRET et DOCUMENT_FISCAL sont créables côté admin.");
        }

        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Utilisateur introuvable : id=" + request.getUserId()));

        String raison = request.getRaisonSociale().trim();
        Document saved;

        if (type == TypeDocument.DOCUMENT_PRET) {
            if (request.getPret() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Les détails prêt (pret) sont obligatoires pour un document de prêt.");
            }
            saved = documentRepository.save(buildPret(targetUser, raison, request.getNom(), request.getPret()));
        } else {
            if (request.getFiscal() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Les détails fiscaux (fiscal) sont obligatoires pour un document fiscal.");
            }
            saved = documentRepository.save(buildFiscal(targetUser, raison, request.getNom(), request.getFiscal()));
        }

        return mapToDTO(saved);
    }

    private DocumentPret buildPret(
            User user, String raison, String nom, AdminDocumentCreateRequest.PretPayload payload) {
        InfoCle objetPret = infoCleRepository.findBySlug(payload.getObjetPretSlug())
                .filter(ic -> ic.getType() == TypeInfoCle.OBJET_PRET)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Objet de prêt inconnu : " + payload.getObjetPretSlug()));

        String documentNom = (nom == null || nom.isBlank())
                ? "Demande de prêt — " + raison
                : nom.trim();

        DocumentPret pret = new DocumentPret();
        pret.setNom(documentNom);
        pret.setType(TypeDocument.DOCUMENT_PRET);
        pret.setObjet(objetPret.getNom());
        pret.setDateDeGeneration(Instant.now());
        pret.setUser(user);
        pret.setRaisonSociale(raison);
        pret.setCapitalPropre(payload.getCapitalPropre() != null ? payload.getCapitalPropre() : 0);
        pret.setBanque(payload.getBanque().trim());
        pret.setAgence(payload.getAgence().trim());
        pret.setMontantDemande(payload.getMontantDemande());
        pret.setDureeMois(payload.getDureeMois());
        pret.setGaranties(payload.getGaranties() != null ? payload.getGaranties().trim() : "");

        pret.getInfoCles().add(objetPret);
        pret.getInfoCles().addAll(infoCleRepository.findByTypeOrderByNomAsc(TypeInfoCle.PIECE_A_JOINDRE_PRET));
        return pret;
    }

    private RegimeFiscal parseRegime(String raw) {
        try {
            return RegimeFiscal.fromFrontCode(raw);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private DocumentFiscaux buildFiscal(
            User user, String raison, String nom, AdminDocumentCreateRequest.FiscalPayload payload) {
        RegimeFiscal regime = parseRegime(payload.getRegimeFiscal());

        String documentNom = (nom == null || nom.isBlank())
                ? "Dossier fiscal — " + raison
                : nom.trim();

        DocumentFiscaux fiscal = new DocumentFiscaux();
        fiscal.setNom(documentNom);
        fiscal.setType(TypeDocument.DOCUMENT_FISCAL);
        fiscal.setObjet(regime.getLabel());
        fiscal.setDateDeGeneration(Instant.now());
        fiscal.setUser(user);
        fiscal.setRaisonSociale(raison);
        fiscal.setRegimeFiscal(regime);
        fiscal.setExerciceFiscal(payload.getExerciceFiscal().trim());
        fiscal.setCentreImpots(payload.getCentreImpots().trim());
        fiscal.setNatureImpot(payload.getNatureImpot().trim());
        fiscal.setDebutPeriodeDeclaration(payload.getDebutPeriodeDeclaration().trim());
        fiscal.setFinPeriodeDeclaration(payload.getFinPeriodeDeclaration().trim());
        fiscal.setMontantImpot(payload.getMontantImpot().trim());
        fiscal.setDatePaiement(blankToNull(payload.getDatePaiement()));
        fiscal.setMoyenPaiement(blankToNull(payload.getMoyenPaiement()));
        fiscal.setReferencePaiement(blankToNull(payload.getReferencePaiement()));
        fiscal.setChiffreAffairesPeriode(payload.getChiffreAffairesPeriode());

        fiscal.getInfoCles().addAll(infoCleRepository.findByTypeOrderByNomAsc(TypeInfoCle.PIECE_A_JOINDRE_FISCAL));
        attachMatchingInfoCle(fiscal, TypeInfoCle.CENTRE_IMPOTS, payload.getCentreImpots());
        attachMatchingInfoCle(fiscal, TypeInfoCle.NATURE_IMPOT, payload.getNatureImpot());
        return fiscal;
    }

    @Override
    @Transactional
    public AdminDocumentResponseDTO updateDocument(
            String adminEmail, Long documentId, AdminDocumentUpdateRequest request) {
        verifyAdmin(adminEmail);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document introuvable : id=" + documentId));

        if (request.getNom() != null && !request.getNom().isBlank()) {
            document.setNom(request.getNom().trim());
        }

        if (document instanceof DocumentPret pret) {
            if (request.getPret() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Les détails prêt (pret) sont obligatoires pour mettre à jour ce document.");
            }
            applyPretUpdate(pret, request.getRaisonSociale(), request.getPret());
        } else if (document instanceof DocumentFiscaux fiscal) {
            if (request.getFiscal() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Les détails fiscaux (fiscal) sont obligatoires pour mettre à jour ce document.");
            }
            applyFiscalUpdate(fiscal, request.getRaisonSociale(), request.getFiscal());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ce type de document ne peut pas être modifié côté admin.");
        }

        return mapToDTO(documentRepository.save(document));
    }

    private void applyPretUpdate(
            DocumentPret pret, String raisonSociale, AdminDocumentUpdateRequest.PretPayload payload) {
        pret.setRaisonSociale(raisonSociale.trim());
        pret.setBanque(payload.getBanque().trim());
        pret.setAgence(payload.getAgence().trim());
        pret.setCapitalPropre(payload.getCapitalPropre() != null ? payload.getCapitalPropre() : pret.getCapitalPropre());
        if (payload.getMontantDemande() != null) {
            pret.setMontantDemande(payload.getMontantDemande());
        }
        if (payload.getDureeMois() != null) {
            pret.setDureeMois(payload.getDureeMois());
        }
        if (payload.getGaranties() != null) {
            pret.setGaranties(payload.getGaranties().trim());
        }

        if (payload.getObjetPretSlug() != null && !payload.getObjetPretSlug().isBlank()) {
            InfoCle objetPret = infoCleRepository.findBySlug(payload.getObjetPretSlug())
                    .filter(ic -> ic.getType() == TypeInfoCle.OBJET_PRET)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Objet de prêt inconnu : " + payload.getObjetPretSlug()));
            pret.setObjet(objetPret.getNom());
            pret.getInfoCles().removeIf(ic -> ic.getType() == TypeInfoCle.OBJET_PRET);
            pret.getInfoCles().add(objetPret);
        }
    }

    private void applyFiscalUpdate(
            DocumentFiscaux fiscal, String raisonSociale, AdminDocumentUpdateRequest.FiscalPayload payload) {
        RegimeFiscal regime = parseRegime(payload.getRegimeFiscal());

        fiscal.setRaisonSociale(raisonSociale.trim());
        fiscal.setRegimeFiscal(regime);
        fiscal.setObjet(regime.getLabel());
        fiscal.setExerciceFiscal(payload.getExerciceFiscal().trim());
        fiscal.setCentreImpots(payload.getCentreImpots().trim());
        fiscal.setNatureImpot(payload.getNatureImpot().trim());
        fiscal.setDebutPeriodeDeclaration(payload.getDebutPeriodeDeclaration().trim());
        fiscal.setFinPeriodeDeclaration(payload.getFinPeriodeDeclaration().trim());
        fiscal.setMontantImpot(payload.getMontantImpot().trim());
        fiscal.setDatePaiement(blankToNull(payload.getDatePaiement()));
        fiscal.setMoyenPaiement(blankToNull(payload.getMoyenPaiement()));
        fiscal.setReferencePaiement(blankToNull(payload.getReferencePaiement()));
        fiscal.setChiffreAffairesPeriode(payload.getChiffreAffairesPeriode());

        fiscal.getInfoCles().removeIf(ic ->
                ic.getType() == TypeInfoCle.CENTRE_IMPOTS || ic.getType() == TypeInfoCle.NATURE_IMPOT);
        attachMatchingInfoCle(fiscal, TypeInfoCle.CENTRE_IMPOTS, payload.getCentreImpots());
        attachMatchingInfoCle(fiscal, TypeInfoCle.NATURE_IMPOT, payload.getNatureImpot());
    }

    @Override
    @Transactional
    public void deleteDocument(String adminEmail, Long documentId) {
        verifyAdmin(adminEmail);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document introuvable : id=" + documentId));

        documentRepository.delete(document);
    }
}
