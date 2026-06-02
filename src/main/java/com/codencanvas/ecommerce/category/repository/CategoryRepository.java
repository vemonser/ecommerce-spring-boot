package com.codencanvas.ecommerce.category.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.codencanvas.ecommerce.category.dto.response.CategoryResponse;
import com.codencanvas.ecommerce.category.model.Category;
import com.codencanvas.ecommerce.common.model.Language;

import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("""
            SELECT new com.codencanvas.ecommerce.brand.dto.CategoryResponse(
                c.id, c.slug,
                CASE WHEN c.parent IS NULL THEN NULL ELSE c.parent.id END,
                t.name, t.description, c.isActive, c.createdAt, c.updatedAt, c.deletedAt
            )
            FROM Category c
            JOIN c.translations t
            WHERE c.deletedAt IS NULL
            AND t.languageCode = :lang
            """)
    List<CategoryResponse> findAllActiveByLanguage(@Param("lang") Language lang);
    
    @Query("""
            SELECT new com.codencanvas.ecommerce.brand.dto.CategoryResponse(
                c.id, c.slug,
                CASE WHEN c.parent IS NULL THEN NULL ELSE c.parent.id END,
                t.name, t.description, c.isActive, c.createdAt, c.updatedAt, c.deletedAt
            )
            FROM Category c
            JOIN c.translations t
            WHERE t.languageCode = :lang
            """)
    List<CategoryResponse> findAllByLanguage(@Param("lang") Language lang);

    @Query("""
            SELECT new com.codencanvas.ecommerce.brand.dto.BrandResponse(
                c.id, c.slug, t.name, t.description, c.isActive,c.deletedAt
            )
            FROM Category c
            JOIN c.translations t
            WHERE c.slug = :slug
            AND c.deletedAt IS NULL
            AND t.languageCode = :lang
            """)
    Optional<CategoryResponse> findBySlugWithLanguage(
            @Param("slug") String slug,
            @Param("lang") Language lang);

    @Query("""
            SELECT new com.codencanvas.ecommerce.brand.dto.BrandResponse(
                c.id, c.slug, t.name, t.description, c.isActive
            )
            FROM Category c
            JOIN c.translations t
            WHERE c.id = :id
            AND c.deletedAt IS NULL
            AND t.languageCode = :lang
            """)
    Optional<CategoryResponse> findByIdWithLanguage(
            @Param("id") Long id,
            @Param("lang") Language lang);

    @Query("""
            SELECT c FROM Category c
            LEFT JOIN FETCH c.translations
            WHERE c.id = :id
            AND c.deletedAt IS NULL
            """)
    Optional<Category> findByIdWithTranslations(@Param("id") Long id);

    boolean existsByParentIdAndDeletedAtIsNull(Long parentId);

}
