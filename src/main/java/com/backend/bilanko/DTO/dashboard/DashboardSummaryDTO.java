package com.backend.bilanko.DTO.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryDTO {
    private List<KpiDTO> kpis;
    private ActivityEvolutionDTO activity;
    private List<SupplierShareDTO> suppliers;
    private List<RecentSaleDTO> recentSales;
    private List<RecentUserDTO> recentUsers;
    private List<StockAlertDTO> stockAlerts;
}
