package com.backend.bilanko.services.transaction.admin;

import com.backend.bilanko.DTO.transaction.admin.AdminSaleCreateRequest;
import com.backend.bilanko.DTO.transaction.admin.AdminSaleResponseDTO;
import com.backend.bilanko.DTO.transaction.admin.AdminSaleSummaryDTO;
import com.backend.bilanko.DTO.transaction.admin.AdminSaleUpdateRequest;
import com.backend.bilanko.DTO.transaction.sale.SaleItemRequestDTO;
import com.backend.bilanko.DTO.transaction.sale.SaleItemResponseDTO;
import com.backend.bilanko.models.object.product.Product;
import com.backend.bilanko.models.person.user.Role;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.models.transaction.Sale;
import com.backend.bilanko.models.transaction.SaleItem;
import com.backend.bilanko.repository.object.product.ProductRepository;
import com.backend.bilanko.repository.person.UserRepository;
import com.backend.bilanko.repository.transaction.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSaleServiceImpl implements AdminSaleService {

    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // ---- Helpers ----

    private void verifyAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrateur introuvable"));
        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Accès refusé. Vous n'êtes pas administrateur.");
        }
    }

    private AdminSaleResponseDTO mapToDTO(Sale sale) {
        List<SaleItemResponseDTO> items = sale.getItems().stream()
                .map(item -> new SaleItemResponseDTO(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitSellingPrice(),
                        item.getUnitPurchasePrice(),
                        item.getMargin()
                ))
                .toList();

        return AdminSaleResponseDTO.builder()
                .id(sale.getId())
                .saleDate(sale.getSaleDate())
                .customerName(sale.getCustomerName())
                .totalAmount(sale.getTotalAmount())
                .totalMargin(sale.getTotalMargin())
                .itemCount(sale.getItems().size())
                .items(items)
                .userId(sale.getUser().getId())
                .userName(sale.getUser().getName())
                .userSubname(sale.getUser().getSubname())
                .build();
    }

    private void restoreStock(Sale sale) {
        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private void applyItems(Sale sale, List<SaleItemRequestDTO> itemDtos, User owner) {
        List<SaleItem> items = new ArrayList<>();
        double totalAmount = 0;
        double totalMargin = 0;

        for (SaleItemRequestDTO itemDto : itemDtos) {
            Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Produit introuvable : id=" + itemDto.productId()
                    ));

            if (product.getUser() == null || product.getUser().getId() != owner.getId()) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Le produit id=" + product.getId() + " n'appartient pas à l'utilisateur id=" + owner.getId()
                );
            }

            if (itemDto.quantity() > product.getQuantity()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Stock insuffisant pour '" + product.getName()
                                + "' (disponible: " + product.getQuantity()
                                + ", demandé: " + itemDto.quantity() + ")"
                );
            }

            double unitSellingPrice = itemDto.unitSellingPrice() != null
                    ? itemDto.unitSellingPrice()
                    : product.getPrice();
            double unitPurchasePrice = product.getPurchasePrice();
            double margin = (unitSellingPrice - unitPurchasePrice) * itemDto.quantity();

            SaleItem item = SaleItem.builder()
                    .sale(sale)
                    .product(product)
                    .quantity(itemDto.quantity())
                    .unitSellingPrice(unitSellingPrice)
                    .unitPurchasePrice(unitPurchasePrice)
                    .margin(margin)
                    .build();

            items.add(item);
            totalAmount += unitSellingPrice * itemDto.quantity();
            totalMargin += margin;

            product.setQuantity(product.getQuantity() - itemDto.quantity());
            productRepository.save(product);
        }

        sale.getItems().addAll(items);
        sale.setTotalAmount(totalAmount);
        sale.setTotalMargin(totalMargin);
    }

    // ---- Service Methods ----

    @Override
    public AdminSaleSummaryDTO getSummary(String adminEmail) {
        verifyAdmin(adminEmail);

        long totalCount = saleRepository.count();

        Double totalAmount = saleRepository.sumAllTotalAmount();
        if (totalAmount == null) totalAmount = 0.0;

        Double totalMargin = saleRepository.sumAllTotalMargin();
        if (totalMargin == null) totalMargin = 0.0;

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        Double currentMonthAmount = saleRepository.sumTotalAmountByPeriod(startOfMonth, endOfMonth);
        if (currentMonthAmount == null) currentMonthAmount = 0.0;

        return AdminSaleSummaryDTO.builder()
                .totalCount(totalCount)
                .totalAmount(totalAmount)
                .totalMargin(totalMargin)
                .currentMonthAmount(currentMonthAmount)
                .build();
    }

    @Override
    public Page<AdminSaleResponseDTO> getPagedSales(String adminEmail, int page, int size) {
        verifyAdmin(adminEmail);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "saleDate"));
        return saleRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    public Page<AdminSaleResponseDTO> searchSales(String adminEmail, String keyword, Integer minItems, Integer maxItems,
                                                   Double minAmount, Double maxAmount,
                                                   LocalDateTime startDate, LocalDateTime endDate,
                                                   int page, int size) {
        verifyAdmin(adminEmail);
        Pageable pageable = PageRequest.of(page, size);
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        Page<AdminSaleResponseDTO> results = saleRepository.adminSearchSales(
                kw, minAmount, maxAmount, startDate, endDate, pageable
        ).map(this::mapToDTO);

        // Filtre côté Java pour le nombre d'items (DISTINCT en JPQL rend le count complexe)
        if (minItems != null || maxItems != null) {
            int min = minItems != null ? minItems : 0;
            int max = maxItems != null ? maxItems : Integer.MAX_VALUE;
            List<AdminSaleResponseDTO> filtered = results.stream()
                    .filter(dto -> dto.getItemCount() >= min && dto.getItemCount() <= max)
                    .toList();
            return new org.springframework.data.domain.PageImpl<>(filtered, pageable, filtered.size());
        }

        return results;
    }

    @Override
    @Transactional
    public AdminSaleResponseDTO createSale(String adminEmail, AdminSaleCreateRequest request) {
        verifyAdmin(adminEmail);

        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Utilisateur introuvable : id=" + request.getUserId()));

        LocalDateTime saleDate = request.getSaleDate() != null ? request.getSaleDate() : LocalDateTime.now();

        Sale sale = Sale.builder()
                .saleDate(saleDate)
                .customerName(request.getCustomerName())
                .user(targetUser)
                .totalAmount(0)
                .totalMargin(0)
                .build();

        applyItems(sale, request.getItems(), targetUser);

        Sale saved = saleRepository.save(sale);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public AdminSaleResponseDTO updateSale(String adminEmail, Long saleId, AdminSaleUpdateRequest request) {
        verifyAdmin(adminEmail);

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Vente introuvable : id=" + saleId));

        restoreStock(sale);
        sale.getItems().clear();

        sale.setSaleDate(request.getSaleDate() != null ? request.getSaleDate() : sale.getSaleDate());
        sale.setCustomerName(request.getCustomerName());

        applyItems(sale, request.getItems(), sale.getUser());

        Sale saved = saleRepository.save(sale);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void deleteSale(String adminEmail, Long saleId) {
        verifyAdmin(adminEmail);

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Vente introuvable : id=" + saleId));

        restoreStock(sale);
        saleRepository.delete(sale);
    }
}
