package com.backend.bilanko.utils.routes;

public final class SaleApiRoutes {
    public static final String BASE = "/api/sales";
    public static final String BY_ID = BASE + "/{id}";
    public static final String SUMMARY = BASE + "/summary";
    public static final String SEARCH = BASE + "/search";

    private SaleApiRoutes() {}
}