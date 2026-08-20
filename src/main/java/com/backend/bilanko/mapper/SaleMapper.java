package com.backend.bilanko.mapper;

import com.backend.bilanko.DTO.concept.transaction.SaleItemResponseDTO;
import com.backend.bilanko.DTO.concept.transaction.SaleResponseDTO;
import com.backend.bilanko.models.transaction.Sale;
import com.backend.bilanko.models.transaction.SaleItem;

import java.util.List;

public final class SaleMapper {

    private SaleMapper() {
    }

    public static SaleResponseDTO toDto(Sale sale) {
        List<SaleItemResponseDTO> items = sale.getItems().stream()
                .map(SaleMapper::toDto)
                .toList();

        return new SaleResponseDTO(
                sale.getId(),
                sale.getSaleDate(),
                sale.getCustomerName(),
                sale.getTotalAmount(),
                sale.getTotalMargin(),
                items
        );
    }

    public static SaleItemResponseDTO toDto(SaleItem item) {
        return new SaleItemResponseDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitSellingPrice(),
                item.getUnitPurchasePrice(),
                item.getMargin()
        );
    }
}
