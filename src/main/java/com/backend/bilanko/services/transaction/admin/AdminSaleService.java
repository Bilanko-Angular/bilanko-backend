package com.backend.bilanko.services.transaction.admin;

import com.backend.bilanko.DTO.transaction.admin.AdminSaleCreateRequest;
import com.backend.bilanko.DTO.transaction.admin.AdminSaleResponseDTO;
import com.backend.bilanko.DTO.transaction.admin.AdminSaleSummaryDTO;
import com.backend.bilanko.DTO.transaction.admin.AdminSaleUpdateRequest;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface AdminSaleService {
    AdminSaleSummaryDTO getSummary();
    Page<AdminSaleResponseDTO> getPagedSales(int page, int size);
    Page<AdminSaleResponseDTO> searchSales(String keyword, Integer minItems, Integer maxItems,
                                           Double minAmount, Double maxAmount,
                                           LocalDateTime startDate, LocalDateTime endDate,
                                           int page, int size);
    AdminSaleResponseDTO createSale(AdminSaleCreateRequest request);
    AdminSaleResponseDTO updateSale(Long saleId, AdminSaleUpdateRequest request);
    void deleteSale(Long saleId);
}
