package com.backend.bilanko.DTO.person;

public record NotificationPreferencesDTO(
        boolean stockAlerts,
        boolean newSales,
        boolean monthlyReports,
        boolean updates
) {}