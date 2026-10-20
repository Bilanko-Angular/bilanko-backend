package com.backend.bilanko.services.transaction;

import com.backend.bilanko.DTO.summary.sale.SaleUserSummary;
import com.backend.bilanko.DTO.transaction.sale.SaleItemRequestDTO;
import com.backend.bilanko.DTO.transaction.sale.SaleRequestDTO;
import com.backend.bilanko.DTO.transaction.sale.SaleResponseDTO;
import com.backend.bilanko.DTO.summary.sale.SaleSummaryDTO;
import com.backend.bilanko.DTO.summary.overview.SaleTimeSeriesPointDTO;
import com.backend.bilanko.DTO.summary.overview.TopSoldProductDTO;
import com.backend.bilanko.mapper.SaleMapper;
import com.backend.bilanko.models.object.product.Product;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.models.transaction.Sale;
import com.backend.bilanko.models.transaction.SaleItem;
import com.backend.bilanko.repository.transaction.SaleRepository;
import com.backend.bilanko.repository.object.product.ProductRepository;
import com.backend.bilanko.services.person.user.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.backend.bilanko.services.person.notification.NotificationService;
import com.backend.bilanko.models.person.notification.NotificationType;

import com.backend.bilanko.DTO.shared.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    public final UserServices userServices;

    @Transactional
    public SaleResponseDTO createSale(SaleRequestDTO dto, User currentUser) {
        LocalDateTime saleDate = dto.saleDate() != null ? dto.saleDate() : LocalDateTime.now();

        Sale sale = Sale.builder()
                .saleDate(saleDate)
                .customerName(dto.customerName())
                .user(currentUser)
                .totalAmount(0)
                .totalMargin(0)
                .build();

        applyItems(sale, dto.items(), currentUser);

        Sale saved = saleRepository.save(sale);

        notificationService.createNotification(
                currentUser,
                NotificationType.NEW_SALE,
                "Nouvelle vente",
                String.format("Une vente de %.2f a été enregistrée%s.", 
                        saved.getTotalAmount(), 
                        (dto.customerName() != null && !dto.customerName().isBlank()) ? " pour " + dto.customerName() : ""),
                saved.getId()
        );

        return SaleMapper.toDto(saved);
    }

    public List<SaleResponseDTO> getAllSales(User currentUser, LocalDateTime from, LocalDateTime to) {
        return findSales(currentUser, from, to).stream()
                .map(SaleMapper::toDto)
                .toList();
    }

    public SaleResponseDTO getSaleById(long id, User currentUser) {
        return SaleMapper.toDto(findOwnedSale(id, currentUser));
    }

    public List<SaleResponseDTO> searchSales(String keyword, User currentUser) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return saleRepository.searchByKeyword(currentUser, keyword.trim()).stream()
                .map(SaleMapper::toDto)
                .toList();
    }

    public PageResponse<SaleResponseDTO> getAllSalesPaged(
            User currentUser, LocalDateTime from, LocalDateTime to, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "saleDate"));
        Page<Sale> result;
        if (from == null && to == null) {
            result = saleRepository.findByUserOrderBySaleDateDesc(currentUser, pageRequest);
        } else {
            if (from == null || to == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Les paramètres 'from' et 'to' doivent être fournis ensemble"
                );
            }
            if (from.isAfter(to)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La date 'from' ne peut pas être postérieure à 'to'"
                );
            }
            result = saleRepository.findByUserAndSaleDateBetweenOrderBySaleDateDesc(currentUser, from, to, pageRequest);
        }
        return PageResponse.from(result.map(SaleMapper::toDto));
    }

    public PageResponse<SaleResponseDTO> searchSalesPaged(
            String keyword, User currentUser, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return new PageResponse<>(List.of(), page, size, 0, 0);
        }
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Sale> result = saleRepository.searchByKeywordPaged(currentUser, keyword.trim(), pageRequest);
        return PageResponse.from(result.map(SaleMapper::toDto));
    }

    public SaleSummaryDTO getSummary(User currentUser, LocalDateTime from, LocalDateTime to) {
        List<Sale> sales = findSales(currentUser, from, to);

        double totalAmount = sales.stream().mapToDouble(Sale::getTotalAmount).sum();
        double totalMargin = sales.stream().mapToDouble(Sale::getTotalMargin).sum();

        return new SaleSummaryDTO(sales.size(), totalAmount, totalMargin, from, to);
    }

    public List<SaleTimeSeriesPointDTO> getTimeSeries(
            User currentUser,
            LocalDateTime from,
            LocalDateTime to,
            String granularity
    ) {
        String resolved = resolveGranularity(granularity);
        DateTimeFormatter formatter = "month".equals(resolved)
                ? DateTimeFormatter.ofPattern("yyyy-MM")
                : DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<Sale> sales = findSales(currentUser, from, to);
        Map<String, List<Sale>> grouped = sales.stream()
                .sorted(Comparator.comparing(Sale::getSaleDate))
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleDate().format(formatter),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return grouped.entrySet().stream()
                .map(entry -> {
                    List<Sale> periodSales = entry.getValue();
                    double totalAmount = periodSales.stream().mapToDouble(Sale::getTotalAmount).sum();
                    double totalMargin = periodSales.stream().mapToDouble(Sale::getTotalMargin).sum();
                    return new SaleTimeSeriesPointDTO(
                            entry.getKey(),
                            periodSales.size(),
                            totalAmount,
                            totalMargin
                    );
                })
                .toList();
    }

    public List<TopSoldProductDTO> getTopProducts(
            User currentUser,
            LocalDateTime from,
            LocalDateTime to,
            int limit,
            String sortBy
    ) {
        if (limit < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le paramètre 'limit' doit être au moins 1");
        }

        String resolvedSort = resolveTopSort(sortBy);
        // On laisse la base faire le tri et la limitation : 1 seule requête SQL
        Pageable pageable = PageRequest.of(0, limit);

        if (from == null && to == null) {
            return switch (resolvedSort) {
                case "revenue" -> saleRepository.findTopProductsByRevenue(currentUser, pageable);
                case "margin"  -> saleRepository.findTopProductsByMargin(currentUser, pageable);
                default        -> saleRepository.findTopProductsByQuantity(currentUser, pageable);
            };
        }

        if (from == null || to == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Les paramètres 'from' et 'to' doivent être fournis ensemble"
            );
        }
        if (from.isAfter(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La date 'from' ne peut pas être postérieure à 'to'"
            );
        }

        return switch (resolvedSort) {
            case "revenue" -> saleRepository.findTopProductsByRevenueAndPeriod(currentUser, from, to, pageable);
            case "margin"  -> saleRepository.findTopProductsByMarginAndPeriod(currentUser, from, to, pageable);
            default        -> saleRepository.findTopProductsByQuantityAndPeriod(currentUser, from, to, pageable);
        };
    }

    @Transactional
    public SaleResponseDTO updateSale(long id, SaleRequestDTO dto, User currentUser) {
        Sale sale = findOwnedSale(id, currentUser);

        restoreStock(sale);
        sale.getItems().clear();

        sale.setSaleDate(dto.saleDate() != null ? dto.saleDate() : sale.getSaleDate());
        sale.setCustomerName(dto.customerName());

        applyItems(sale, dto.items(), currentUser);

        Sale saved = saleRepository.save(sale);
        return SaleMapper.toDto(saved);
    }

    @Transactional
    public void deleteSale(long id, User currentUser) {
        Sale sale = findOwnedSale(id, currentUser);
        restoreStock(sale);
        saleRepository.delete(sale);
    }

    List<Sale> findSales(User currentUser, LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) {
            return saleRepository.findByUserOrderBySaleDateDesc(currentUser);
        }
        if (from == null || to == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Les paramètres 'from' et 'to' doivent être fournis ensemble"
            );
        }
        if (from.isAfter(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La date 'from' ne peut pas être postérieure à 'to'"
            );
        }
        return saleRepository.findByUserAndSaleDateBetweenOrderBySaleDateDesc(currentUser, from, to);
    }

    private String resolveGranularity(String granularity) {
        if (granularity == null || granularity.isBlank() || "day".equalsIgnoreCase(granularity)) {
            return "day";
        }
        if ("month".equalsIgnoreCase(granularity)) {
            return "month";
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Le paramètre 'granularity' doit être 'day' ou 'month'"
        );
    }

    private String resolveTopSort(String sortBy) {
        if (sortBy == null || sortBy.isBlank() || "quantity".equalsIgnoreCase(sortBy)) {
            return "quantity";
        }
        if ("revenue".equalsIgnoreCase(sortBy) || "margin".equalsIgnoreCase(sortBy)) {
            return sortBy.toLowerCase();
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Le paramètre 'sortBy' doit être 'quantity', 'revenue' ou 'margin'"
        );
    }

    private Sale findOwnedSale(long id, User currentUser) {
        return saleRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Vente introuvable : id=" + id
                ));
    }

    private void restoreStock(Sale sale) {
        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private void applyItems(Sale sale, List<SaleItemRequestDTO> itemDtos, User currentUser) {
        List<SaleItem> items = new ArrayList<>();
        double totalAmount = 0;
        double totalMargin = 0;

        for (SaleItemRequestDTO itemDto : itemDtos) {
            Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Produit introuvable : id=" + itemDto.productId()
                    ));

            if (product.getUser() == null || product.getUser().getId() != currentUser.getId()) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Ce produit n'appartient pas à votre commerce : id=" + product.getId()
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
    public SaleUserSummary getSummary(String email){
        User user = userServices.findUserByEmail(email);
        // 1. Total des ventes
        long totalSales = saleRepository.countByUser(user);

        // 2. Ventes du mois en cours
        LocalDateTime startOfMonth = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay();

        LocalDateTime endOfMonth = LocalDate.now()
                .with(TemporalAdjusters.lastDayOfMonth())
                .atTime(LocalTime.MAX);

        long monthlySales = saleRepository.countByUserAndSaleDateBetween(user, startOfMonth, endOfMonth);

        // 3. Montant total des marges
        double totalMargin = saleRepository.sumTotalMarginByUser(user);

        // 4. Meilleur client (LIMIT 1 via PageRequest)
        String bestCustomer = saleRepository.findTopCustomerBySalesCount(user, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse("Aucun client enregistre");

        return SaleUserSummary.builder()
                .bestCustomer(bestCustomer)
                .monthlySales(monthlySales)
                .totalMargin(totalMargin)
                .totalSales(totalSales)
                .build();
    }
}
