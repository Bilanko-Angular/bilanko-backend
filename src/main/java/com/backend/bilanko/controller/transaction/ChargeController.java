package com.backend.bilanko.controller.transaction;

import com.backend.bilanko.DTO.concept.transaction.ChargeRequestDTO;
import com.backend.bilanko.DTO.concept.transaction.ChargeResponseDTO;
import com.backend.bilanko.DTO.concept.transaction.ChargeSummaryDTO;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.services.transaction.ChargeService;
import com.backend.bilanko.utils.routes.ChargeApiRoutes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChargeController {

    private final ChargeService chargeService;

    @PostMapping(ChargeApiRoutes.BASE)
    public ResponseEntity<ChargeResponseDTO> createCharge(
            @Valid @RequestBody ChargeRequestDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        ChargeResponseDTO response = chargeService.createCharge(dto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(ChargeApiRoutes.BASE)
    public ResponseEntity<List<ChargeResponseDTO>> getAllCharges(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(chargeService.getAllCharges(currentUser, from, to));
    }

    @GetMapping(ChargeApiRoutes.SUMMARY)
    public ResponseEntity<ChargeSummaryDTO> getSummary(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(chargeService.getSummary(currentUser, from, to));
    }

    @GetMapping(ChargeApiRoutes.BY_ID)
    public ResponseEntity<ChargeResponseDTO> getChargeById(
            @PathVariable long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(chargeService.getChargeById(id, currentUser));
    }

    @PutMapping(ChargeApiRoutes.BY_ID)
    public ResponseEntity<ChargeResponseDTO> updateCharge(
            @PathVariable long id,
            @Valid @RequestBody ChargeRequestDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(chargeService.updateCharge(id, dto, currentUser));
    }

    @DeleteMapping(ChargeApiRoutes.BY_ID)
    public ResponseEntity<Void> deleteCharge(
            @PathVariable long id,
            @AuthenticationPrincipal User currentUser
    ) {
        chargeService.deleteCharge(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
