package com.backend.bilanko.DTO.object.product.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminProductResponseDTO {
    private long id;
    private Instant createdAt;
    private String name;
    private String reference;
    private double purchasePrice;
    private double price;
    private int quantity;
    private Integer alertThreshold;
    private List<CategoryInfo> categories;

    private long userId;
    private String userName;
    private String userSubname;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryInfo {
        private long id;
        private String name;
    }
}
