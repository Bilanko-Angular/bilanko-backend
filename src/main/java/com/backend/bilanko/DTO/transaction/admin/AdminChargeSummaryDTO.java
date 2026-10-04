package com.backend.bilanko.DTO.transaction.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminChargeSummaryDTO {
    private long totalCount;
    private double totalSum;
    private double currentMonthSum;
    private double averagePrice;
}
