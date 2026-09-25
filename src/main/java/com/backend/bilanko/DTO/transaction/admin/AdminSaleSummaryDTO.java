package com.backend.bilanko.DTO.transaction.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminSaleSummaryDTO {
    private long totalCount;
    private double totalAmount;
    private double totalMargin;
    private double currentMonthAmount;
}
