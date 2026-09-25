package com.backend.bilanko.services.person.admin;

import com.backend.bilanko.DTO.person.admin.AdminUserCreateRequest;
import com.backend.bilanko.DTO.person.admin.AdminUserResponseDTO;
import com.backend.bilanko.DTO.person.admin.AdminUserSummaryDTO;
import com.backend.bilanko.DTO.person.admin.AdminUserUpdateRequest;
import com.backend.bilanko.models.person.user.Role;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.repository.object.product.ProductRepository;
import com.backend.bilanko.repository.person.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    private void verifyAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        if (user.getRole() != Role.ADMIN) {
            throw new org.springframework.security.access.AccessDeniedException("Accès refusé. Vous n'êtes pas administrateur.");
        }
    }

    private AdminUserResponseDTO mapToDTO(User user) {
        int numberOfProducts = productRepository.countByUser_Id(user.getId());
        return AdminUserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .subname(user.getSubname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .adresse(user.getAdresse())
                .active(user.isActive())
                .lastConnectionDate(user.getLastConnectionDate())
                .numberOfProducts(numberOfProducts)
                .build();
    }

    @Override
    public AdminUserSummaryDTO getUsersSummary(String adminEmail) {
        verifyAdmin(adminEmail);
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActive(true);
        long blockedUsers = userRepository.countByActive(false);
        
        // Utilisateurs créés ce mois-ci
        ZonedDateTime startOfMonth = ZonedDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        Instant startOfMonthInstant = startOfMonth.toInstant();
        long newUsersThisMonth = userRepository.countByCreatedAtAfter(startOfMonthInstant);

        return AdminUserSummaryDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .blockedUsers(blockedUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .build();
    }

    @Override
    public Page<AdminUserResponseDTO> searchUsers(String adminEmail, String keyword, Boolean active, Role role, int page, int size) {
        verifyAdmin(adminEmail);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> usersPage = userRepository.searchUsers(keyword, role, active, pageable);
        return usersPage.map(this::mapToDTO);
    }

    @Override
    public AdminUserResponseDTO createUser(String adminEmail, AdminUserCreateRequest request) {
        verifyAdmin(adminEmail);
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        User newUser = User.builder()
                .name(request.getName())
                .subname(request.getSubname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.MERCHANT)
                .active(true)
                .build();

        userRepository.save(newUser);
        return mapToDTO(newUser);
    }

    @Override
    public AdminUserResponseDTO updateUser(String adminEmail, Long targetUserId, AdminUserUpdateRequest request) {
        verifyAdmin(adminEmail);

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'id : " + targetUserId));

        if (!targetUser.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé par un autre utilisateur");
        }

        targetUser.setName(request.getName());
        targetUser.setSubname(request.getSubname());
        targetUser.setEmail(request.getEmail());
        targetUser.setPhoneNumber(request.getPhoneNumber());
        targetUser.setAdresse(request.getAdresse());

        userRepository.save(targetUser);
        return mapToDTO(targetUser);
    }

    @Override
    public AdminUserResponseDTO updateUserStatus(String adminEmail, Long targetUserId, boolean active) {
        verifyAdmin(adminEmail);

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'id : " + targetUserId));

        if (targetUser.getEmail().equals(adminEmail)) {
            throw new IllegalArgumentException("Un administrateur ne peut pas modifier son propre statut via cette route.");
        }

        targetUser.setActive(active);
        userRepository.save(targetUser);
        return mapToDTO(targetUser);
    }

    @Override
    public void deleteUser(String adminEmail, Long targetUserId) {
        verifyAdmin(adminEmail);

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'id : " + targetUserId));
                
        if (targetUser.getEmail().equals(adminEmail)) {
            throw new IllegalArgumentException("Un administrateur ne peut pas se supprimer lui-même.");
        }

        userRepository.delete(targetUser);
    }

    @Override
    public Page<AdminUserResponseDTO> getPagedUsers(String adminEmail, int page, int size) {
        verifyAdmin(adminEmail);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    public List<AdminUserResponseDTO> getAllUsers(String adminEmail) {
        verifyAdmin(adminEmail);
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}
