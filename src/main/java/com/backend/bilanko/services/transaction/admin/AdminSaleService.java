package com.backend.bilanko.services.transaction.admin;

import com.backend.bilanko.DTO.transaction.admin.AdminSaleCreateRequest;
import com.backend.bilanko.DTO.transaction.admin.AdminSaleResponseDTO;
import com.backend.bilanko.DTO.transaction.admin.AdminSaleSummaryDTO;
import com.backend.bilanko.DTO.transaction.admin.AdminSaleUpdateRequest;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface AdminSaleService {
    AdminSaleSummaryDTO getSummary(String adminEmail);
    Page<AdminSaleResponseDTO> getPagedSales(String adminEmail, int page, int size);
    Page<AdminSaleResponseDTO> searchSales(String adminEmail, String keyword, Integer minItems, Integer maxItems,
                                           Double minAmount, Double maxAmount,
                                           LocalDateTime startDate, LocalDateTime endDate,
                                           int page, int size);
    AdminSaleResponseDTO createSale(String adminEmail, AdminSaleCreateRequest request);
    AdminSaleResponseDTO updateSale(String adminEmail, Long saleId, AdminSaleUpdateRequest request);
    void deleteSale(String adminEmail, Long saleId);
}
