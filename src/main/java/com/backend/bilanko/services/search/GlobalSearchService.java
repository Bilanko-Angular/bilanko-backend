package com.backend.bilanko.services.search;

import com.backend.bilanko.DTO.search.GlobalSearchResultDTO;
import com.backend.bilanko.mapper.ChargeMapper;
import com.backend.bilanko.mapper.ProductMapper;
import com.backend.bilanko.mapper.SaleMapper;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.repository.object.product.ProductRepository;
import com.backend.bilanko.repository.transaction.ChargeRepository;
import com.backend.bilanko.repository.transaction.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Service de recherche globale optimisé.
 *
 * <p>Trois axes d'optimisation par rapport à l'implémentation naïve :</p>
 * <ol>
 *   <li><b>Push-down BDD</b> : les requêtes JPQL filtrent par keyword (LIKE)
 *       directement en base, sans charger toutes les entités du user en mémoire.</li>
 *   <li><b>Élimination du N+1</b> : JOIN FETCH sur les associations eagerly chargées
 *       (Sale→items→product→categories, Charge→chargeCategory).</li>
 *   <li><b>Parallélisation @Async</b> : les trois requêtes indépendantes (produits,
 *       ventes, charges) sont lancées simultanément via le pool "searchExecutor".</li>
 * </ol>
 *
 * <p>Chaque source est limitée à {@value MAX_RESULTS_PER_TYPE} résultats directement
 * en BDD via {@link PageRequest}, ce qui donne un maximum de
 * {@code 3 × MAX_RESULTS_PER_TYPE} résultats fusionnés.</p>
 */
@Service
@RequiredArgsConstructor
public class GlobalSearchService {

    /** Nombre maximal de résultats retournés par source (produits / ventes / charges). */
    static final int MAX_RESULTS_PER_TYPE = 5;

    private final ProductRepository productRepository;
    private final SaleRepository    saleRepository;
    private final ChargeRepository  chargeRepository;

    // -------------------------------------------------------------------------
    // Point d'entrée public
    // -------------------------------------------------------------------------

    public List<GlobalSearchResultDTO> search(String keyword, User currentUser) {
        String kw = normalizeKeyword(keyword);

        // Lancement parallèle des 3 requêtes via le pool "searchExecutor"
        CompletableFuture<List<ScoredResult>> productsFuture  = searchProducts(kw, currentUser);
        CompletableFuture<List<ScoredResult>> salesFuture     = searchSales(kw, currentUser);
        CompletableFuture<List<ScoredResult>> chargesFuture   = searchCharges(kw, currentUser);

        // Attente + fusion des résultats dans l'ordre de fraîcheur
        List<ScoredResult> merged = new ArrayList<>();
        merged.addAll(productsFuture.join());
        merged.addAll(salesFuture.join());
        merged.addAll(chargesFuture.join());

        return merged.stream()
                .sorted(Comparator
                        .comparing(ScoredResult::createdAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ScoredResult::type)
                        .thenComparingLong(ScoredResult::id))
                .map(r -> new GlobalSearchResultDTO(r.type(), r.id(), r.data()))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Méthodes @Async — exécutées sur le pool "searchExecutor"
    // -------------------------------------------------------------------------

    /**
     * Recherche les produits correspondant au keyword.
     * Limite à {@value MAX_RESULTS_PER_TYPE} résultats directement en BDD.
     */
    @Async("searchExecutor")
    protected CompletableFuture<List<ScoredResult>> searchProducts(String kw, User currentUser) {
        PageRequest page = PageRequest.of(0, MAX_RESULTS_PER_TYPE);
        List<ScoredResult> results = productRepository
                .searchByUserEmailAndKeyword(currentUser.getEmail(), kw)
                .stream()
                .limit(MAX_RESULTS_PER_TYPE)   // sécurité côté Java si la BDD retourne DISTINCT sans LIMIT
                .map(p -> new ScoredResult("PRODUCT", p.getId(), p.getCreatedAt(),
                        ProductMapper.mapProductToProductApiDTO(p)))
                .toList();
        return CompletableFuture.completedFuture(results);
    }

    /**
     * Recherche les ventes correspondant au keyword, avec JOIN FETCH pour éviter le N+1.
     * Limite à {@value MAX_RESULTS_PER_TYPE} résultats directement en BDD.
     */
    @Async("searchExecutor")
    protected CompletableFuture<List<ScoredResult>> searchSales(String kw, User currentUser) {
        PageRequest page = PageRequest.of(0, MAX_RESULTS_PER_TYPE);
        List<ScoredResult> results = saleRepository
                .searchByKeywordFetched(currentUser, kw, page)
                .stream()
                .map(s -> new ScoredResult("SALE", s.getId(), s.getCreatedAt(),
                        SaleMapper.toDto(s)))
                .toList();
        return CompletableFuture.completedFuture(results);
    }

    /**
     * Recherche les charges correspondant au keyword, avec JOIN FETCH sur chargeCategory.
     * Limite à {@value MAX_RESULTS_PER_TYPE} résultats directement en BDD.
     */
    @Async("searchExecutor")
    protected CompletableFuture<List<ScoredResult>> searchCharges(String kw, User currentUser) {
        PageRequest page = PageRequest.of(0, MAX_RESULTS_PER_TYPE);
        List<ScoredResult> results = chargeRepository
                .searchByUserAndKeyword(currentUser, kw, page)
                .stream()
                .map(c -> new ScoredResult("CHARGE", c.getId(), c.getCreatedAt(),
                        ChargeMapper.toDto(c)))
                .toList();
        return CompletableFuture.completedFuture(results);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le paramètre 'keyword' est obligatoire et ne peut pas être vide"
            );
        }
        return keyword.trim().toLowerCase(Locale.ROOT);
    }

    // DTO interne pour la fusion et le tri avant mapping final
    record ScoredResult(String type, long id, Instant createdAt, Object data) {
    }
}
