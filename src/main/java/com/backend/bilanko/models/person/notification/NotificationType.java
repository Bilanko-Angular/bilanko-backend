package com.backend.bilanko.models.person.notification;

/**
 * Types de notifications.
 * Les types SUPPORT_* sont destinés aux MERCHANT et/ou ADMIN selon le cas.
 * Les types absents de {@link NotificationPreferences} sont toujours délivrés.
 */
public enum NotificationType {
    NEW_SALE,
    MONTHLY_REPORT,
    APP_UPDATE,
    WELCOME,
    NEW_CHARGE,
    /** Nouvelle conversation support : invite les admins à prendre la main. */
    SUPPORT_CLAIM_REQUEST,
    /** Transfert de conversation vers un autre admin. */
    SUPPORT_TRANSFER_REQUEST,
    /** Conversation prise en charge : le commerçant peut discuter. */
    SUPPORT_CONVERSATION_READY,
    /** Changement d'admin sur la conversation. */
    SUPPORT_ADMIN_CHANGED,
    /** Nouveau message dans une conversation support. */
    SUPPORT_NEW_MESSAGE
}
