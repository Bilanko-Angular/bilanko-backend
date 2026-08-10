package com.backend.bilanko.controller.object.product;

import com.backend.bilanko.DTO.object.product.ProductDTO;
import com.backend.bilanko.models.object.product.Product;
import com.backend.bilanko.services.object.product.ProductServices;
import com.backend.bilanko.utils.constant.ProductApiRoutes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ProductApiRoutes.PRODUCT8)
public class ProductController {

    private final ProductServices productServices;

    public ProductController(ProductServices productServices) {
        this.productServices = productServices;
    }

    // ── CREATE ─────────────────────────────────────────────────────────────
    // POST /api/products/create
    @PostMapping(ProductApiRoutes.PRODUCTS_CREATE)
    public ResponseEntity<Product> create(
            @RequestBody ProductDTO productDTO,
            Authentication authentication) {

        String email = authentication.getName();
        Product created = productServices.create(productDTO, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ── READ ───────────────────────────────────────────────────────────────
    // GET /api/products/all  →  tous les produits (catalogue public)
    @GetMapping(ProductApiRoutes.PRODUCTS_ALL)
    public ResponseEntity<List<Product>> findAll() {
        return ResponseEntity.ok(productServices.findAll());
    }

    // GET /api/products/my  →  uniquement les produits du user connecté
    @GetMapping(ProductApiRoutes.PRODUCTS_MY)
    public ResponseEntity<List<Product>> findMyProducts(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(productServices.findMyProducts(email));
    }

    // GET /api/products/{id}  →  un produit précis par ID
    @GetMapping(ProductApiRoutes.PRODUCTS_BY_ID)
    public ResponseEntity<Product> findById(@PathVariable long id) {
        return ResponseEntity.ok(productServices.findById(id));
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────
    // PUT /api/products/{id}  →  seul le propriétaire peut modifier
    @PutMapping(ProductApiRoutes.PRODUCTS_UPDATE)
    public ResponseEntity<Product> update(
            @PathVariable long id,
            @RequestBody ProductDTO productDTO,
            Authentication authentication) {

        String email = authentication.getName();
        Product updated = productServices.update(id, productDTO, email);
        return ResponseEntity.ok(updated);
    }

    // ── DELETE ─────────────────────────────────────────────────────────────
    // DELETE /api/products/{id}  →  seul le propriétaire peut supprimer
    @DeleteMapping(ProductApiRoutes.PRODUCTS_DELETE)
    public ResponseEntity<Void> delete(
            @PathVariable long id,
            Authentication authentication) {

        String email = authentication.getName();
        productServices.delete(id, email);
        return ResponseEntity.noContent().build();  // 204 No Content
    }
}

