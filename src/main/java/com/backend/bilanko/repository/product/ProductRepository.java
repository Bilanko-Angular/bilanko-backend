package com.backend.bilanko.repository.product;

import com.backend.bilanko.models.object.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // Tous les produits appartenant à un user (via son email)
    List<Product> findByUserEmail(String email);
}

