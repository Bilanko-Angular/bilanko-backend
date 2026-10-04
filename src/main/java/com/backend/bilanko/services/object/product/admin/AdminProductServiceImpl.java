package com.backend.bilanko.services.object.product.admin;

import com.backend.bilanko.DTO.object.product.admin.AdminProductCreateRequest;
import com.backend.bilanko.DTO.object.product.admin.AdminProductResponseDTO;
import com.backend.bilanko.DTO.object.product.admin.AdminProductSummaryDTO;
import com.backend.bilanko.DTO.object.product.admin.AdminProductUpdateRequest;
import com.backend.bilanko.mapper.ProductMapper;
import com.backend.bilanko.models.concept.category.ProductCategory;
import com.backend.bilanko.models.object.product.Product;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.repository.concept.category.ProductCategoryRepository;
import com.backend.bilanko.repository.object.product.ProductRepository;
import com.backend.bilanko.repository.person.UserRepository;
import com.backend.bilanko.utils.annotation.AdminOnly;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductCategoryRepository productCategoryRepository;

    private String generateReference(String name, long userId) {
        String prefix = name.length() >= 3 ? name.substring(0, 3).toUpperCase() : name.toUpperCase();
        long timestamp = System.currentTimeMillis();
        return prefix + "-" + userId + "-" + timestamp;
    }

    private List<ProductCategory> resolveCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }
        List<ProductCategory> categories = productCategoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une ou plusieurs catégories sont introuvables");
        }
        return categories;
    }

    @Override
    @Transactional(readOnly = true)
    @AdminOnly
    public AdminProductSummaryDTO getSummary() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        Instant startInstant = startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant();

        Double averagePrice = productRepository.averageProductPrice();
        if (averagePrice == null) {
            averagePrice = 0.0;
        }

        return AdminProductSummaryDTO.builder()
                .totalCount(productRepository.count())
                .addedThisMonthCount(productRepository.countByCreatedAtGreaterThanEqual(startInstant))
                .associatedWithSaleCount(productRepository.countAssociatedWithSale())
                .averagePrice(averagePrice)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @AdminOnly
    public Page<AdminProductResponseDTO> getPagedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productRepository.findAll(pageable).map(ProductMapper::mapProductToAdminProductDTO);
    }

    @Override
    @Transactional(readOnly = true)
    @AdminOnly
    public Page<AdminProductResponseDTO> searchProducts(
            String keyword,
            Long categoryId,
            Double minPurchasePrice,
            Double maxPurchasePrice,
            Double minPrice,
            Double maxPrice,
            int page,
            int size) {

        if (keyword == null || keyword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le mot-clé de recherche est obligatoire");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productRepository.adminSearchProducts(
                        keyword.trim(),
                        categoryId,
                        minPurchasePrice,
                        maxPurchasePrice,
                        minPrice,
                        maxPrice,
                        pageable)
                .map(ProductMapper::mapProductToAdminProductDTO);
    }

    @Override
    @Transactional(readOnly = true)
    @AdminOnly
    public AdminProductResponseDTO getById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Produit introuvable : id=" + productId));
        return ProductMapper.mapProductToAdminProductDTO(product);
    }

    @Override
    @Transactional
    @AdminOnly
    public AdminProductResponseDTO createProduct(AdminProductCreateRequest request) {
        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Utilisateur introuvable : id=" + request.getUserId()));

        String name = request.getName().trim();
        List<ProductCategory> categories = resolveCategories(request.getCategoryIds());

        Product product = Product.builder()
                .name(name)
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .purchasePrice(request.getPurchasePrice())
                .alertThreshold(request.getAlertThreshold())
                .categories(categories)
                .user(targetUser)
                .reference(generateReference(name, targetUser.getId()))
                .build();

        return ProductMapper.mapProductToAdminProductDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    @AdminOnly
    public AdminProductResponseDTO updateProduct(Long productId, AdminProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Produit introuvable : id=" + productId));

        product.setName(request.getName().trim());
        product.setQuantity(request.getQuantity());
        product.setPrice(request.getPrice());
        product.setPurchasePrice(request.getPurchasePrice());
        product.setAlertThreshold(request.getAlertThreshold());
        product.setCategories(resolveCategories(request.getCategoryIds()));

        return ProductMapper.mapProductToAdminProductDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    @AdminOnly
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Produit introuvable : id=" + productId));
        productRepository.delete(product);
    }
}
