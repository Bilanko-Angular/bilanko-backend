package com.backend.bilanko.services.transaction;

import com.backend.bilanko.DTO.concept.transaction.SaleItemRequestDTO;
import com.backend.bilanko.DTO.concept.transaction.SaleRequestDTO;
import com.backend.bilanko.DTO.concept.transaction.SaleResponseDTO;
import com.backend.bilanko.DTO.concept.transaction.SaleSummaryDTO;
import com.backend.bilanko.DTO.concept.transaction.SaleTimeSeriesPointDTO;
import com.backend.bilanko.DTO.concept.transaction.TopSoldProductDTO;
import com.backend.bilanko.mapper.SaleMapper;
import com.backend.bilanko.models.object.product.Product;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.models.transaction.Sale;
import com.backend.bilanko.models.transaction.SaleItem;
import com.backend.bilanko.repository.concept.transaction.SaleRepository;
import com.backend.bilanko.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        List<Sale> sales = findSales(currentUser, from, to);

        Map<Long, Acc> byProduct = new LinkedHashMap<>();
        for (Sale sale : sales) {
            for (SaleItem item : sale.getItems()) {
                Product product = item.getProduct();
                Acc incoming = new Acc(
                        product.getId(),
                        product.getName(),
                        product.getReference(),
                        item.getQuantity(),
                        item.getUnitSellingPrice() * item.getQuantity(),
                        item.getMargin()
                );
                byProduct.merge(product.getId(), incoming, Acc::combine);
            }
        }

        Comparator<Acc> comparator = switch (resolvedSort) {
            case "revenue" -> Comparator.comparingDouble(Acc::revenue).reversed();
            case "margin" -> Comparator.comparingDouble(Acc::margin).reversed();
            default -> Comparator.comparingLong(Acc::quantity).reversed();
        };

        return byProduct.values().stream()
                .sorted(comparator)
                .limit(limit)
                .map(acc -> new TopSoldProductDTO(
                        acc.productId(),
                        acc.name(),
                        acc.reference(),
                        acc.quantity(),
                        acc.revenue(),
                        acc.margin()
                ))
                .toList();
    }

    private record Acc(long productId, String name, String reference, long quantity, double revenue, double margin) {
        Acc combine(Acc other) {
            return new Acc(
                    productId,
                    name,
                    reference,
                    quantity + other.quantity,
                    revenue + other.revenue,
                    margin + other.margin
            );
        }
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
}
