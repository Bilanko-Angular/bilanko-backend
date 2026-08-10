package com.backend.bilanko.models.object.product;

import com.backend.bilanko.models.person.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    private String name;
    @Min(0)
    private int quantity;
    @Min(0)
    private double price;

    @ManyToMany
    private List<Category> categories;

    @ManyToOne
    private User user;

}
