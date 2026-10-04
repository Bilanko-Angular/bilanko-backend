package com.backend.bilanko.controller.concept.category;

import com.backend.bilanko.DTO.concept.category.CategoryDTO;
import com.backend.bilanko.DTO.concept.category.CategorySearch;
import com.backend.bilanko.DTO.concept.category.CleanCategoryDTO;
import com.backend.bilanko.DTO.shared.PageResponse;
import com.backend.bilanko.DTO.summary.category.CategorySummaryDTO;
import com.backend.bilanko.mapper.CategoryMapper;
import com.backend.bilanko.models.concept.category.BaseCategoryModel;
import com.backend.bilanko.models.concept.category.CategoryType;
import com.backend.bilanko.services.concept.category.CategoryService;
import com.backend.bilanko.services.concept.category.MultiCategoryService;
import com.backend.bilanko.services.concept.category.ProductCategoryServices;
import com.backend.bilanko.utils.routes.CategoryApiRoutes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(CategoryApiRoutes.category)
public class CategoryController {
    private final ProductCategoryServices productCategoryServices;
    private final Map<CategoryType, CategoryService> categoryServices;
    private final MultiCategoryService multiCategoryService;
    public CategoryController(ProductCategoryServices productCategoryServices, List<CategoryService> categoryServices, MultiCategoryService multiCategoryService) {
        this.productCategoryServices = productCategoryServices;
        this.categoryServices= categoryServices.stream()
                .collect(Collectors.toMap(
                        CategoryService::type,
                        Function.identity(),
                        (a, b) -> { throw new IllegalStateException(
                                "Deux services pour " + a.type() + " : "
                                        + a.getClass().getName() + " et " + b.getClass().getName()); },
                        () -> new EnumMap<>(CategoryType.class)));
        this.multiCategoryService = multiCategoryService;
    }
    // ── CREATE ─────────────────────────────────────────────────────────────
    // POST /api/categories/create  →  ADMIN uniquement
    @PostMapping(CategoryApiRoutes.create_category)
    public ResponseEntity<CleanCategoryDTO> create(
            @RequestBody CategoryDTO categoryDTO) {

        CategoryService categoryService=categoryServices.get(categoryDTO.categoryType());
        BaseCategoryModel categoryModel=categoryService.create(categoryDTO.name());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoryMapper.fromBaseCategoryToCleanCategoryDTO(categoryModel,categoryDTO.categoryType()));
    }

    // ── READ ───────────────────────────────────────────────────────────────
    // GET /api/categories/all  →  public (catalogue)
    @GetMapping(CategoryApiRoutes.find_all)
    public ResponseEntity<List<CleanCategoryDTO>> findAll(@RequestParam CategoryType categoryType) {
        CategoryService categoryService=categoryServices.get(categoryType);
        List<CleanCategoryDTO> result = categoryService.findAll().stream()
                .map(c -> CategoryMapper.fromBaseCategoryToCleanCategoryDTO(c, categoryType))
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<PageResponse<CategorySearch>> findAll(
            @RequestParam(required = false) CategoryType categoryType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(multiCategoryService.findAll(categoryType, page, size));
    }

    //Obtenir un résumé des catégories
    // GET /api/summary
    @GetMapping(CategoryApiRoutes.summary)
    public ResponseEntity<CategorySummaryDTO> getSummary(){
        return ResponseEntity.ok(multiCategoryService.summary());
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────
    // PUT /api/categories/{id}  →  ADMIN uniquement
    @PutMapping(CategoryApiRoutes.update_category)
    public ResponseEntity<CleanCategoryDTO> update(
            @RequestBody CategoryDTO categoryDTO) {

        CategoryService categoryService=categoryServices.get(categoryDTO.categoryType());
        BaseCategoryModel categoryModel=categoryService.update(categoryDTO.id(),  categoryDTO.name());
        return ResponseEntity.ok(CategoryMapper.fromBaseCategoryToCleanCategoryDTO(categoryModel,categoryDTO.categoryType()));
    }

    // ── DELETE ─────────────────────────────────────────────────────────────
    @DeleteMapping(CategoryApiRoutes.delete_category)
    public ResponseEntity<Void> delete(@RequestBody CategoryDTO categoryDTO) {
        CategoryService categoryService=categoryServices.get(categoryDTO.categoryType());
        categoryService.delete(categoryDTO.id());
        return ResponseEntity.noContent().build();  // 204 No Content
    }

    // Route spéciale ouverte à tout
    @GetMapping("/search")
    public ResponseEntity<List<CleanCategoryDTO>> findCategoriesProductByName(@RequestParam String name) {
        return  ResponseEntity.ok(productCategoryServices.findCategoriesProductByName(name));
    }
}


