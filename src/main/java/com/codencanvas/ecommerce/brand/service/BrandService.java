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
import com.codencanvas.ecommerce.brand.repository.BrandTranslationRepository;
import com.codencanvas.ecommerce.cloudinary.service.CloudinaryService;
import com.codencanvas.ecommerce.common.exception.AppException;
import com.codencanvas.ecommerce.common.model.Language;
import com.codencanvas.ecommerce.common.util.LanguageUtils;
import com.codencanvas.ecommerce.common.util.SlugUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandService {

        private final BrandRepository brandRepository;
        private final BrandTranslationRepository translationRepository;
        private final BrandMapper brandMapper;
        private final CloudinaryService cloudinaryService;

        private String extractEnglishName(List<CreateBrandTranslationRequest> translations) {
                return translations.stream()
                                .filter(t -> t.getLanguageCode() == Language.EN)
                                .findFirst()
                                .orElseThrow(() -> new AppException("error.brand.english_required", 400))
                                .getName();
        }

        private String extractEnglishNameFromEntities(List<BrandTranslation> translations) {
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

                BrandTranslation translation = brand.getTranslations().stream()
                                .filter(t -> t.getLanguageCode() == language)
                                .findFirst()
                                .orElseThrow();
                return new BrandResponse(
                                brand.getId(),
                                brand.getSlug(),
                                brand.getLogoUrl(),
                                brandMapper.toDto(translation));
        }

        public List<BrandResponse> getBrands() {
                return brandRepository.findAllWithTranslations()
                                .stream().map(c -> buildResponse(c))
                                .toList();
        }

        public BrandResponse getBrandBySlug(String slug) {
                Brand brand = brandRepository.findBySlugWithTranslations(slug)
                                .orElseThrow(() -> new AppException("error.brand.not_found", 404));
                return buildResponse(brand);
        }

        public BrandResponse getBrandById(Long id) {
                Brand brand = brandRepository.findByIdWithTranslations(id)
                                .orElseThrow(() -> new AppException("error.brand.not_found", 404));
                return buildResponse(brand);
        }

        @Transactional
        public BrandResponse createBrand(CreateBrandRequest request, MultipartFile logo) {
                CloudinaryService.UploadResult result = cloudinaryService.upload(logo, "brands/logos");

                String slug = generateSlug(request.getTranslations());
                Brand brand = brandMapper.toEntity(request);
                brand.setSlug(slug);
                brand.setLogoUrl(result.url());
                brand.setLogoPublicId(result.publicId());

                Brand savedBrand = brandRepository.save(brand);

                List<BrandTranslation> translations = request.getTranslations().stream()
                                .map(t -> {
                                        BrandTranslation translation = brandMapper.toEntity(t);
                                        translation.setBrand(savedBrand);
                                        return translation;
                                })
                                .toList();
                translationRepository.saveAll(translations);
                savedBrand.setTranslations(translations);

                return buildResponse(savedBrand);
        }

        @Transactional
        public BrandResponse updateBrand(Long id, UpdateBrandRequest request, MultipartFile logo) {
                Brand currentBrand = brandRepository.findByIdWithTranslations(id)
                                .orElseThrow(() -> new AppException("error.brand.not_found", 404));

                String slug = currentBrand.getSlug();
                if (request.getTranslations() != null) {
                        String currentEnglishName = extractEnglishNameFromEntities(currentBrand.getTranslations());
                        String requestEnglishName = extractEnglishName(request.getTranslations());
                        slug = !currentEnglishName.equals(requestEnglishName)
                                        ? SlugUtils.toSlug(requestEnglishName)
                                        : currentBrand.getSlug();
                }

                if (logo != null && !logo.isEmpty()) {
                        cloudinaryService.delete(currentBrand.getLogoPublicId());
                        CloudinaryService.UploadResult result = cloudinaryService.upload(logo, "brands/logos");
                        currentBrand.setLogoUrl(result.url());
                        currentBrand.setLogoPublicId(result.publicId());

                }

                if (request.getIsActive() != null) {
                        currentBrand.setIsActive(request.getIsActive());
                }
                currentBrand.setSlug(slug);

                if (request.getTranslations() != null) {
                        request.getTranslations().forEach(t -> {
                                currentBrand.getTranslations().stream()
                                                .filter(existing -> existing.getLanguageCode() == t.getLanguageCode())
                                                .findFirst()
                                                .ifPresentOrElse(
                                                                existing -> brandMapper.updateTranslationFromRequest(
                                                                                t, existing),
                                                                () -> {
                                                                        BrandTranslation newTranslation = brandMapper
                                                                                        .toEntity(t);
                                                                        newTranslation.setBrand(currentBrand);
                                                                        translationRepository.save(newTranslation);
                                                                });
                        });
                }
                brandRepository.save(currentBrand);
                return buildResponse(currentBrand);
        }

        @Transactional
        public void deleteBrand(Long id) {
                Brand brand = brandRepository.findById(id).orElseThrow(
                                (() -> new AppException("error.brand.not_found", 404)));

                brand.softDelete();
                brandRepository.save(brand);
        }
}