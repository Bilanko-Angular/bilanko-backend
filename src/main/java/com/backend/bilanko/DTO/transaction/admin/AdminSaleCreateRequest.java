package com.backend.bilanko.DTO.transaction.admin;

import com.backend.bilanko.DTO.transaction.sale.SaleItemRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class AdminSaleCreateRequest {

    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    private Long userId;

    private LocalDateTime saleDate;

    private String customerName;

    @NotEmpty(message = "La liste des articles ne peut pas être vide")
    @Valid
    private List<SaleItemRequestDTO> items;
}
