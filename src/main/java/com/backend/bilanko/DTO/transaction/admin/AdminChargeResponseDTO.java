package com.backend.bilanko.DTO.transaction.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminChargeResponseDTO {
    private long id;
    private LocalDate date;
    private Instant createdAt;
    private String label;
    private String supplier;
    private double amount;
    
    private Long categoryId;
    private String categoryName;
    
    private Long userId;
    private String userName;
    private String userSubname;
}
