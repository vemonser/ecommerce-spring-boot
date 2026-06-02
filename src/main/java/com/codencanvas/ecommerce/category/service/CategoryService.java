package com.codencanvas.ecommerce.category.service;

import java.util.List;

import org.springframework.stereotype.Service;


import com.codencanvas.ecommerce.category.dto.request.CreateCategoryRequest;
import com.codencanvas.ecommerce.category.dto.request.CreateCategoryTranslationRequest;
import com.codencanvas.ecommerce.category.dto.request.UpdateCategoryRequest;
import com.codencanvas.ecommerce.category.dto.response.CategoryResponse;
import com.codencanvas.ecommerce.category.mapper.CategoryMapper;
import com.codencanvas.ecommerce.category.model.Category;
import com.codencanvas.ecommerce.category.model.CategoryTranslation;
import com.codencanvas.ecommerce.category.repository.CategoryRepository;
import com.codencanvas.ecommerce.common.exception.AppException;
import com.codencanvas.ecommerce.common.model.Language;
import com.codencanvas.ecommerce.common.service.TranslationSyncService;
import com.codencanvas.ecommerce.common.util.LanguageUtils;
import com.codencanvas.ecommerce.common.util.SlugUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

        private final CategoryRepository categoryRepository;
        private final CategoryMapper categoryMapper;
        private final TranslationSyncService syncService;

        private String extractEnglishName(List<CreateCategoryTranslationRequest> translations) {
                return translations.stream()
                                .filter(t -> t.getLanguageCode() == Language.EN)
                                .findFirst()
                                .orElseThrow(() -> new AppException("error.category.english_required", 400))
                                .getName();
        }

        private String generateSlug(List<CreateCategoryTranslationRequest> translations) {
                return SlugUtils.toSlug(extractEnglishName(translations));
        }

        private CategoryResponse buildResponse(Category category) {
                Language language = LanguageUtils.getCurrentLanguage();

                return category.getTranslations().stream()
                                .filter(t -> t.getLanguageCode() == language)
                                .findFirst()
                                .map(translation -> new CategoryResponse(
                                                category.getId(),
                                                category.getSlug(),
                                                category.getParent() == null ? null : category.getParent().getId(),
                                                translation.getName(),
                                                translation.getDescription(),
                                                category.getIsActive(),
                                                category.getCreatedAt(),
                                                category.getUpdatedAt(),
                                                category.getDeletedAt()))
                                .orElseThrow(() -> new AppException("error.category.translation_not_found", 404));
        }

        private Category fetchParentIfExists(Long parentId) {
                if (parentId == null)
                        return null;
                return categoryRepository.findById(parentId)
                                .orElseThrow(() -> new AppException("error.category.parent_not_found", 404));
        }

        public List<CategoryResponse> getCategories() {
                Language language = LanguageUtils.getCurrentLanguage();
                return categoryRepository.findAllActiveByLanguage(language);
        }

        public CategoryResponse getCategoryBySlug(String slug) {
                Language language = LanguageUtils.getCurrentLanguage();
                return categoryRepository.findBySlugWithLanguage(slug, language)
                                .orElseThrow(() -> new AppException("error.category.not_found", 404));
        }

        public CategoryResponse getCategoryById(Long id) {
                Language language = LanguageUtils.getCurrentLanguage();
                return categoryRepository.findByIdWithLanguage(id, language)
                                .orElseThrow(() -> new AppException("error.category.not_found", 404));
        }

        @Transactional
        public CategoryResponse createCategory(CreateCategoryRequest request) {
                String slug = generateSlug(request.getTranslations());
                Boolean isActive = request.getIsActive() != null ? request.getIsActive() : true;

                Category parent = fetchParentIfExists(request.getParentId());

                Category category = Category.create(slug, parent, isActive);

                request.getTranslations().forEach(t -> {
                        CategoryTranslation translation = categoryMapper.toEntity(t);
                        category.addTranslation(translation);
                });
                Category savedCategory = categoryRepository.save(category);
                return buildResponse(savedCategory);

        }

        @Transactional
        public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
                Category currentCategory = categoryRepository.findByIdWithTranslations(id)
                                .orElseThrow(() -> new AppException("error.category.not_found", 404));

                Category parent = fetchParentIfExists(request.getParentId());
                currentCategory.setParent(parent);

                if (request.getIsActive() != null) {
                        currentCategory.setIsActive(request.getIsActive());
                }

                // 4. Sync Translations
                if (request.getTranslations() != null) {
                        syncService.sync(
                                        currentCategory,
                                        request.getTranslations(),
                                        CategoryTranslation::getLanguageCode,
                                        CreateCategoryTranslationRequest::getLanguageCode,
                                        categoryMapper::updateTranslationFromRequest,
                                        categoryMapper::toEntity);
                }
                Category saved = categoryRepository.save(currentCategory);
                // 5. Return DTO
                return buildResponse(saved);

        }

        @Transactional
        public CategoryResponse deleteCategory(Long id) {
                Category category = categoryRepository.findById(id).orElseThrow(
                                (() -> new AppException("error.category.not_found", 404)));
                if (categoryRepository.existsByParentIdAndDeletedAtIsNull(id)) {
                        throw new AppException("error.category.has_children", 409);
                }
                category.softDelete();
                categoryRepository.save(category);
                return buildResponse(category);

        }
}