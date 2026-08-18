package com.backend.bilanko.services.object.product;

import com.backend.bilanko.DTO.object.product.CategoryDTO;
import com.backend.bilanko.models.object.product.Category;
import com.backend.bilanko.models.object.product.Product;
import com.backend.bilanko.models.person.Role;
import com.backend.bilanko.repository.product.CategoryRepository;
import com.backend.bilanko.repository.product.ProductRepository;
import com.backend.bilanko.services.person.UserServices;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@AllArgsConstructor
@Service
public class CategoryServicesImpl implements CategoryServices {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserServices userServices;

    // ── CREATE ──────────────────────────────────────────────────────────────
    @Override
    public Category create(CategoryDTO categoryDTO, String email) {
        requireAdmin(email);
        return categoryRepository.save(buildCategory(categoryDTO));
    }

    // ── READ ─────────────────────────────────────────────────────────────────
    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category findById(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Catégorie introuvable : id=" + id));
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    @Override
    public Category update(long id, CategoryDTO categoryDTO, String email) {
        requireAdmin(email);
        Category existing = findById(id);
        List<Product> products = productRepository.findAllById(categoryDTO.idProducts());
        existing.setName(categoryDTO.name());
        existing.setProducts(products);
        return categoryRepository.save(existing);
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    @Override
    public void delete(long id, String email) {
        requireAdmin(email);
        Category existing = findById(id);
        categoryRepository.delete(existing);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private void requireAdmin(String email) {
        if (!userServices.confirmRoleByEmail(Role.ADMIN, email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé : rôle ADMIN requis");
        }
    }

    private Category buildCategory(CategoryDTO categoryDTO) {
        List<Product> products = productRepository.findAllById(categoryDTO.idProducts());
        return Category.builder()
                .name(categoryDTO.name())
                .products(products)
                .build();
    }
}

