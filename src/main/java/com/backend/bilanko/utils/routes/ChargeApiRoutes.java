package com.backend.bilanko.utils.routes;

public final class ChargeApiRoutes {
    public static final String BASE = "/api/charges";
    public static final String BY_ID = BASE + "/{id}";
    public static final String SUMMARY = BASE + "/summary";
    public static final String SEARCH = BASE + "/search";

    private ChargeApiRoutes() {
    }
}
