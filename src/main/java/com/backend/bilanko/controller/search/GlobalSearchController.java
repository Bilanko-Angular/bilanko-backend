package com.backend.bilanko.controller.search;

import com.backend.bilanko.DTO.search.GlobalSearchResultDTO;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.services.search.GlobalSearchService;
import com.backend.bilanko.utils.annotation.MerchantOnly;
import com.backend.bilanko.utils.routes.GlobalSearchApiRoutes;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(GlobalSearchApiRoutes.BASE)
@RequiredArgsConstructor
@MerchantOnly
@Validated
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    @GetMapping(GlobalSearchApiRoutes.GLOBAL_PATH)
    public List<GlobalSearchResultDTO> search(
            @RequestParam String keyword,
            @AuthenticationPrincipal User currentUser
    ) {
        return globalSearchService.search(keyword, currentUser);
    }
}
