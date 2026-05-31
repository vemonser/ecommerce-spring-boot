package com.codencanvas.ecommerce.brand.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.codencanvas.ecommerce.brand.dto.request.CreateBrandRequest;
import com.codencanvas.ecommerce.brand.dto.request.CreateBrandTranslationRequest;
import com.codencanvas.ecommerce.brand.dto.response.BrandTranslationResponse;
import com.codencanvas.ecommerce.brand.model.Brand;
import com.codencanvas.ecommerce.brand.model.BrandTranslation;


@Mapper(componentModel = "spring")
public interface BrandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "logoPublicId", ignore = true)
    @Mapping(target = "logoUrl", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Brand toEntity(CreateBrandRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    BrandTranslation toEntity(CreateBrandTranslationRequest request);

    BrandTranslationResponse toDto(BrandTranslation translation);

    // update
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateTranslationFromRequest(
            CreateBrandTranslationRequest request,
            @MappingTarget BrandTranslation translation);
}
