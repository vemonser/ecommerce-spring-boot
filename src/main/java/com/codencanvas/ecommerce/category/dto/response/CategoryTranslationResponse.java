package com.codencanvas.ecommerce.category.dto.response;

import com.codencanvas.ecommerce.common.model.Language;

public record CategoryTranslationResponse(
                String name,
                String description,
                Language languageCode) {
}
