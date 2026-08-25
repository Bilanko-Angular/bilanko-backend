package com.backend.bilanko.services.object.product;

import com.backend.bilanko.models.object.product.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    public static Specification<Product> belongsToUser(String email) {
        return (root, query, cb) -> cb.equal(root.get("user").get("email"), email);
    }

    public static Specification<Product> nameContains(String search) {
        if (search == null || search.isBlank()) return null;
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("reference")), pattern)
        );
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        if (categoryId == null) return null;
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.join("categories").get("id"), categoryId);
        };
    }

    // ok / warning / error, calculé à partir de quantity vs alertThreshold
    public static Specification<Product> hasStockStatus(String status) {
        if (status == null || status.isBlank() || status.equalsIgnoreCase("tous")) return null;
        return switch (status.toLowerCase()) {
            case "error" -> (root, query, cb) -> cb.equal(root.get("quantity"), 0);
            case "warning" -> (root, query, cb) -> cb.and(
                    cb.greaterThan(root.get("quantity"), 0),
                    cb.lessThanOrEqualTo(root.get("quantity"), root.get("alertThreshold"))
            );
            case "ok" -> (root, query, cb) -> cb.greaterThan(root.get("quantity"), root.get("alertThreshold"));
            default -> null;
        };
    }

    @SafeVarargs
    public static Specification<Product> combine(Specification<Product>... specs) {
        Specification<Product> result = null;
        for (Specification<Product> s : specs) {
            if (s == null) continue;
            result = (result == null) ? Specification.where(s) : result.and(s);
        }
        return result != null ? result : (root, query, cb) -> cb.conjunction();
    }
}