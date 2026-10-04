package com.backend.bilanko.mapper;

import com.backend.bilanko.DTO.object.product.admin.AdminProductResponseDTO;
import com.backend.bilanko.models.object.product.Product;

import java.util.Collections;
import java.util.List;

public final class ProductMapper {

    private ProductMapper() {
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
