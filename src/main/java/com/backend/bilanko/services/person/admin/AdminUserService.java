package com.backend.bilanko.services.person.admin;

import com.backend.bilanko.DTO.person.admin.AdminUserCreateRequest;
import com.backend.bilanko.DTO.person.admin.AdminUserResponseDTO;
import com.backend.bilanko.DTO.person.admin.AdminUserSummaryDTO;
import com.backend.bilanko.DTO.person.admin.AdminUserUpdateRequest;
import com.backend.bilanko.models.person.user.Role;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdminUserService {
    AdminUserSummaryDTO getUsersSummary(String adminEmail);
    Page<AdminUserResponseDTO> searchUsers(String adminEmail, String keyword, Boolean active, Role role, int page, int size);
    AdminUserResponseDTO createUser(String adminEmail, AdminUserCreateRequest request);
    AdminUserResponseDTO updateUser(String adminEmail, Long targetUserId, AdminUserUpdateRequest request);
    AdminUserResponseDTO updateUserStatus(String adminEmail, Long targetUserId, boolean active);
    void deleteUser(String adminEmail, Long targetUserId);
    Page<AdminUserResponseDTO> getPagedUsers(String adminEmail, int page, int size);
    List<AdminUserResponseDTO> getAllUsers(String adminEmail);
}
