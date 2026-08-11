package com.backend.bilanko.controller.object.product;

import com.backend.bilanko.DTO.object.product.CategoryDTO;
import com.backend.bilanko.models.object.product.Category;
import com.backend.bilanko.services.object.product.CategoryServices;
import com.backend.bilanko.utils.constant.CategoryApiRoutes;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(CategoryApiRoutes.category)
@AllArgsConstructor
public class CategoryController {
    private final CategoryServices categoryServices;

    // ── CREATE ─────────────────────────────────────────────────────────────
    // POST /api/categories/create  →  ADMIN uniquement
    @PostMapping(CategoryApiRoutes.create_category)
    public ResponseEntity<Category> create(
            @RequestBody CategoryDTO categoryDTO,
            Authentication authentication) {

        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryServices.create(categoryDTO, email));
    }

    // ── READ ───────────────────────────────────────────────────────────────
    // GET /api/categories/all  →  public (catalogue)
    @GetMapping(CategoryApiRoutes.find_all)
    public ResponseEntity<List<Category>> findAll() {
        return ResponseEntity.ok(categoryServices.findAll());
    }

    // GET /api/categories/{id}
    @GetMapping(CategoryApiRoutes.find_by_id)
    public ResponseEntity<Category> findById(@PathVariable long id) {
        return ResponseEntity.ok(categoryServices.findById(id));
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────
    // PUT /api/categories/{id}  →  ADMIN uniquement
    @PutMapping(CategoryApiRoutes.update_category)
    public ResponseEntity<Category> update(
            @PathVariable long id,
            @RequestBody CategoryDTO categoryDTO,
            Authentication authentication) {

        String email = authentication.getName();
        return ResponseEntity.ok(categoryServices.update(id, categoryDTO, email));
    }

    // ── DELETE ─────────────────────────────────────────────────────────────
    // DELETE /api/categories/{id}  →  ADMIN uniquement
    @DeleteMapping(CategoryApiRoutes.delete_category)
    public ResponseEntity<Void> delete(
            @PathVariable long id,
            Authentication authentication) {

        String email = authentication.getName();
        categoryServices.delete(id, email);
        return ResponseEntity.noContent().build();  // 204 No Content
    }
}


