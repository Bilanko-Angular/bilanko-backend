package com.backend.bilanko.mapper;

import com.backend.bilanko.DTO.transaction.admin.AdminSaleResponseDTO;
import com.backend.bilanko.DTO.transaction.sale.SaleItemResponseDTO;
import com.backend.bilanko.DTO.transaction.sale.SaleResponseDTO;
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

    public static AdminSaleResponseDTO mapSaleToAdminSaleDTO(Sale sale) {
        List<SaleItemResponseDTO> items = sale.getItems().stream()
                .map(item -> new SaleItemResponseDTO(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitSellingPrice(),
                        item.getUnitPurchasePrice(),
                        item.getMargin()
                ))
                .toList();

        return AdminSaleResponseDTO.builder()
                .id(sale.getId())
                .saleDate(sale.getSaleDate())
                .customerName(sale.getCustomerName())
                .totalAmount(sale.getTotalAmount())
                .totalMargin(sale.getTotalMargin())
                .itemCount(sale.getItems().size())
                .items(items)
                .userId(sale.getUser().getId())
                .userName(sale.getUser().getName())
                .userSubname(sale.getUser().getSubname())
                .build();
    }
}
