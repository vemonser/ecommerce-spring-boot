package com.codencanvas.ecommerce.brand.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.codencanvas.ecommerce.brand.dto.request.CreateBrandRequest;
import com.codencanvas.ecommerce.brand.dto.request.CreateBrandTranslationRequest;
import com.codencanvas.ecommerce.brand.dto.request.UpdateBrandRequest;
import com.codencanvas.ecommerce.brand.dto.response.BrandResponse;
import com.codencanvas.ecommerce.brand.mapper.BrandMapper;
import com.codencanvas.ecommerce.brand.model.Brand;
import com.codencanvas.ecommerce.brand.model.BrandTranslation;
import com.codencanvas.ecommerce.brand.repository.BrandRepository;
import com.codencanvas.ecommerce.cloudinary.service.CloudinaryService;
import com.codencanvas.ecommerce.common.exception.AppException;
import com.codencanvas.ecommerce.common.model.Language;
import com.codencanvas.ecommerce.common.service.TranslationSyncService;
import com.codencanvas.ecommerce.common.util.LanguageUtils;
import com.codencanvas.ecommerce.common.util.SlugUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandService {

        private final BrandRepository brandRepository;
        private final BrandMapper brandMapper;
        private final CloudinaryService cloudinaryService;
        private final TranslationSyncService syncService;

        private String extractEnglishName(List<CreateBrandTranslationRequest> translations) {
                return translations.stream()
                                .filter(t -> t.getLanguageCode() == Language.EN)
                                .findFirst()
                                .orElseThrow(() -> new AppException("error.brand.english_required", 400))
                                .getName();
        }

        private String generateSlug(List<CreateBrandTranslationRequest> translations) {
                return SlugUtils.toSlug(extractEnglishName(translations));
        }

        private BrandResponse buildResponse(Brand brand) {
                Language language = LanguageUtils.getCurrentLanguage();

                return brand.getTranslations().stream()
                                .filter(t -> t.getLanguageCode() == language)
                                .findFirst()
                                .map(translation -> new BrandResponse(
                                                brand.getId(),
                                                brand.getSlug(),
                                                brand.getLogoUrl(),
                                                translation.getName(),
                                                translation.getDescription(),
                                                brand.getIsActive(),
                                                brand.getCreatedAt(), 
                                                brand.getUpdatedAt(), 
                                                brand.getDeletedAt() 
                                ))
                                .orElseThrow(() -> new AppException("error.brand.translation_not_found", 404));
        }

        public List<BrandResponse> getBrands() {
                Language language = LanguageUtils.getCurrentLanguage();
                return brandRepository.findAllByLanguage(language);
        }

        public BrandResponse getBrandBySlug(String slug) {
                Language language = LanguageUtils.getCurrentLanguage();
                return brandRepository.findBySlugWithLanguage(slug, language)
                                .orElseThrow(() -> new AppException("error.brand.not_found", 404));
        }

        public BrandResponse getBrandById(Long id) {
                Language language = LanguageUtils.getCurrentLanguage();
                return brandRepository.findByIdWithLanguage(id, language)
                                .orElseThrow(() -> new AppException("error.brand.not_found", 404));
        }

        @Transactional
        public BrandResponse createBrand(CreateBrandRequest request, MultipartFile logo) {
                CloudinaryService.UploadResult result = cloudinaryService.upload(logo, "brands/logos");

                String slug = generateSlug(request.getTranslations());
                Boolean isActive = request.getIsActive() != null ? request.getIsActive() : true;

                Brand brand = Brand.create(slug, result.url(), result.publicId(), isActive);

                request.getTranslations().forEach(t -> {
                        BrandTranslation translation = brandMapper.toEntity(t);
                        brand.addTranslation(translation);
                });
                Brand savedBrand = brandRepository.save(brand);
                return buildResponse(savedBrand);

        }

        @Transactional
        public BrandResponse updateBrand(Long id, UpdateBrandRequest request, MultipartFile logo) {
                // 1. Brand ENTITY
                Brand currentBrand = brandRepository.findByIdWithTranslations(id)
                                .orElseThrow(() -> new AppException("error.brand.not_found", 404));

                // 2. Logo
                if (logo != null && !logo.isEmpty()) {
                        cloudinaryService.delete(currentBrand.getLogoPublicId());
                        CloudinaryService.UploadResult result = cloudinaryService.upload(logo, "brands/logos");
                        currentBrand.setLogoUrl(result.url());
                        currentBrand.setLogoPublicId(result.publicId());
                }

                // 3. isActive
                if (request.getIsActive() != null) {
                        currentBrand.setIsActive(request.getIsActive());
                }

                // 4. Sync Translations
                if (request.getTranslations() != null) {
                        syncService.sync(
                                        currentBrand,
                                        request.getTranslations(),
                                        BrandTranslation::getLanguageCode,
                                        CreateBrandTranslationRequest::getLanguageCode,
                                        brandMapper::updateTranslationFromRequest,
                                        brandMapper::toEntity);
                }
                Brand saved = brandRepository.save(currentBrand);
                // 5. Return DTO
                return buildResponse(saved);
        }

        @Transactional
        public BrandResponse  deleteBrand(Long id) {
                Brand brand = brandRepository.findById(id).orElseThrow(
                                (() -> new AppException("error.brand.not_found", 404)));

                brand.softDelete();
                brandRepository.save(brand);
                return buildResponse(brand);
        }
}