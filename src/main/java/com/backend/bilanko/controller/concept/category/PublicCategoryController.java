package com.backend.bilanko.controller.concept.category;

import com.backend.bilanko.DTO.concept.category.CategorySearch;
import com.backend.bilanko.DTO.concept.category.CleanCategoryDTO;
import com.backend.bilanko.DTO.shared.PageResponse;
import com.backend.bilanko.models.concept.category.CategoryType;
import com.backend.bilanko.services.concept.category.ChargeCategoryService;
import com.backend.bilanko.services.concept.category.MultiCategoryService;
import com.backend.bilanko.services.concept.category.ProductCategoryServices;
import com.backend.bilanko.utils.routes.category.PublicCategoryApiRoutes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(PublicCategoryApiRoutes.publicCategory)
public class PublicCategoryController {

    private final MultiCategoryService multiCategoryService;
    private final ProductCategoryServices productCategoryServices;
    private final ChargeCategoryService chargeCategoryServices;

    @GetMapping(PublicCategoryApiRoutes.search_by_name)
    public ResponseEntity<PageResponse<CategorySearch>> searchByName(
            @RequestParam String name,
            @RequestParam(required = false) CategoryType categoryType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(multiCategoryService.search(name, categoryType, page, size));
    }

    @GetMapping(PublicCategoryApiRoutes.find_all_charges_category)
    public ResponseEntity<List<CleanCategoryDTO>> findAllChargesCategory() {
        return
    }


}
