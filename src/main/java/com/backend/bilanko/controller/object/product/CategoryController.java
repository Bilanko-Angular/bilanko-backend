package com.backend.bilanko.controller.object.product;

import com.backend.bilanko.DTO.object.product.CategoryDTO;
import com.backend.bilanko.models.object.product.Category;
import com.backend.bilanko.models.person.Role;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.repository.UserRepository;
import com.backend.bilanko.services.object.product.CategoryServices;
import com.backend.bilanko.services.person.UserServices;
import com.backend.bilanko.utils.constant.CategoryApiRoutes;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(CategoryApiRoutes.category)
@AllArgsConstructor
public class CategoryController {
    private final UserServices userServices;
    private final CategoryServices categoryServices;

    @PostMapping(CategoryApiRoutes.create_category)
    public ResponseEntity<Category> create(@RequestBody CategoryDTO categoryDTO, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryServices.create(categoryDTO, email));
    }
}

