package com.backend.bilanko.models.object.product;
import com.backend.bilanko.models.person.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;

import com.backend.bilanko.models.BaseEntity;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;
    @Min(0)
    private int quantity;
    @Min(0)
    private double price;
    @Min(0)
    private double purchasePrice;
    @ManyToMany
    private List<Category> categories;
    @ManyToOne
    @JsonIgnore
    private User user;
    // New fields
    @Column(nullable = false, unique = true)
    private String reference;
    private Integer alertThreshold;

}