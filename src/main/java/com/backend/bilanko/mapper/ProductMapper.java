package com.backend.bilanko.mapper;

import com.backend.bilanko.DTO.object.product.admin.AdminProductResponseDTO;
import com.backend.bilanko.DTO.object.product.ProductApiDTO;
import com.backend.bilanko.models.object.product.Product;

import java.util.Collections;
import java.util.List;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductApiDTO mapProductToProductApiDTO(Product product) {
        List<ProductApiDTO.CategoryDTO> categories = product.getCategories() == null
                ? Collections.emptyList()
                : product.getCategories().stream()
                .map(category -> new ProductApiDTO.CategoryDTO(
                        category.getId(),
                        category.getName()))
                .toList();

        return new ProductApiDTO(
                product.getId(),
                product.getName(),
                product.getQuantity(),
                product.getPrice(),
                product.getPurchasePrice(),
                categories,
                product.getReference(),
                product.getAlertThreshold(),
                product.getCreatedAt());
    }

    public static AdminProductResponseDTO mapProductToAdminProductDTO(Product product) {
        List<AdminProductResponseDTO.CategoryInfo> categories = product.getCategories() == null
                ? Collections.emptyList()
                : product.getCategories().stream()
                .map(c -> AdminProductResponseDTO.CategoryInfo.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .toList();

        return AdminProductResponseDTO.builder()
                .id(product.getId())
                .createdAt(product.getCreatedAt())
                .name(product.getName())
                .reference(product.getReference())
                .purchasePrice(product.getPurchasePrice())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .alertThreshold(product.getAlertThreshold())
                .categories(categories)
                .userId(product.getUser().getId())
                .userName(product.getUser().getName())
                .userSubname(product.getUser().getSubname())
                .build();
    }
}
