package com.codencanvas.ecommerce.brand.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
 

@Getter
@Setter
public class CreateBrandRequest {

    @NotEmpty(message = "Translations cannot be empty")
    @Size(min = 2, max = 2, message = "Must provide exactly 2 translations")
    private List<@Valid CreateBrandTranslationRequest> translations;
}