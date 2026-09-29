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
@Builder
public class ProductCategory extends BaseCategoryModel {
    @ManyToMany
    private List<Product> products;

}
