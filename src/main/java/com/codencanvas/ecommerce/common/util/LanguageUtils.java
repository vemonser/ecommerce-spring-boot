package com.codencanvas.ecommerce.common.util;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;

import com.codencanvas.ecommerce.common.model.Language;

public class LanguageUtils {

    public static Language getCurrentLanguage() {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return Language.valueOf(locale.getLanguage().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Language.EN;
        }
    }

    public static Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }
}