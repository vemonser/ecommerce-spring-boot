package com.codencanvas.ecommerce.brand.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.codencanvas.ecommerce.brand.dto.response.BrandResponse;
import com.codencanvas.ecommerce.brand.model.Brand;
import com.codencanvas.ecommerce.common.model.Language;

import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    // === For READ operations (return DTO) ===

    @Query("""
            SELECT new com.codencanvas.ecommerce.brand.dto.BrandResponse(
                b.id, b.slug, b.logoUrl, t.name, t.description,
                b.isActive, b.createdAt, b.updatedAt,b.deletedAt
            )
            FROM Brand b
            JOIN b.translations t
            WHERE b.deletedAt IS NULL
            AND t.languageCode = :lang
            """)
    List<BrandResponse> findAllByLanguage(@Param("lang") Language lang);

    @Query("""
            SELECT new com.codencanvas.ecommerce.brand.dto.BrandResponse(
                b.id, b.slug, b.logoUrl, t.name, t.description,
                b.isActive, b.createdAt, b.updatedAt,b.deletedAt
            )
            FROM Brand b
            JOIN b.translations t
            WHERE b.slug = :slug
            AND b.deletedAt IS NULL
            AND t.languageCode = :lang
            """)
    Optional<BrandResponse> findBySlugWithLanguage(
            @Param("slug") String slug,
            @Param("lang") Language lang);

    @Query("""
            SELECT new com.codencanvas.ecommerce.brand.dto.BrandResponse(
                b.id, b.slug, b.logoUrl, t.name, t.description,
                b.isActive, b.createdAt, b.updatedAt,b.deletedAt
            )
            FROM Brand b
            JOIN b.translations t
            WHERE b.id = :id
            AND b.deletedAt IS NULL
            AND t.languageCode = :lang
            """)
    Optional<BrandResponse> findByIdWithLanguage(
            @Param("id") Long id,
            @Param("lang") Language lang);

    // === For WRITE operations (return Entity) ===

    @Query("""
            SELECT b FROM Brand b
            LEFT JOIN FETCH b.translations
            WHERE b.id = :id
            AND b.deletedAt IS NULL
            """)
    Optional<Brand> findByIdWithTranslations(@Param("id") Long id);

}
