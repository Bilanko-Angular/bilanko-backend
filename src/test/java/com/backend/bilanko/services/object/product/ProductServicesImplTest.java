package com.backend.bilanko.services.object.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.backend.bilanko.DTO.object.product.ProductDTO;
import com.backend.bilanko.models.object.product.Product;
import com.backend.bilanko.models.person.Role;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.repository.product.CategoryRepository;
import com.backend.bilanko.repository.product.ProductRepository;
import com.backend.bilanko.services.person.UserServices;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProductServicesImplTest {

    @Mock
    private ProductRepository repo;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserServices userServices;

    @InjectMocks
    private ProductServicesImpl productService;

    // ── Helpers pour construire les entités via le Builder ──────────────────

    private User buildUser(String email) {
        return User.builder()
                .id(1L)
                .name("Test")
                .subname("User")
                .email(email)
                .password("secret")
                .role(Role.MERCHANT)
                .build();
    }

    private Product buildProduct(Long id, String name, double price, User owner) {
        return Product.builder()
                .id(id)
                .name(name)
                .quantity(5)
                .price(price)
                .purchasePrice(0.0)
                .categories(List.of())
                .user(owner)
                .build();
    }

    // ── RULE 1 : CREATION ────────────────────────────────────────────────────

    @Test
    void create_WithValidMerchant_ShouldReturnCreatedProduct() {
        // Arrange
        String email = "merchant@email.com";
        User merchant = buildUser(email);

        // ProductDTO(name, quantity, price, idCategories, purchasePrice)
        ProductDTO inputDTO = new ProductDTO("New Product", 5, 10, 29.99, Optional.empty(), 15.00);

        when(userServices.findUserByEmail(email)).thenReturn(merchant);
        // Renvoie l'objet exact passé à save()
        when(repo.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Product result = productService.create(inputDTO, email);

        // Assert
        assertNotNull(result);
        assertEquals("New Product", result.getName());
        assertEquals(29.99, result.getPrice());
        assertEquals(merchant, result.getUser());
        assertNotNull(result.getReference());
        assertNotNull(result.getCreatedAt());
        assertEquals(5, result.getAlertThreshold());
    }

    @Test
    void create_WithNonMerchant_ShouldThrowException() {
        // Arrange
        String email = "regular@email.com";
        // Simule un user introuvable → findUserByEmail lève une exception
        when(userServices.findUserByEmail(email))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        ProductDTO inputDTO = new ProductDTO("Any Product", null, 1, 9.99, Optional.empty(), 5.00);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                productService.create(inputDTO, email)
        );
    }

    // ── RULE 2 : UPDATE ──────────────────────────────────────────────────────

    @Test
    void update_AsMerchantOwner_ShouldUpdateAndReturnProduct() {
        // Arrange
        long productId = 1L;
        String email = "merchant@email.com";
        User merchant = buildUser(email);

        Product existingProduct = buildProduct(productId, "Old Name", 10.00, merchant);

        ProductDTO updateDTO = new ProductDTO("Updated Product", 2, 3, 14.99, Optional.empty(), 8.00);

        when(userServices.findUserByEmail(email)).thenReturn(merchant);
        when(repo.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(repo.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Product result = productService.update(productId, updateDTO, email);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals(14.99, result.getPrice(), 0.01);
        assertEquals(2, result.getAlertThreshold());
    }

    @Test
    void update_AsNonOwner_ShouldThrowForbidden() {
        // Arrange
        long productId = 1L;
        String ownerEmail  = "owner@email.com";
        String otherEmail  = "other@email.com";

        User owner = buildUser(ownerEmail);
        Product existingProduct = buildProduct(productId, "My Product", 10.00, owner);

        ProductDTO updateDTO = new ProductDTO("Hacked Product", null, 1, 1.00, Optional.empty(), 0.50);

        when(repo.findById(productId)).thenReturn(Optional.of(existingProduct));

        // Act & Assert — checkOwnership doit lever 403
        assertThrows(ResponseStatusException.class, () ->
                productService.update(productId, updateDTO, otherEmail)
        );
    }

    // ── SEARCH ───────────────────────────────────────────────────────────────

    @Test
    void findMyProducts_ShouldReturnMerchantProducts() {
        // Arrange
        String email = "merchant@email.com";
        User merchant = buildUser(email);

        Product p1 = buildProduct(1L, "Product 1", 19.99, merchant);

        when(userServices.findUserByEmail(email)).thenReturn(merchant);
        when(repo.findByUserEmail(email)).thenReturn(List.of(p1));

        // Act
        List<Product> products = productService.findMyProducts(email);

        // Assert
        assertNotNull(products);
        assertEquals(1, products.size());
        assertEquals("Product 1", products.get(0).getName());
    }
}