package com.codencanvas.ecommerce.brand.model;

import java.util.ArrayList;
import java.util.List;

import com.codencanvas.ecommerce.common.model.BaseEntity;
import com.codencanvas.ecommerce.common.service.Translatable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;

import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "brands")
public class Brand extends BaseEntity implements Translatable<BrandTranslation> {

    @Column(name = "slug", nullable = false, unique = true, length = 120)
    private String slug;

    @Setter
    @Column(name = "logo_url")
    private String logoUrl;

    @Setter
    @Column(name = "logo_public_id")
    private String logoPublicId;

    @Setter
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BrandTranslation> translations = new ArrayList<>();

    public static Brand create(String slug, String logoUrl, String logoPublicId, boolean isActive) {
        return Brand.builder()
                .slug(slug)
                .logoUrl(logoUrl)
                .logoPublicId(logoPublicId)
                .isActive(isActive) 
                .build();
    }

    @Override
    public void addTranslation(BrandTranslation translation) {
        translations.add(translation);
        translation.setBrand(this);
    }

    @Override
    public void removeTranslation(BrandTranslation translation) {
        translations.remove(translation);
        translation.setBrand(null);
    }

}
