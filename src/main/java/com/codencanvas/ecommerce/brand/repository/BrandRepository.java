package com.codencanvas.ecommerce.brand.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.codencanvas.ecommerce.brand.model.Brand;

import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    @Query("SELECT b FROM Brand b JOIN FETCH b.translations WHERE b.deletedAt IS NULL")
    List<Brand> findAllWithTranslations();

    @Query("SELECT b FROM Brand b JOIN FETCH b.translations WHERE b.slug = :slug AND b.deletedAt IS NULL")
    Optional<Brand> findBySlugWithTranslations(@Param("slug") String slug);

    List<Brand> findByParentIsNullAndDeletedAtIsNull();

    List<Brand> findByParentIdAndDeletedAtIsNull(Long parentId);

    @Query("SELECT b FROM Brand b JOIN FETCH b.translations WHERE b.id = :id AND b.deletedAt IS NULL")
    Optional<Brand> findByIdWithTranslations(@Param("id") Long id);

    boolean existsByParentIdAndDeletedAtIsNull(Long parentId);

}
