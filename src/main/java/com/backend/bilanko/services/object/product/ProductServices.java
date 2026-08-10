package com.backend.bilanko.services.object.product;

import com.backend.bilanko.DTO.object.product.ProductDTO;
import com.backend.bilanko.models.object.product.Product;

import java.util.List;

public interface ProductServices {

    // Créer un produit pour le user connecté
    Product create(ProductDTO productDTO, String email);

    // Tous les produits (admin / catalogue public)
    List<Product> findAll();

    // Produits du user connecté uniquement
    List<Product> findMyProducts(String email);

    // Un produit par son ID
    Product findById(long id);

    // Mettre à jour un produit (uniquement si le user en est propriétaire)
    Product update(long id, ProductDTO productDTO, String email);

    // Supprimer un produit (uniquement si le user en est propriétaire)
    void delete(long id, String email);
}

