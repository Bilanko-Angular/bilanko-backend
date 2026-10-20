package com.backend.bilanko.controller.transaction;

import com.backend.bilanko.DTO.summary.sale.SaleUserSummary;
import com.backend.bilanko.DTO.transaction.sale.SaleRequestDTO;
import com.backend.bilanko.DTO.transaction.sale.SaleResponseDTO;
import com.backend.bilanko.DTO.shared.PageResponse;
import com.backend.bilanko.DTO.summary.sale.SaleSummaryDTO;
import com.backend.bilanko.DTO.summary.overview.SaleTimeSeriesPointDTO;
import com.backend.bilanko.DTO.summary.overview.TopSoldProductDTO;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.services.transaction.SaleService;
import com.backend.bilanko.utils.routes.SaleApiRoutes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<PageResponse<SaleResponseDTO>> getAllSales(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(saleService.getAllSalesPaged(currentUser, from, to, page, size));
    }

    @GetMapping(SaleApiRoutes.SUMMARY)
    public ResponseEntity<SaleSummaryDTO> getSummary(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(saleService.getSummary(currentUser, from, to));
    }

    @GetMapping(SaleApiRoutes.TIMESERIES)
    public ResponseEntity<List<SaleTimeSeriesPointDTO>> getTimeSeries(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "day") String granularity
    ) {
        return ResponseEntity.ok(saleService.getTimeSeries(currentUser, from, to, granularity));
    }

    @GetMapping(SaleApiRoutes.TOP_PRODUCTS)
    public ResponseEntity<List<TopSoldProductDTO>> getTopProducts(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(defaultValue = "quantity") String sortBy
    ) {
        return ResponseEntity.ok(saleService.getTopProducts(currentUser, from, to, limit, sortBy));
    }

    @GetMapping(SaleApiRoutes.SEARCH)
    public ResponseEntity<PageResponse<SaleResponseDTO>> searchSales(
            @RequestParam String keyword,
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(saleService.searchSalesPaged(keyword, currentUser, page, size));
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

    @GetMapping(SaleApiRoutes.USER_SUMMARY)
    public ResponseEntity<SaleUserSummary> saleUserSummary(Authentication authentication){
        return ResponseEntity.ok(saleService.getSummary(authentication.getName()));
    }
}
