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
import com.codencanvas.ecommerce.category.repository.CategoryTranslationRepository;
import com.codencanvas.ecommerce.common.exception.AppException;
import com.codencanvas.ecommerce.common.model.Language;
import com.codencanvas.ecommerce.common.util.LanguageUtils;
import com.codencanvas.ecommerce.common.util.SlugUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

        private final CategoryRepository categoryRepository;
        private final CategoryTranslationRepository translationRepository;
        private final CategoryMapper categoryMapper;

        private String extractEnglishName(List<CreateCategoryTranslationRequest> translations) {
                return translations.stream()
                                .filter(t -> t.getLanguageCode() == Language.EN)
                                .findFirst()
                                .orElseThrow(() -> new AppException("error.category.english_required", 400))
                                .getName();
        }

        private String extractEnglishNameFromEntities(List<CategoryTranslation> translations) {
                return translations.stream()
                                .filter(t -> t.getLanguageCode() == Language.EN)
                                .findFirst()
                                .orElseThrow(() -> new AppException("error.category.english_required", 400))
                                .getName();
        }

        private String generateSlug(List<CreateCategoryTranslationRequest> translations) {
                return SlugUtils.toSlug(extractEnglishName(translations));
        }

        private Category fetchParentIfExists(Long parentId) {
                if (parentId == null)
                        return null;
                return categoryRepository.findById(parentId)
                                .orElseThrow(() -> new AppException("error.category.parent_not_found", 404));
        }

        private CategoryResponse buildResponse(Category category) {
                Language language = LanguageUtils.getCurrentLanguage();

                CategoryTranslation translation = category.getTranslations().stream()
                                .filter(t -> t.getLanguageCode() == language)
                                .findFirst()
                                .orElseThrow();
                return new CategoryResponse(
                                category.getId(),
                                category.getSlug(),
                                category.getParent() == null ? null : category.getParent().getId(),
                                categoryMapper.toDto(translation));
        }

        public List<CategoryResponse> getCategories() {
                return categoryRepository.findAllWithTranslations()
                                .stream().map(c -> buildResponse(c))
                                .toList();
        }

        public CategoryResponse getCategoryBySlug(String slug) {
                Category category = categoryRepository.findBySlugWithTranslations(slug)
                                .orElseThrow(() -> new AppException("error.category.not_found", 404));
                return buildResponse(category);
        }
        public CategoryResponse getCategoryById(Long id) {
                Category category = categoryRepository.findByIdWithTranslations(id)
                                .orElseThrow(() -> new AppException("error.category.not_found", 404));
                return buildResponse(category);
        }

        @Transactional
        public CategoryResponse createCategory(CreateCategoryRequest request) {
                String slug = generateSlug(request.getTranslations());
                Category parent = fetchParentIfExists(request.getParentId());

                Category category = categoryMapper.toEntity(request);
                category.setSlug(slug);
                category.setParent(parent);

                Category savedCategory = categoryRepository.save(category);

                List<CategoryTranslation> translations = request.getTranslations().stream()
                                .map(t -> {
                                        CategoryTranslation translation = categoryMapper.toEntity(t);
                                        translation.setCategory(savedCategory);
                                        return translation;
                                })
                                .toList();

                translationRepository.saveAll(translations);
                savedCategory.setTranslations(translations);

                return buildResponse(savedCategory);
        }

        @Transactional
        public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
                Category currentCategory = categoryRepository.findByIdWithTranslations(id)
                                .orElseThrow(() -> new AppException("error.category.not_found", 404));

                String slug = currentCategory.getSlug();
                if (request.getTranslations() != null) {
                        String currentEnglishName = extractEnglishNameFromEntities(currentCategory.getTranslations());
                        String requestEnglishName = extractEnglishName(request.getTranslations());
                        slug = !currentEnglishName.equals(requestEnglishName)
                                        ? SlugUtils.toSlug(requestEnglishName)
                                        : currentCategory.getSlug();
                }

                Category parent = fetchParentIfExists(request.getParentId());
                currentCategory.setParent(parent);

                if (request.getIsActive() != null) {
                        currentCategory.setIsActive(request.getIsActive());
                }
                currentCategory.setSlug(slug);

                if (request.getTranslations() != null) {
                        request.getTranslations().forEach(t -> {
                                currentCategory.getTranslations().stream()
                                                .filter(existing -> existing.getLanguageCode() == t.getLanguageCode())
                                                .findFirst()
                                                .ifPresentOrElse(
                                                                existing -> categoryMapper.updateTranslationFromRequest(
                                                                                t, existing),
                                                                () -> {
                                                                        CategoryTranslation newTranslation = categoryMapper
                                                                                        .toEntity(t);
                                                                        newTranslation.setCategory(currentCategory);
                                                                        translationRepository.save(newTranslation);
                                                                });
                        });
                }

                categoryRepository.save(currentCategory);

                return buildResponse(currentCategory);

        }

        @Transactional
        public void deleteCategory(Long id) {
                Category category = categoryRepository.findById(id).orElseThrow(
                                (() -> new AppException("error.category.not_found", 404)));
                if (categoryRepository.existsByParentIdAndDeletedAtIsNull(id)) {
                        throw new AppException("error.category.has_children", 409);
                }
                category.softDelete();
                categoryRepository.save(category);
        }
}