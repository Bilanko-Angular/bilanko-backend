package com.backend.bilanko.services.object.product.admin;

import com.backend.bilanko.DTO.object.product.admin.AdminProductCreateRequest;
import com.backend.bilanko.DTO.object.product.admin.AdminProductResponseDTO;
import com.backend.bilanko.DTO.object.product.admin.AdminProductSummaryDTO;
import com.backend.bilanko.DTO.object.product.admin.AdminProductUpdateRequest;
import org.springframework.data.domain.Page;

public interface AdminProductService {

    AdminProductSummaryDTO getSummary();

    Page<AdminProductResponseDTO> getPagedProducts(int page, int size);

    Page<AdminProductResponseDTO> searchProducts(
            String keyword,
            Long categoryId,
            Double minPurchasePrice,
            Double maxPurchasePrice,
            Double minPrice,
            Double maxPrice,
            int page,
            int size);

    AdminProductResponseDTO getById(Long productId);

    AdminProductResponseDTO createProduct(AdminProductCreateRequest request);

    AdminProductResponseDTO updateProduct(Long productId, AdminProductUpdateRequest request);

    void deleteProduct(Long productId);
}
