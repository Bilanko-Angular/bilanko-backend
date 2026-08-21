package com.backend.bilanko.services.object.product;

import com.backend.bilanko.DTO.object.product.ProductDTO;
import com.backend.bilanko.models.object.product.Category;
import com.backend.bilanko.models.object.product.Product;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.repository.product.CategoryRepository;
import com.backend.bilanko.repository.product.ProductRepository;
import com.backend.bilanko.services.person.UserServices;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductServicesImpl implements ProductServices {

    public final ProductRepository repo;
    public final UserServices userServices;
    public final CategoryRepository categoryRepository;


    // ── CREATE ──────────────────────────────────────────────────

    @Override
    public Product create(ProductDTO productDTO, String email) {
        User user = userServices.findUserByEmail(email);
        Product product = buildProduct(productDTO, user);
        // Save first to obtain ID
        Product saved = repo.save(product);
        // Set reference and timestamps
        saved.setReference(generateReference(saved.getName(), saved.getId(), saved.getUser().getId()));
        saved.setCreatedAt(java.time.LocalDateTime.now());
        // Set alert threshold if provided
        if (productDTO.alertThreshold() != null) {
            saved.setAlertThreshold(productDTO.alertThreshold());
        }
        return repo.save(saved);
    }

    // ── READ ─────────────────────────────────────────────────────

    @Override
    public List<Product> findAll() {
        return repo.findAll();
    }

    @Override
    public List<Product> findMyProducts(String email) {
        // Vérifie que le user existe, puis retourne ses produits
        userServices.findUserByEmail(email);
        return repo.findByUserEmail(email);
    }

    @Override
    public Product findById(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produit introuvable : id=" + id));
    }

    // ── UPDATE ───────────────────────────────────────────────────

    @Override
    public Product update(long id, ProductDTO productDTO, String email) {
        Product existing = findById(id);

        // Seul le propriétaire peut modifier son produit
        checkOwnership(existing, email);

        List<Category> categories = productDTO.idCategories()
                .map(categoryRepository::findAllById)
                .orElseGet(List::of);

        existing.setName(productDTO.name());
        existing.setPrice(productDTO.price());
        existing.setPurchasePrice(productDTO.purchasePrice());
        existing.setQuantity(productDTO.quantity());
        existing.setCategories(categories);

        return repo.save(existing);
    }

    // ── DELETE ───────────────────────────────────────────────────

    @Override
    public void delete(long id, String email) {
        Product existing = findById(id);

        // Seul le propriétaire peut supprimer son produit
        checkOwnership(existing, email);

        repo.delete(existing);
    }

    // ── HELPERS ──────────────────────────────────────────────────

    /** Lève 403 si le user connecté n'est pas le propriétaire du produit. */
    private void checkOwnership(Product product, String email) {
        if (!product.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Vous n'êtes pas le propriétaire de ce produit");
        }
    }

    private Product buildProduct(ProductDTO dto, User user) {
        List<Category> categories = dto.idCategories()
                .map(categoryRepository::findAllById)
                .orElseGet(List::of);

        return Product.builder()
                .name(dto.name())
                .price(dto.price())
                .purchasePrice(dto.purchasePrice())
                .quantity(dto.quantity())
                .categories(categories)
                .user(user)
                .build();
    }
    private String generateReference(String name, long productId, long userId) {
        String prefix = name.length() >= 3 ? name.substring(0, 3).toUpperCase() : name.toUpperCase();
        return prefix + "-" + productId + "-" + userId;
    }
}


