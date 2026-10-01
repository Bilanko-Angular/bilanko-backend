package com.backend.bilanko.controller.object.product.admin;

import com.backend.bilanko.DTO.object.product.admin.AdminProductCreateRequest;
import com.backend.bilanko.DTO.object.product.admin.AdminProductResponseDTO;
import com.backend.bilanko.DTO.object.product.admin.AdminProductSummaryDTO;
import com.backend.bilanko.DTO.object.product.admin.AdminProductUpdateRequest;
import com.backend.bilanko.services.object.product.admin.AdminProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping("/summary")
    public ResponseEntity<AdminProductSummaryDTO> getSummary() {
        return ResponseEntity.ok(adminProductService.getSummary());
    }

    @GetMapping
    public ResponseEntity<Page<AdminProductResponseDTO>> getPagedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminProductService.getPagedProducts(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AdminProductResponseDTO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPurchasePrice,
            @RequestParam(required = false) Double maxPurchasePrice,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminProductService.searchProducts(
                keyword, categoryId,
                minPurchasePrice, maxPurchasePrice,
                minPrice, maxPrice,
                page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminProductResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(adminProductService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AdminProductResponseDTO> createProduct(
            @Valid @RequestBody AdminProductCreateRequest request) {
        return ResponseEntity.ok(adminProductService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody AdminProductUpdateRequest request) {
        return ResponseEntity.ok(adminProductService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        adminProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
