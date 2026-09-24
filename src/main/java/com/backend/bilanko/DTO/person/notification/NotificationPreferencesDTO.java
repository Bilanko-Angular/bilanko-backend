package com.backend.bilanko.DTO.person.notification;

public record NotificationPreferencesDTO(
        boolean stockAlerts,
        boolean newSales,
        boolean monthlyReports,
        boolean updates
) {}