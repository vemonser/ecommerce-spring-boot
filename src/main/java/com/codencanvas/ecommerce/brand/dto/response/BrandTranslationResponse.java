package com.codencanvas.ecommerce.brand.dto.response;

import com.codencanvas.ecommerce.common.model.Language;

public record BrandTranslationResponse(
                String name,
                String description,
                Language languageCode) {
}
