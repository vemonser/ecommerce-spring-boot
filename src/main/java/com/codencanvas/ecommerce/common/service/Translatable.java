package com.codencanvas.ecommerce.common.service;

import java.util.List;

import com.codencanvas.ecommerce.common.model.BaseTranslation;

public interface Translatable<T extends BaseTranslation> {
    List<T> getTranslations();
    void addTranslation(T translation);
    void removeTranslation(T translation);

}
