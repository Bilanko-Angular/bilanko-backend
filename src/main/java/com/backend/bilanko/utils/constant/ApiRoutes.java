package com.backend.bilanko.utils.constant;

public class ApiRoutes {
    private ApiRoutes() {}

    // ── Produits ────────────────────────────────────────────────
    public static final String PRODUCT8          = "/api/products";

    public static final String PRODUCTS_CREATE   = "/create";           // POST   /api/products/create
    public static final String PRODUCTS_ALL      = "/all";              // GET    /api/products/all
    public static final String PRODUCTS_MY       = "/my";               // GET    /api/products/my         (produits du user connecté)
    public static final String PRODUCTS_BY_ID    = "/{id}";             // GET    /api/products/{id}
    public static final String PRODUCTS_UPDATE   = "/{id}";             // PUT    /api/products/{id}
    public static final String PRODUCTS_DELETE   = "/{id}";             // DELETE /api/products/{id}
}

