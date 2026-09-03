package com.backend.bilanko.models.object.product;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import com.backend.bilanko.models.BaseEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Category extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @ManyToMany
    private List<Product> products;


}
