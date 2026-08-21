package com.backend.bilanko.DTO.concept.transaction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ChargeRequestDTO(
        @NotBlank
        String label,
        @NotBlank
        String supplier,
        @Min(0)
        double amount,
        @NotNull
        LocalDate date
) {
}
