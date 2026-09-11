package com.backend.bilanko.controller.transaction;

import com.backend.bilanko.DTO.concept.transaction.OverviewSummaryDTO;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.services.transaction.OverviewService;
import com.backend.bilanko.utils.routes.OverviewApiRoutes;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class OverviewController {

    private final OverviewService overviewService;

    @GetMapping(OverviewApiRoutes.SUMMARY)
    public ResponseEntity<OverviewSummaryDTO> getSummary(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(overviewService.getSummary(currentUser, from, to));
    }
}
