package com.backend.bilanko.controller.object;
import com.backend.bilanko.models.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @PostMapping("/create")
    public ResponseEntity <Product> create(){

    }
}
