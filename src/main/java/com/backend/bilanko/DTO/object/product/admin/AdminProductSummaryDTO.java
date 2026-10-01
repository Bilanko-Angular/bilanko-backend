package com.backend.bilanko.DTO.object.product.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminProductSummaryDTO {
    private long totalCount;
    private long addedThisMonthCount;
    private long associatedWithSaleCount;
    private double averagePrice;
}
