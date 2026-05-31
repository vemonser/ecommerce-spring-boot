package com.codencanvas.ecommerce.brand.dto.response;

public record BrandResponse(
        Long id,
        String slug,
        String logoUrl,
        BrandTranslationResponse translation) {
}
