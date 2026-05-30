package com.codencanvas.ecommerce.category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codencanvas.ecommerce.category.model.CategoryTranslation;
import com.codencanvas.ecommerce.common.model.Language;

@Repository
public interface CategoryTranslationRepository 
        extends JpaRepository<CategoryTranslation, Long> {

    Optional<CategoryTranslation> findByCategoryIdAndLanguageCode(
        Long categoryId, Language languageCode);

    List<CategoryTranslation> findByCategoryId(Long categoryId);
}
