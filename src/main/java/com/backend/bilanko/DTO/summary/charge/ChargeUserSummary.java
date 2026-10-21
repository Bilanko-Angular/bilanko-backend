package com.backend.bilanko.DTO.summary.charge;

public record ChargeUserSummary(
        long totalCharges,
        long monthlyCharges,
        String topSupplier,
        double averageChargeAmount
) {}
