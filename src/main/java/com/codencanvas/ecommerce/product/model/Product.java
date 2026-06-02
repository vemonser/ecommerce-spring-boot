package com.codencanvas.ecommerce.product.model;

import java.util.ArrayList;
import java.util.List;

import com.codencanvas.ecommerce.brand.model.Brand;
import com.codencanvas.ecommerce.category.model.Category;
import com.codencanvas.ecommerce.common.model.BaseEntity;
import com.codencanvas.ecommerce.common.service.Translatable;
import com.codencanvas.ecommerce.user.model.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Product extends BaseEntity implements Translatable<ProductTranslation> {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Builder.Default
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductTranslation> translations = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductMedia> productMedia = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> productVariants = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    // ===== Media Helpers =====
    public void addMedia(ProductMedia media) {
        productMedia.add(media);
        media.setProduct(this);
    }

    public void removeMedia(ProductMedia media) {
        productMedia.remove(media);
        media.setProduct(null);
    }

    // ===== Variant Helpers =====
    public void addVariant(ProductVariant variant) {
        productVariants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(ProductVariant variant) {
        productVariants.remove(variant);
        variant.setProduct(null);
    }

    @Override
    public void addTranslation(ProductTranslation translation) {
        productTranslations.add(translation);
        translation.setProduct(this);
    }

    @Override
    public void removeTranslation(ProductTranslation translation) {
        productTranslations.remove(translation);
        translation.setProduct(null);
    }

}
