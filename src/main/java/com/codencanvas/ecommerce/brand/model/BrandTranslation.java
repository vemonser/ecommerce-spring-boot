package com.codencanvas.ecommerce.brand.model;

import com.codencanvas.ecommerce.common.model.BaseEntity;
import com.codencanvas.ecommerce.common.model.Language;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "brand_translations", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "brand_id", "language_code" })
})
public class BrandTranslation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;
    
    @Column(name = "name", nullable = false,  length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;
    @Column(name = "language_code")
    private Language languageCode;

}
