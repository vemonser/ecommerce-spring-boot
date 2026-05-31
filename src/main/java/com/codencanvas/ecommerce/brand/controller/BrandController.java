package com.codencanvas.ecommerce.brand.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codencanvas.ecommerce.brand.dto.request.CreateBrandRequest;
import com.codencanvas.ecommerce.brand.dto.request.UpdateBrandRequest;
import com.codencanvas.ecommerce.brand.dto.response.BrandResponse;
import com.codencanvas.ecommerce.brand.service.BrandService;

import com.codencanvas.ecommerce.common.annotation.IsAdmin;
import com.codencanvas.ecommerce.common.dto.ApiResponse;
import com.codencanvas.ecommerce.common.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Validated
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandResponse>>> allBrands() {

        return ResponseUtil.ok("Brands fetched successfully", brandService.getBrands());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<BrandResponse>> getBrandBySlug(

            @PathVariable String slug) {
        return ResponseUtil.ok("Brands fetched successfully", brandService.getBrandBySlug(slug));
    }

    @IsAdmin
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(
        @RequestPart("data") @Valid CreateBrandRequest request,
        @RequestPart("logo") MultipartFile logo) {
    return ResponseUtil.created(brandService.createBrand(request, logo));
    }

    @IsAdmin
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BrandResponse>> updateBrand(
        @PathVariable Long id,
        @RequestPart("data") @Valid UpdateBrandRequest request,
        @RequestPart(value = "logo", required = false) MultipartFile logo) {
    return ResponseUtil.ok("Brand Updated successfully", brandService.updateBrand(id, request, logo));
    }

    @IsAdmin
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(
            @PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseUtil.noContent();
    }
}
