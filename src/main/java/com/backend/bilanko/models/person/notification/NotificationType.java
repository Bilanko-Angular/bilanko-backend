package com.backend.bilanko.models.person.notification;

/**
 * Types de notifications destinées aux MERCHANT.
 * Les types absents de {@link NotificationPreferences} sont toujours délivrés.
 */
public enum NotificationType {
    NEW_SALE,
    MONTHLY_REPORT,
    APP_UPDATE,
    WELCOME,
    NEW_CHARGE
}
