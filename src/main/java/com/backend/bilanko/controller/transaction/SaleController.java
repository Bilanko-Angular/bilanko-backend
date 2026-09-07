package com.backend.bilanko.controller.transaction;

import com.backend.bilanko.DTO.concept.transaction.SaleRequestDTO;
import com.backend.bilanko.DTO.concept.transaction.SaleResponseDTO;
import com.backend.bilanko.DTO.concept.transaction.SaleSummaryDTO;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.services.transaction.SaleService;
import com.backend.bilanko.utils.routes.SaleApiRoutes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping(SaleApiRoutes.BASE)
    public ResponseEntity<SaleResponseDTO> createSale(
            @Valid @RequestBody SaleRequestDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        SaleResponseDTO response = saleService.createSale(dto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(SaleApiRoutes.BASE)
    public ResponseEntity<List<SaleResponseDTO>> getAllSales(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(saleService.getAllSales(currentUser, from, to));
    }

    @GetMapping(SaleApiRoutes.SUMMARY)
    public ResponseEntity<SaleSummaryDTO> getSummary(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(saleService.getSummary(currentUser, from, to));
    }

    @GetMapping(SaleApiRoutes.BY_ID)
    public ResponseEntity<SaleResponseDTO> getSaleById(
            @PathVariable long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(saleService.getSaleById(id, currentUser));
    }

    @PutMapping(SaleApiRoutes.BY_ID)
    public ResponseEntity<SaleResponseDTO> updateSale(
            @PathVariable long id,
            @Valid @RequestBody SaleRequestDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(saleService.updateSale(id, dto, currentUser));
    }

    @DeleteMapping(SaleApiRoutes.BY_ID)
    public ResponseEntity<Void> deleteSale(
            @PathVariable long id,
            @AuthenticationPrincipal User currentUser
    ) {
        saleService.deleteSale(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
