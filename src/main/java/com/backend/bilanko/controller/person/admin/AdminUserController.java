package com.backend.bilanko.controller.person.admin;

import com.backend.bilanko.DTO.person.admin.AdminUserCreateRequest;
import com.backend.bilanko.DTO.person.admin.AdminUserResponseDTO;
import com.backend.bilanko.DTO.person.admin.AdminUserSummaryDTO;
import com.backend.bilanko.DTO.person.admin.AdminUserUpdateRequest;
import com.backend.bilanko.models.person.user.Role;
import com.backend.bilanko.services.person.admin.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/summary")
    public ResponseEntity<AdminUserSummaryDTO> getSummary() {
        return ResponseEntity.ok(adminUserService.getUsersSummary(currentAdminEmail()));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AdminUserResponseDTO>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminUserService.searchUsers(currentAdminEmail(), keyword, active, role, page, size));
    }

    @PostMapping
    public ResponseEntity<AdminUserResponseDTO> createUser(@Valid @RequestBody AdminUserCreateRequest request) {
        return ResponseEntity.ok(adminUserService.createUser(currentAdminEmail(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminUserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        return ResponseEntity.ok(adminUserService.updateUser(currentAdminEmail(), id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminUserResponseDTO> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(adminUserService.updateUserStatus(currentAdminEmail(), id, active));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(currentAdminEmail(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<AdminUserResponseDTO>> getPagedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminUserService.getPagedUsers(currentAdminEmail(), page, size));
    }

    @GetMapping("/all")
    public ResponseEntity<List<AdminUserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers(currentAdminEmail()));
    }

    // --- Utilitaires privés ---

    private String currentAdminEmail() {
        return Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
    }
}
