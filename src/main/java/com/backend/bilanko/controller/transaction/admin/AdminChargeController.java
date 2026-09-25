package com.backend.bilanko.controller.transaction.admin;

import com.backend.bilanko.DTO.transaction.admin.AdminChargeCreateRequest;
import com.backend.bilanko.DTO.transaction.admin.AdminChargeResponseDTO;
import com.backend.bilanko.DTO.transaction.admin.AdminChargeSummaryDTO;
import com.backend.bilanko.DTO.transaction.admin.AdminChargeUpdateRequest;
import com.backend.bilanko.services.transaction.admin.AdminChargeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/admin/charges")
@RequiredArgsConstructor
public class AdminChargeController {

    private final AdminChargeService adminChargeService;

    @GetMapping("/summary")
    public ResponseEntity<AdminChargeSummaryDTO> getSummary() {
        return ResponseEntity.ok(adminChargeService.getSummary(currentAdminEmail()));
    }

    @GetMapping
    public ResponseEntity<Page<AdminChargeResponseDTO>> getPagedCharges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminChargeService.getPagedCharges(currentAdminEmail(), page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AdminChargeResponseDTO>> searchCharges(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminChargeService.searchCharges(currentAdminEmail(), keyword, categoryId, startDate, endDate, page, size));
    }

    @PostMapping
    public ResponseEntity<AdminChargeResponseDTO> createCharge(@Valid @RequestBody AdminChargeCreateRequest request) {
        return ResponseEntity.ok(adminChargeService.createCharge(currentAdminEmail(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminChargeResponseDTO> updateCharge(
            @PathVariable Long id,
            @Valid @RequestBody AdminChargeUpdateRequest request) {
        return ResponseEntity.ok(adminChargeService.updateCharge(currentAdminEmail(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharge(@PathVariable Long id) {
        adminChargeService.deleteCharge(currentAdminEmail(), id);
        return ResponseEntity.noContent().build();
    }

    // --- Utilitaires privés ---

    private String currentAdminEmail() {
        return Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
    }
}
