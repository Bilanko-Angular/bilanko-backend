package com.backend.bilanko.services.object.product;

import com.backend.bilanko.DTO.object.product.CategoryDTO;
import com.backend.bilanko.models.object.product.Category;
import com.backend.bilanko.models.object.product.Product;
import com.backend.bilanko.models.person.Role;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.repository.UserRepository;
import com.backend.bilanko.repository.product.CategoryRepository;
import com.backend.bilanko.repository.product.ProductRepository;
import com.backend.bilanko.services.person.UserServices;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class CategoryServicesImpl implements CategoryServices {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserServices userServices;
    @Override
    public Category create(CategoryDTO categoryDTO, String email) {
        if (!userServices.confirmRoleByEmail( Role.ADMIN,email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé : rôle ADMIN requis");
        }
        return categoryRepository.save(buildCategory(categoryDTO));
    }

    private Category buildCategory(CategoryDTO categoryDTO){
        List<Product> products=categoryDTO.idProducts()
                .map(productRepository::findAllById)
                .orElseGet(List::of);
        return Category.builder()
                .name(categoryDTO.name())
                .products(products)
                .build();
    }
}
