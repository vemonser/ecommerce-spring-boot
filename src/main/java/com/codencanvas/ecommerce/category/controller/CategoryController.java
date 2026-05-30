package com.codencanvas.ecommerce.category.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codencanvas.ecommerce.category.dto.request.CreateCategoryRequest;
import com.codencanvas.ecommerce.category.dto.request.UpdateCategoryRequest;
import com.codencanvas.ecommerce.category.dto.response.CategoryResponse;

import com.codencanvas.ecommerce.category.service.CategoryService;
import com.codencanvas.ecommerce.common.annotation.IsAdmin;
import com.codencanvas.ecommerce.common.dto.ApiResponse;
import com.codencanvas.ecommerce.common.model.Language;
import com.codencanvas.ecommerce.common.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> allCategories(
            @RequestHeader(value = "Accept-Language", defaultValue = "EN") Language language) {

        return ResponseUtil.ok("Categories fetched successfully", categoryService.getCategories(language));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryBySlug(
            @RequestHeader(value = "Accept-Language", defaultValue = "EN") Language language,
            @PathVariable String slug) {
        return ResponseUtil.ok("Categories fetched successfully", categoryService.getCategoryBySlug(slug, language));
    }

    @IsAdmin
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @RequestHeader(value = "Accept-Language", defaultValue = "EN") Language language,
            @RequestBody @Valid CreateCategoryRequest request) {
        return ResponseUtil.ok("Category Created successfully", categoryService.createCategory(request, language));
    }

    @IsAdmin
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @RequestHeader(value = "Accept-Language", defaultValue = "EN") Language language,
            @PathVariable Long id,
            @RequestBody @Valid UpdateCategoryRequest request) {
        return ResponseUtil.ok("Category Updated successfully",
                categoryService.updateCategory(id, request, language));
    }

    @IsAdmin
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseUtil.noContent();
    }
}
