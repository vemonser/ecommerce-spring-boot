package com.codencanvas.ecommerce.category.model;

import java.util.List;

import com.codencanvas.ecommerce.common.model.BaseEntity;
import com.codencanvas.ecommerce.common.service.Translatable;

import jakarta.persistence.Column;
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

@SuperBuilder
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "categories")
public class Category extends BaseEntity implements Translatable<CategoryTranslation> {
    @Column(name = "slug", nullable = false, unique = true, length = 120)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @Setter
    private Category parent;

    @Builder.Default
    @Setter
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<CategoryTranslation> translations;

    public Boolean isRootCategory() {
        return this.parent == null;
    }

    public void updateParent(Category parent) {
        this.parent = parent;
    }

    public static Category create(String slug, Category parent, boolean isActive) {
        return Category.builder()
                .slug(slug)
                .parent(parent)
                .isActive(isActive)
                .build();
    }

    @Override
    public void addTranslation(CategoryTranslation translation) {
        translations.add(translation);
        translation.setCategory(this);
    }

    @Override
    public void removeTranslation(CategoryTranslation translation) {
        translations.remove(translation);
        translation.setCategory(null);
    }
}
