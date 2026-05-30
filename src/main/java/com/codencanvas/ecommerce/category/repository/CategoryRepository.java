package com.codencanvas.ecommerce.category.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.codencanvas.ecommerce.category.model.Category;

import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c JOIN FETCH c.translations WHERE c.deletedAt IS NULL")
    List<Category> findAllWithTranslations();

    @Query("SELECT c FROM Category c JOIN FETCH c.translations WHERE c.slug = :slug AND c.deletedAt IS NULL")
    Optional<Category> findBySlugWithTranslations(@Param("slug") String slug);

    List<Category> findByParentIsNullAndDeletedAtIsNull();

    List<Category> findByParentIdAndDeletedAtIsNull(Long parentId);

    @Query("SELECT c FROM Category c JOIN FETCH c.translations WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Category> findByIdWithTranslations(@Param("id") Long id);

    boolean existsByParentIdAndDeletedAtIsNull(Long parentId);

}
