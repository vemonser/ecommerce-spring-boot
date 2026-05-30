package com.codencanvas.ecommerce.category.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.codencanvas.ecommerce.category.dto.request.CreateCategoryRequest;
import com.codencanvas.ecommerce.category.dto.request.CreateCategoryTranslationRequest;
import com.codencanvas.ecommerce.category.dto.response.CategoryTranslationResponse;
import com.codencanvas.ecommerce.category.model.Category;
import com.codencanvas.ecommerce.category.model.CategoryTranslation;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Category toEntity(CreateCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    CategoryTranslation toEntity(CreateCategoryTranslationRequest request);

    CategoryTranslationResponse toDto(CategoryTranslation translation);

    // update
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateTranslationFromRequest(
            CreateCategoryTranslationRequest request,
            @MappingTarget CategoryTranslation translation);
}
