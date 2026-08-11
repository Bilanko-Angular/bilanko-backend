package com.backend.bilanko.services.object.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.backend.bilanko.DTO.object.product.CategoryDTO;
import com.backend.bilanko.models.object.product.Category;
import com.backend.bilanko.models.object.product.Product;
import com.backend.bilanko.models.person.Role;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.repository.product.CategoryRepository;
import com.backend.bilanko.repository.product.ProductRepository;
import com.backend.bilanko.services.person.UserServices;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

/**
 * Unit tests for CategoryServiceImpl.
 * Verifies business rules:
 *  - Product belongs to a single merchant.
 *  - Only merchants and admins can perform operations on products/categories.
 *  - Product-category bidirectionality (0* relationships).
 */
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserServices userServices;

    private CategoryServicesImpl categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        categoryService = new CategoryServicesImpl(productRepository,categoryRepository ,userServices);

        User merchantUser = new User();
        merchantUser.setEmail("merchant@email.com");
        User adminUser = new User();
        adminUser.setEmail("admin@email.com");
        User regularUser = new User();
        regularUser.setEmail("regular@email.com");
        
        when(userServices.findUserByEmail(anyString())).thenReturn(merchantUser);
        when(userServices.confirmRoleByEmail(eq(Role.MERCHANT), anyString())).thenReturn(true);
    }

    // Business Rule 1: Product belongs to a single merchant
    @Test
    void testCreateCategory_Merchant_Failure() {
        CategoryDTO dto = new CategoryDTO("New Category", Optional.empty());
        when(userServices.confirmRoleByEmail(eq(Role.ADMIN), anyString())).thenReturn(false);

        assertThrows(ResponseStatusException.class, () ->
                categoryService.create(dto, "merchant@email.com"));
    }

    @Test
    void testCreateCategory_RegularUser_Failure() {
        when(userServices.confirmRoleByEmail(Role.ADMIN, "regular@email.com")).thenReturn(false);
        CategoryDTO dto = new CategoryDTO("Test", Optional.empty());

        assertThrows(Exception.class, () -> 
            categoryService.create(dto, "regular@email.com"));
    }

    // Business Rule 2: Only merchants and admins can perform operations
    @Test
    void testUpdateCategory_Admin_Success() {
        // 1. Given (Préparation des données)
        Long categoryId = 1L;
        String adminEmail = "admin@example.com";

        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Ancien Nom");

        CategoryDTO updateDto = new CategoryDTO("Nouveau Nom",null);

        // Mock du contrôle de rôle ADMIN (si requireAdmin est appelé)
        when(userServices.confirmRoleByEmail(eq(Role.ADMIN), eq(adminEmail))).thenReturn(true);

        // Mock de la recherche en base
        when(categoryRepository.findById(eq(categoryId))).thenReturn(Optional.of(existingCategory));

        // Mock de la sauvegarde (IMPORTANT : Mockito doit retourner l'entité sauvegardée)
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. When (Exécution)
        Category result = categoryService.update(categoryId, updateDto, adminEmail);

        // 3. Then (Vérifications)
        assertNotNull(result);
        assertEquals("Nouveau Nom", result.getName());
    }

    @Test
    void testUpdateCategory_RegularUser_Failure() {
        when(userServices.confirmRoleByEmail(Role.ADMIN, "regular@email.com")).thenReturn(false);
        CategoryDTO dto = new CategoryDTO("Test", Optional.empty());

        assertThrows(Exception.class, () -> 
            categoryService.update(1L, dto, "regular@email.com"));
    }

    @Test
    void testDeleteCategory_Admin_Success() {
        when(userServices.confirmRoleByEmail(Role.ADMIN, "admin@email.com")).thenReturn(true);
        Category toDelete = Category.builder()
                .id(1L)
                .name("ToDelete")
                .build();
        when(categoryRepository.findById(1L)).thenReturn(java.util.Optional.of(toDelete));

        categoryService.delete(1L, "admin@email.com");

        verify(categoryRepository, times(1)).delete(toDelete);
    }

    @Test
    void testDeleteCategory_RegularUser_Failure() {
        when(userServices.confirmRoleByEmail(Role.ADMIN, "regular@email.com")).thenReturn(false);
        Category toDelete = Category.builder()
                .id(1L)
                .name("ToDelete")
                .build();
        when(categoryRepository.findById(1L)).thenReturn(java.util.Optional.of(toDelete));

        assertThrows(Exception.class, () -> 
            categoryService.delete(1L, "regular@email.com"));
    }

    // Business Rule 3: Product‑Category relationships
    @Test
    void testCreateCategory_WithProducts() {
        Product product1 = Product.builder().id(1L).name("Product A").build();
        Product product2 = Product.builder().id(2L).name("Product B").build();

        when(userServices.confirmRoleByEmail(eq(Role.ADMIN), anyString())).thenReturn(true);
        when(productRepository.findAllById(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(product1, product2));

        CategoryDTO dto = new CategoryDTO("Category With Products", Optional.of(Arrays.asList(1L, 2L)));
        Category result = categoryService.create(dto, "admin@email.com");

        assertEquals(2, result.getProducts().size());
        assertTrue(result.getProducts().containsAll(Arrays.asList(product1, product2)));
    }

    @Test
    void testUpdateCategory_WithProducts() {
        Product existingProduct = Product.builder().id(3L).name("Existing Product").build();

        when(userServices.confirmRoleByEmail(eq(Role.ADMIN), anyString())).thenReturn(true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(Category.builder().build()));
        when(productRepository.findAllById(List.of(3L))).thenReturn(Collections.singletonList(existingProduct));

        CategoryDTO dto = new CategoryDTO("Updated With Product", Optional.of(List.of(3L)));
        Category result = categoryService.update(1L, dto, "admin@email.com");

        assertEquals(1, result.getProducts().size());
        assertEquals(existingProduct, result.getProducts().getFirst());
    }

    @Test
    void testFindAllCategories_ReturnsList() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(
                Category.builder().id(1L).name("Cat1").build(), 
                Category.builder().id(2L).name("Cat2").build()));

        List<Category> result = categoryService.findAll();

        assertEquals(2, result.size());
        assertEquals("Cat1", result.getFirst().getName());
    }

    @Test
    void testFindById_ExistingCategory() {
        Category mockedCategory = Category.builder()
                .id(1L)
                .name("Test Cat")
                .build();
        when(categoryRepository.findById(1L)).thenReturn(java.util.Optional.of(mockedCategory));

        Category result = categoryService.findById(1L);

        assertEquals(mockedCategory, result);
    }
}