package com.backend.bilanko.controller.concept.transaction;

import com.backend.bilanko.models.object.product.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    private Sale sale;

    @ManyToOne(optional = false)
    private Product product;

    @Min(1)
    @Column(nullable = false)
    private int quantity;

    // Prix de vente appliqué (pré-rempli depuis le catalogue, modifiable au cas par cas)
    @Min(0)
    @Column(nullable = false)
    private double unitSellingPrice;

    // Snapshot du prix d'achat au moment de la vente, pour figer la marge
    @Min(0)
    @Column(nullable = false)
    private double unitPurchasePrice;

    @Column(nullable = false)
    private double margin;
}
