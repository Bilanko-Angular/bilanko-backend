package com.backend.bilanko.services.transaction;

import com.backend.bilanko.DTO.concept.transaction.SaleItemRequestDTO;
import com.backend.bilanko.DTO.concept.transaction.SaleRequestDTO;
import com.backend.bilanko.DTO.concept.transaction.SaleResponseDTO;
import com.backend.bilanko.models.object.product.Product;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.models.transaction.Sale;
import com.backend.bilanko.models.transaction.SaleItem;

// ^ ajuste ce dernier import pour qu'il corresponde à ta vraie ProductRepository

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

        List<SaleItem> items = new ArrayList<>();
        double totalAmount = 0;
        double totalMargin = 0;

        for (SaleItemRequestDTO itemDto : dto.items()) {
            Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Produit introuvable : id=" + itemDto.productId()
                    ));

            // Un produit appartient à un seul marchand : on vérifie que c'est bien le sien
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

            // Prix de vente : celui fourni, sinon le prix catalogue par défaut
            double unitSellingPrice = itemDto.unitSellingPrice() != null
                    ? itemDto.unitSellingPrice()
                    : product.getPrice();

            // Snapshot du prix d'achat au moment de la vente, pour figer la marge
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

            // Décrémente le stock immédiatement
            product.setQuantity(product.getQuantity() - itemDto.quantity());
            productRepository.save(product);
        }

        sale.setItems(items);
        sale.setTotalAmount(totalAmount);
        sale.setTotalMargin(totalMargin);

        Sale saved = saleRepository.save(sale);
        return com.backend.bilanko.mapper.SaleMapper.toDto(saved);
    }

    public List<SaleResponseDTO> getAllSales(User currentUser) {
        return saleRepository.findByUserOrderBySaleDateDesc(currentUser)
                .stream()
                .map(com.backend.bilanko.mapper.SaleMapper::toDto)
                .toList();
    }

    public SaleResponseDTO getSaleById(long id, User currentUser) {
        Sale sale = saleRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Vente introuvable : id=" + id
                ));
        return com.backend.bilanko.mapper.SaleMapper.toDto(sale);
    }
}