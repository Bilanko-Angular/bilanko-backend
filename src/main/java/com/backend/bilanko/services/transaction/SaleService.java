package com.backend.bilanko.services.transaction;

import com.backend.bilanko.DTO.concept.transaction.SaleItemRequestDTO;
import com.backend.bilanko.DTO.concept.transaction.SaleRequestDTO;
import com.backend.bilanko.DTO.concept.transaction.SaleResponseDTO;
import com.backend.bilanko.DTO.concept.transaction.SaleSummaryDTO;
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
import java.util.ArrayList;
import java.util.List;

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

    private List<Sale> findSales(User currentUser, LocalDateTime from, LocalDateTime to) {
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
