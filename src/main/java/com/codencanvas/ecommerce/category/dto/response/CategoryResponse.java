package com.codencanvas.ecommerce.category.dto.response;

public record CategoryResponse(

        Long id,
        String slug,
        Long parentId,
        CategoryTranslationResponse translation) {
}
