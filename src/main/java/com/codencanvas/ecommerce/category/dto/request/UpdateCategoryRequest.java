package com.codencanvas.ecommerce.category.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCategoryRequest {
    private Long parentId;
    private Boolean isActive;
    private List<CreateCategoryTranslationRequest> translations;
}
