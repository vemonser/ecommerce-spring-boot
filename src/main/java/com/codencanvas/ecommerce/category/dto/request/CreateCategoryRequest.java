package com.codencanvas.ecommerce.category.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCategoryRequest {
    
    private Boolean isActive;
    private Long parentId;
    @NotEmpty(message = "Translations cannot be empty")
    @Size(min = 2, max = 2, message = "Must provide exactly 2 translations")
    private List<@Valid CreateCategoryTranslationRequest> translations;
}