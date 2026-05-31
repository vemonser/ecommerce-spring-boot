package com.codencanvas.ecommerce.brand.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codencanvas.ecommerce.brand.model.BrandTranslation;
import com.codencanvas.ecommerce.common.model.Language;

@Repository
public interface BrandTranslationRepository
        extends JpaRepository<BrandTranslation, Long> {

    Optional<BrandTranslation> findByBrandIdAndLanguageCode(
            Long brandId, Language languageCode);

    List<BrandTranslation> findByBrandId(Long brandId);
}
