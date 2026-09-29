package com.backend.bilanko.models.concept.category;

import com.backend.bilanko.models.object.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "category")
@Builder
public class ProductCategory extends BaseCategoryModel {
    @ManyToMany
    @JoinTable(
            name = "product_category_product",
            joinColumns = @JoinColumn(name = "product_category_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;

}
