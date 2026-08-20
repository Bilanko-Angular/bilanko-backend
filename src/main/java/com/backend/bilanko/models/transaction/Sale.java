package com.backend.bilanko.models.transaction;

import com.backend.bilanko.models.person.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private LocalDateTime saleDate;

    // Optionnel : pas d'entité Client dédiée en V1
    private String customerName;

    @Column(nullable = false)
    private double totalAmount;

    @Column(nullable = false)
    private double totalMargin;

    @ManyToOne(optional = false)
    private User user;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SaleItem> items = new ArrayList<>();
}
