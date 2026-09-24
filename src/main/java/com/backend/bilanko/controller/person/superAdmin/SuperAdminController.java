package com.backend.bilanko.controller.person.superAdmin;

import com.backend.bilanko.DTO.auth.AuthResponse;
import com.backend.bilanko.DTO.auth.RegisterRequest;
import com.backend.bilanko.services.person.superAdmin.SuperAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/super")
public class SuperAdminController {
    private final SuperAdminService superAdminService;

    @PostMapping("/create-admin")
    public ResponseEntity<?> createSuperAdmin(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody RegisterRequest registerRequest) throws Exception {

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid Authorization header");
        }

        String secret = authHeader.substring("Basic ".length()).trim();
        AuthResponse response = superAdminService.createAdmin(registerRequest, secret);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
