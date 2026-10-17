package com.backend.bilanko.services.search;

import com.backend.bilanko.DTO.search.GlobalSearchResultDTO;
import com.backend.bilanko.mapper.ChargeMapper;
import com.backend.bilanko.mapper.ProductMapper;
import com.backend.bilanko.mapper.SaleMapper;
import com.backend.bilanko.models.concept.category.ProductCategory;
import com.backend.bilanko.models.object.product.Product;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.models.transaction.Charge;
import com.backend.bilanko.models.transaction.Sale;
import com.backend.bilanko.models.transaction.SaleItem;
import com.backend.bilanko.repository.object.product.ProductRepository;
import com.backend.bilanko.repository.transaction.ChargeRepository;
import com.backend.bilanko.repository.transaction.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class GlobalSearchService {

    private static final int MAX_RESULTS = 10;

    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final ChargeRepository chargeRepository;

    public List<GlobalSearchResultDTO> search(String keyword, User currentUser) {
        String normalizedKeyword = normalizeKeyword(keyword);

        List<ScoredResult> results = new ArrayList<>();
        productRepository.findByUserEmail(currentUser.getEmail()).stream()
                .filter(product -> matches(product, normalizedKeyword))
                .map(product -> new ScoredResult(
                        "PRODUCT",
                        product.getId(),
                        product.getCreatedAt(),
                        ProductMapper.mapProductToProductApiDTO(product)))
                .forEach(results::add);

        saleRepository.findByUserOrderBySaleDateDesc(currentUser).stream()
                .filter(sale -> matches(sale, normalizedKeyword))
                .map(sale -> new ScoredResult(
                        "SALE",
                        sale.getId(),
                        sale.getCreatedAt(),
                        SaleMapper.toDto(sale)))
                .forEach(results::add);

        chargeRepository.findByUserOrderByDateDesc(currentUser).stream()
                .filter(charge -> matches(charge, normalizedKeyword))
                .map(charge -> new ScoredResult(
                        "CHARGE",
                        charge.getId(),
                        charge.getCreatedAt(),
                        ChargeMapper.toDto(charge)))
                .forEach(results::add);

        return results.stream()
                .sorted(Comparator
                        .comparing(ScoredResult::createdAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ScoredResult::type)
                        .thenComparingLong(ScoredResult::id))
                .limit(MAX_RESULTS)
                .map(result -> new GlobalSearchResultDTO(result.type(), result.id(), result.data()))
                .toList();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le paramètre 'keyword' est obligatoire et ne peut pas être vide"
            );
        }
        return keyword.trim().toLowerCase(Locale.ROOT);
    }

    private boolean matches(Product product, String keyword) {
        return contains(product.getName(), keyword)
                || contains(product.getReference(), keyword)
                || product.getCategories() != null && product.getCategories().stream()
                .map(ProductCategory::getName)
                .anyMatch(value -> contains(value, keyword));
    }

    private boolean matches(Sale sale, String keyword) {
        return contains(sale.getCustomerName(), keyword)
                || sale.getItems() != null && sale.getItems().stream()
                .map(SaleItem::getProduct)
                .anyMatch(product -> matches(product, keyword));
    }

    private boolean matches(Charge charge, String keyword) {
        return contains(charge.getLabel(), keyword)
                || contains(charge.getSupplier(), keyword)
                || charge.getChargeCategory() != null
                && contains(charge.getChargeCategory().getName(), keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private record ScoredResult(String type, long id, Instant createdAt, Object data) {
    }
}
