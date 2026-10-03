package com.backend.bilanko.utils.routes;

public final class SupportMessagingApiRoutes {
    private SupportMessagingApiRoutes() {}

    public static final String BASE = "/api/support";

    // Merchant
    public static final String MY_CONVERSATION = "/conversations/mine";
    public static final String START = "/conversations/start";

    // Shared
    public static final String CONVERSATION_BY_ID = "/conversations/{id}";
    public static final String MESSAGES = "/conversations/{id}/messages";

    // Admin
    public static final String ADMIN_CONVERSATIONS = "/conversations";
    public static final String CLAIM = "/conversations/claim/{token}";
    public static final String TRANSFER = "/conversations/{id}/transfer";
}
