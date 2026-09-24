package com.backend.bilanko.utils.routes;

public final class NotificationApiRoutes {

    public static final String BASE = "/api/notifications";
    public static final String STREAM = BASE + "/stream";
    public static final String BY_ID = BASE + "/{id}";
    public static final String MARK_READ = BY_ID + "/read";
    public static final String MARK_ALL_READ = BASE + "/read-all";
    public static final String UNREAD_COUNT = BASE + "/unread-count";
    public static final String BROADCAST_APP_UPDATE = BASE + "/broadcast/app-update";

    private NotificationApiRoutes() {
    }
}
