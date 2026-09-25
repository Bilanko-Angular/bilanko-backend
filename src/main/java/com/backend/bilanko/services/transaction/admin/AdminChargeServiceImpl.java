package com.backend.bilanko.services.transaction.admin;

import com.backend.bilanko.DTO.transaction.admin.AdminChargeCreateRequest;
import com.backend.bilanko.DTO.transaction.admin.AdminChargeResponseDTO;
import com.backend.bilanko.DTO.transaction.admin.AdminChargeSummaryDTO;
import com.backend.bilanko.DTO.transaction.admin.AdminChargeUpdateRequest;
import com.backend.bilanko.models.person.user.Role;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.models.transaction.Charge;
import com.backend.bilanko.models.transaction.ChargeCategory;
import com.backend.bilanko.repository.person.UserRepository;
import com.backend.bilanko.repository.transaction.ChargeCategoryRepository;
import com.backend.bilanko.repository.transaction.ChargeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AdminChargeServiceImpl implements AdminChargeService {

    private final ChargeRepository chargeRepository;
    private final UserRepository userRepository;
    private final ChargeCategoryRepository chargeCategoryRepository;

    private void verifyAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        if (user.getRole() != Role.ADMIN) {
            throw new org.springframework.security.access.AccessDeniedException("Accès refusé. Vous n'êtes pas administrateur.");
        }
    }

    private AdminChargeResponseDTO mapToDTO(Charge charge) {
        return AdminChargeResponseDTO.builder()
                .id(charge.getId())
                .date(charge.getDate())
                .createdAt(charge.getCreatedAt())
                .label(charge.getLabel())
                .supplier(charge.getSupplier())
                .amount(charge.getAmount())
                .categoryId(charge.getCategory() != null ? charge.getCategory().getId() : null)
                .categoryName(charge.getCategory() != null ? charge.getCategory().getName() : null)
                .userId(charge.getUser().getId())
                .userName(charge.getUser().getName())
                .userSubname(charge.getUser().getSubname())
                .build();
    }

    @Override
    public AdminChargeSummaryDTO getSummary(String adminEmail) {
        verifyAdmin(adminEmail);

        long totalCount = chargeRepository.count();
        Double totalSum = chargeRepository.sumAllCharges();
        if (totalSum == null) totalSum = 0.0;

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        Double currentMonthSum = chargeRepository.sumChargesByPeriod(startOfMonth, endOfMonth);
        if (currentMonthSum == null) currentMonthSum = 0.0;

        Double averagePrice = chargeRepository.averageChargeAmount();
        if (averagePrice == null) averagePrice = 0.0;

        return AdminChargeSummaryDTO.builder()
                .totalCount(totalCount)
                .totalSum(totalSum)
                .currentMonthSum(currentMonthSum)
                .averagePrice(averagePrice)
                .build();
    }

    @Override
    public Page<AdminChargeResponseDTO> getPagedCharges(String adminEmail, int page, int size) {
        verifyAdmin(adminEmail);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date", "createdAt"));
        return chargeRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    public Page<AdminChargeResponseDTO> searchCharges(String adminEmail, String keyword, Long categoryId, LocalDate startDate, LocalDate endDate, int page, int size) {
        verifyAdmin(adminEmail);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date", "createdAt"));
        return chargeRepository.adminSearchCharges(keyword, categoryId, startDate, endDate, pageable)
                .map(this::mapToDTO);
    }

    @Override
    public AdminChargeResponseDTO createCharge(String adminEmail, AdminChargeCreateRequest request) {
        verifyAdmin(adminEmail);

        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        ChargeCategory category = null;
        if (request.getCategoryId() != null) {
            category = chargeCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Catégorie introuvable"));
        }

        Charge charge = Charge.builder()
                .label(request.getLabel())
                .supplier(request.getSupplier())
                .amount(request.getAmount())
                .date(request.getDate())
                .category(category)
                .user(targetUser)
                .build();

        Charge saved = chargeRepository.save(charge);
        return mapToDTO(saved);
    }

    @Override
    public AdminChargeResponseDTO updateCharge(String adminEmail, Long chargeId, AdminChargeUpdateRequest request) {
        verifyAdmin(adminEmail);

        Charge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Charge introuvable"));

        ChargeCategory category = null;
        if (request.getCategoryId() != null) {
            category = chargeCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Catégorie introuvable"));
        }

        charge.setLabel(request.getLabel());
        charge.setSupplier(request.getSupplier());
        charge.setAmount(request.getAmount());
        charge.setDate(request.getDate());
        charge.setCategory(category);

        Charge saved = chargeRepository.save(charge);
        return mapToDTO(saved);
    }

    @Override
    public void deleteCharge(String adminEmail, Long chargeId) {
        verifyAdmin(adminEmail);
        
        Charge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Charge introuvable"));

        chargeRepository.delete(charge);
    }
}
