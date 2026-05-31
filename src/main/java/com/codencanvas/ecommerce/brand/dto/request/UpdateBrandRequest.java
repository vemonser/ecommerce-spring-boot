package com.codencanvas.ecommerce.brand.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBrandRequest {
    private Boolean isActive;
    private List<CreateBrandTranslationRequest> translations;
}
