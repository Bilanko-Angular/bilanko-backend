package com.backend.bilanko.services.transaction.admin;

import com.backend.bilanko.DTO.transaction.admin.AdminSaleCreateRequest;
import com.backend.bilanko.DTO.transaction.admin.AdminSaleResponseDTO;
import com.backend.bilanko.DTO.transaction.admin.AdminSaleSummaryDTO;
import com.backend.bilanko.DTO.transaction.admin.AdminSaleUpdateRequest;
import com.backend.bilanko.DTO.transaction.sale.SaleItemRequestDTO;
import com.backend.bilanko.mapper.SaleMapper;
import com.backend.bilanko.models.object.product.Product;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.models.transaction.Sale;
import com.backend.bilanko.models.transaction.SaleItem;
import com.backend.bilanko.repository.object.product.ProductRepository;
import com.backend.bilanko.repository.person.UserRepository;
import com.backend.bilanko.repository.transaction.SaleRepository;
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
    @AdminOnly
    public AdminSaleSummaryDTO getSummary() {

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
    @AdminOnly
    public Page<AdminSaleResponseDTO> getPagedSales(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "saleDate"));
        return saleRepository.findAll(pageable).map(SaleMapper::mapSaleToAdminSaleDTO);
    }

    @Override
    @AdminOnly
    public Page<AdminSaleResponseDTO> searchSales(String keyword, Integer minItems, Integer maxItems,
                                                   Double minAmount, Double maxAmount,
                                                   LocalDateTime startDate, LocalDateTime endDate,
                                                   int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        Page<AdminSaleResponseDTO> results = saleRepository.adminSearchSales(
                kw, minAmount, maxAmount, startDate, endDate, pageable
        ).map(SaleMapper::mapSaleToAdminSaleDTO);

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
    @AdminOnly
    public AdminSaleResponseDTO createSale(AdminSaleCreateRequest request) {

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
        return SaleMapper.mapSaleToAdminSaleDTO(saved);
    }

    @Override
    @Transactional
    @AdminOnly
    public AdminSaleResponseDTO updateSale(Long saleId, AdminSaleUpdateRequest request) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Vente introuvable : id=" + saleId));

        restoreStock(sale);
        sale.getItems().clear();

        sale.setSaleDate(request.getSaleDate() != null ? request.getSaleDate() : sale.getSaleDate());
        sale.setCustomerName(request.getCustomerName());

        applyItems(sale, request.getItems(), sale.getUser());

        Sale saved = saleRepository.save(sale);
        return SaleMapper.mapSaleToAdminSaleDTO(saved);
    }

    @Override
    @Transactional
    @AdminOnly
    public void deleteSale(Long saleId) {

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Vente introuvable : id=" + saleId));

        restoreStock(sale);
        saleRepository.delete(sale);
    }
}
