package com.backend.bilanko.controller.transaction;

import com.backend.bilanko.DTO.concept.transaction.SaleRequestDTO;
import com.backend.bilanko.DTO.concept.transaction.SaleResponseDTO;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.services.transaction.SaleService;
import com.backend.bilanko.utils.constant.SaleApiRoutes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(saleService.getAllSales(currentUser));
    }

    @GetMapping(SaleApiRoutes.BY_ID)
    public ResponseEntity<SaleResponseDTO> getSaleById(
            @PathVariable long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(saleService.getSaleById(id, currentUser));
    }
}