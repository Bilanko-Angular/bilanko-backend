package com.backend.bilanko.controller.transaction.admin;

import com.backend.bilanko.DTO.transaction.admin.AdminSaleCreateRequest;
import com.backend.bilanko.DTO.transaction.admin.AdminSaleResponseDTO;
import com.backend.bilanko.DTO.transaction.admin.AdminSaleSummaryDTO;
import com.backend.bilanko.DTO.transaction.admin.AdminSaleUpdateRequest;
import com.backend.bilanko.services.transaction.admin.AdminSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/admin/sales")
@RequiredArgsConstructor
public class AdminSaleController {

    private final AdminSaleService adminSaleService;

    @GetMapping("/summary")
    public ResponseEntity<AdminSaleSummaryDTO> getSummary() {
        return ResponseEntity.ok(adminSaleService.getSummary(currentAdminEmail()));
    }

    @GetMapping
    public ResponseEntity<Page<AdminSaleResponseDTO>> getPagedSales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminSaleService.getPagedSales(currentAdminEmail(), page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AdminSaleResponseDTO>> searchSales(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minItems,
            @RequestParam(required = false) Integer maxItems,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminSaleService.searchSales(
                currentAdminEmail(), keyword, minItems, maxItems,
                minAmount, maxAmount, startDate, endDate, page, size));
    }

    @PostMapping
    public ResponseEntity<AdminSaleResponseDTO> createSale(@Valid @RequestBody AdminSaleCreateRequest request) {
        return ResponseEntity.ok(adminSaleService.createSale(currentAdminEmail(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminSaleResponseDTO> updateSale(
            @PathVariable Long id,
            @Valid @RequestBody AdminSaleUpdateRequest request) {
        return ResponseEntity.ok(adminSaleService.updateSale(currentAdminEmail(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id) {
        adminSaleService.deleteSale(currentAdminEmail(), id);
        return ResponseEntity.noContent().build();
    }

    // --- Utilitaires privés ---

    private String currentAdminEmail() {
        return Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
    }
}
