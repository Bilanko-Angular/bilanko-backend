package com.backend.bilanko.services.transaction.admin;

import com.backend.bilanko.DTO.transaction.admin.AdminChargeCreateRequest;
import com.backend.bilanko.DTO.transaction.admin.AdminChargeResponseDTO;
import com.backend.bilanko.DTO.transaction.admin.AdminChargeSummaryDTO;
import com.backend.bilanko.DTO.transaction.admin.AdminChargeUpdateRequest;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface AdminChargeService {
    AdminChargeSummaryDTO getSummary(String adminEmail);
    Page<AdminChargeResponseDTO> getPagedCharges(String adminEmail, int page, int size);
    Page<AdminChargeResponseDTO> searchCharges(String adminEmail, String keyword, Long categoryId, LocalDate startDate, LocalDate endDate, int page, int size);
    AdminChargeResponseDTO createCharge(String adminEmail, AdminChargeCreateRequest request);
    AdminChargeResponseDTO updateCharge(String adminEmail, Long chargeId, AdminChargeUpdateRequest request);
    void deleteCharge(String adminEmail, Long chargeId);
}
