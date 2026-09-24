package com.backend.bilanko.services.document;

import com.backend.bilanko.DTO.object.document.*;
import com.backend.bilanko.mapper.DocumentMapper;
import com.backend.bilanko.models.document.*;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.models.transaction.Charge;
import com.backend.bilanko.models.transaction.Sale;
import com.backend.bilanko.repository.concept.transaction.ChargeRepository;
import com.backend.bilanko.repository.concept.transaction.SaleRepository;
import com.backend.bilanko.repository.document.ConstanceMetierRepository;
import com.backend.bilanko.repository.document.DocumentRepository;
import com.backend.bilanko.repository.document.InfoCleRepository;
import com.backend.bilanko.repository.UserRepository;
import com.backend.bilanko.services.object.product.ProductServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private static final String[] MOIS_NOMS = {
            "Jan", "Fév", "Mar", "Avr", "Mai", "Juin",
            "Juil", "Août", "Sep", "Oct", "Nov", "Déc"
    };

    private final DocumentRepository documentRepository;
    private final InfoCleRepository infoCleRepository;
    private final ConstanceMetierRepository constanceMetierRepository;
    private final SaleRepository saleRepository;
    private final ChargeRepository chargeRepository;
    private final ProductServices productServices;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public DocumentBootstrapDTO getBootstrap(User currentUser, int dureeHistorique) {
        int duree = normalizeDuree(dureeHistorique);

        List<TypeDocumentDTO> types = Arrays.stream(TypeDocument.values())
                .map(DocumentMapper::toTypeDocumentDto)
                .toList();

        List<RegimeFiscalDTO> regimes = Arrays.stream(RegimeFiscal.values())
                .map(DocumentMapper::toRegimeFiscalDto)
                .toList();

        List<InfoCleDTO> objetsPret = mapInfoCles(TypeInfoCle.OBJET_PRET);
        List<InfoCleDTO> centres = mapInfoCles(TypeInfoCle.CENTRE_IMPOTS);
        List<InfoCleDTO> natures = mapInfoCles(TypeInfoCle.NATURE_IMPOT);

        Map<String, List<InfoCleDTO>> pieces = Map.of(
                TypeDocument.DOCUMENT_PRET.name(), mapInfoCles(TypeInfoCle.PIECE_A_JOINDRE_PRET),
                TypeDocument.DOCUMENT_PRET.getFrontCode(), mapInfoCles(TypeInfoCle.PIECE_A_JOINDRE_PRET),
                TypeDocument.DOCUMENT_FISCAL.name(), mapInfoCles(TypeInfoCle.PIECE_A_JOINDRE_FISCAL),
                TypeDocument.DOCUMENT_FISCAL.getFrontCode(), mapInfoCles(TypeInfoCle.PIECE_A_JOINDRE_FISCAL)
        );

        double montantDefaut = constanceMetierRepository
                .findByType(TypeConstanceMetier.MONTANT_IMPOT_DEFAUT)
                .map(c -> parseDoubleSafe(c.getValeur()))
                .orElse(0d);

        double stock = productServices.getStockOverview(currentUser.getEmail()).stockValueAtPurchase();

        return new DocumentBootstrapDTO(
                types,
                objetsPret,
                regimes,
                centres,
                natures,
                pieces,
                montantDefaut,
                DocumentMapper.toCommercantDto(currentUser),
                stock,
                duree,
                buildHistorique(currentUser, duree)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LigneHistoriqueDTO> getHistorique(User currentUser, int dureeHistorique) {
        return buildHistorique(currentUser, normalizeDuree(dureeHistorique));
    }

    @Override
    @Transactional
    public DocumentResponseDTO createPret(User currentUser, CreateDocumentPretRequest request) {
        User user = applyCommercant(currentUser, request.commercant(), request.updateProfil());

        InfoCle objetPret = infoCleRepository.findBySlug(request.objetPretSlug())
                .filter(ic -> ic.getType() == TypeInfoCle.OBJET_PRET)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Objet de prêt inconnu : " + request.objetPretSlug()));

        String raison = request.commercant().raisonSociale().trim();
        String nom = (request.nom() == null || request.nom().isBlank())
                ? "Demande de prêt — " + raison
                : request.nom().trim();

        DocumentPret pret = new DocumentPret();
        pret.setNom(nom);
        pret.setType(TypeDocument.DOCUMENT_PRET);
        pret.setObjet(objetPret.getNom());
        pret.setDateDeGeneration(Instant.now());
        pret.setUser(user);
        pret.setRaisonSociale(raison);
        pret.setCapitalPropre(request.capitalPropre() != null ? request.capitalPropre() : 0);
        pret.setBanque(request.banque().trim());
        pret.setAgence(request.agence().trim());
        pret.setMontantDemande(request.montantDemande());
        pret.setDureeMois(request.dureeMois());
        pret.setGaranties(request.garanties() != null ? request.garanties().trim() : "");

        pret.getInfoCles().add(objetPret);
        pret.getInfoCles().addAll(infoCleRepository.findByTypeOrderByNomAsc(TypeInfoCle.PIECE_A_JOINDRE_PRET));

        return DocumentMapper.toResponse(documentRepository.save(pret));
    }

    @Override
    @Transactional
    public DocumentResponseDTO createFiscal(User currentUser, CreateDocumentFiscalRequest request) {
        User user = applyCommercant(currentUser, request.commercant(), request.updateProfil());

        RegimeFiscal regime = RegimeFiscal.fromFrontCode(request.regimeFiscal());
        String raison = request.commercant().raisonSociale().trim();
        String nom = (request.nom() == null || request.nom().isBlank())
                ? "Dossier fiscal — " + raison
                : request.nom().trim();

        DocumentFiscaux fiscal = new DocumentFiscaux();
        fiscal.setNom(nom);
        fiscal.setType(TypeDocument.DOCUMENT_FISCAL);
        fiscal.setObjet(regime.getLabel());
        fiscal.setDateDeGeneration(Instant.now());
        fiscal.setUser(user);
        fiscal.setRaisonSociale(raison);
        fiscal.setRegimeFiscal(regime);
        fiscal.setExerciceFiscal(request.exerciceFiscal().trim());
        fiscal.setCentreImpots(request.centreImpots().trim());
        fiscal.setNatureImpot(request.natureImpot().trim());
        fiscal.setDebutPeriodeDeclaration(request.debutPeriodeDeclaration().trim());
        fiscal.setFinPeriodeDeclaration(request.finPeriodeDeclaration().trim());
        fiscal.setMontantImpot(request.montantImpot().trim());
        fiscal.setDatePaiement(blankToNull(request.datePaiement()));
        fiscal.setMoyenPaiement(blankToNull(request.moyenPaiement()));
        fiscal.setReferencePaiement(blankToNull(request.referencePaiement()));
        fiscal.setChiffreAffairesPeriode(request.chiffreAffairesPeriode());

        fiscal.getInfoCles().addAll(infoCleRepository.findByTypeOrderByNomAsc(TypeInfoCle.PIECE_A_JOINDRE_FISCAL));
        attachMatchingInfoCle(fiscal, TypeInfoCle.CENTRE_IMPOTS, request.centreImpots());
        attachMatchingInfoCle(fiscal, TypeInfoCle.NATURE_IMPOT, request.natureImpot());

        return DocumentMapper.toResponse(documentRepository.save(fiscal));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> listMine(User currentUser) {
        return documentRepository.findByUserOrderByDateDeGenerationDesc(currentUser).stream()
                .map(DocumentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponseDTO getById(User currentUser, long id) {
        Document document = documentRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable"));
        return DocumentMapper.toResponse(document);
    }

    // ── Historique CA / charges (reprend la logique front) ──────────────

    private List<LigneHistoriqueDTO> buildHistorique(User user, int duree) {
        List<YearMonth> mois = genererMoisCles(duree);
        List<Sale> ventes = saleRepository.findByUserOrderBySaleDateDesc(user);
        List<Charge> charges = chargeRepository.findByUserOrderByDateDesc(user);

        Map<YearMonth, Double> caParMois = new HashMap<>();
        Map<YearMonth, Double> chargesParMois = new HashMap<>();

        for (Sale sale : ventes) {
            YearMonth ym = YearMonth.from(sale.getSaleDate());
            caParMois.merge(ym, sale.getTotalAmount(), Double::sum);
        }
        for (Charge charge : charges) {
            YearMonth ym = YearMonth.from(charge.getDate());
            chargesParMois.merge(ym, charge.getAmount(), Double::sum);
        }

        DateTimeFormatter cleFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        List<LigneHistoriqueDTO> lignes = new ArrayList<>();
        for (YearMonth ym : mois) {
            String cle = ym.format(cleFmt);
            String label = MOIS_NOMS[ym.getMonthValue() - 1] + " " + ym.getYear();
            lignes.add(new LigneHistoriqueDTO(
                    cle,
                    label,
                    caParMois.getOrDefault(ym, 0d),
                    chargesParMois.getOrDefault(ym, 0d)
            ));
        }
        return lignes;
    }

    private List<YearMonth> genererMoisCles(int duree) {
        YearMonth courant = YearMonth.from(LocalDate.now());
        List<YearMonth> resultat = new ArrayList<>();
        for (int i = duree - 1; i >= 0; i--) {
            resultat.add(courant.minusMonths(i));
        }
        return resultat;
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private User applyCommercant(
            User currentUser,
            CreateDocumentPretRequest.InfosCommercantPayload payload,
            boolean updateProfil
    ) {
        return applyCommercantFields(
                currentUser,
                payload.raisonSociale(),
                payload.activite(),
                payload.adresse(),
                payload.niu(),
                payload.dateCreationActivite(),
                updateProfil
        );
    }

    private User applyCommercant(
            User currentUser,
            CreateDocumentFiscalRequest.InfosCommercantPayload payload,
            boolean updateProfil
    ) {
        return applyCommercantFields(
                currentUser,
                payload.raisonSociale(),
                payload.activite(),
                payload.adresse(),
                payload.niu(),
                payload.dateCreationActivite(),
                updateProfil
        );
    }

    private User applyCommercantFields(
            User currentUser,
            String raisonSociale,
            String activite,
            String adresse,
            String niu,
            String dateCreationActivite,
            boolean updateProfil
    ) {
        if (!updateProfil) {
            return currentUser;
        }
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        user.setCompanyName(raisonSociale.trim());
        user.setActivite(activite.trim());
        user.setAdresse(adresse.trim());
        user.setNiu(niu.trim());
        user.setDateDeCreationActivite(dateCreationActivite.trim());
        return userRepository.save(user);
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

    private List<InfoCleDTO> mapInfoCles(TypeInfoCle type) {
        return infoCleRepository.findByTypeOrderByNomAsc(type).stream()
                .map(DocumentMapper::toInfoCleDto)
                .collect(Collectors.toList());
    }

    private static int normalizeDuree(int dureeHistorique) {
        return dureeHistorique == 12 ? 12 : 6;
    }

    private static double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return 0d;
        }
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
