package com.backend.bilanko.utils.constant;


public class CategoryApiRoutes {
    private CategoryApiRoutes() {}
    public static final String category        = "/api/categories";
    public static final String create_category = "/create";          // POST   /api/categories/create
    public static final String find_all        = "/all";             // GET    /api/categories/all
    public static final String find_by_id      = "/{id}";           // GET    /api/categories/{id}
    public static final String update_category = "/{id}";           // PUT    /api/categories/{id}
    public static final String delete_category = "/{id}";           // DELETE /api/categories/{id}
}
