package com.backend.bilanko.DTO.transaction.admin;

import com.backend.bilanko.DTO.transaction.sale.SaleItemResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminSaleResponseDTO {
    private long id;
    private LocalDateTime saleDate;
    private String customerName;
    private double totalAmount;
    private double totalMargin;
    private int itemCount;
    private List<SaleItemResponseDTO> items;

    // Infos utilisateur propriétaire
    private long userId;
    private String userName;
    private String userSubname;
}
