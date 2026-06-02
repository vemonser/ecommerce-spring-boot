package com.codencanvas.ecommerce.common.service;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.codencanvas.ecommerce.common.model.BaseTranslation;
import com.codencanvas.ecommerce.common.model.Language;

@Component
public class TranslationSyncService {

    public <T extends BaseTranslation, R> void sync(
            Translatable<T> parent,
            List<R> requests,
            Function<T, Language> existingKeyFn, // T::getLanguageCode
            Function<R, Language> requestKeyFn, // R::getLanguageCode
            BiConsumer<R, T> updateFn, // mapper::updateTranslation
            Function<R, T> createFn // mapper::toEntity
    ) {
        List<T> existing = parent.getTranslations();

        // 1. Map existing by language
        Map<Language, T> existingMap = existing.stream()
                .collect(Collectors.toMap(
                        existingKeyFn,
                        t -> t,
                        (old, newer) -> old // handle duplicates
                ));

        // 2. Map requested by language
        Map<Language, R> requestMap = requests.stream()
                .collect(Collectors.toMap(
                        requestKeyFn,
                        r -> r));

        // 3. Remove what's not in request
        existing.stream()
                .filter(t -> !requestMap.containsKey(existingKeyFn.apply(t)))
                .toList()
                .forEach(parent::removeTranslation);

        // 4. Update or Add
        requests.forEach(req -> {
            Language key = requestKeyFn.apply(req);
            T old = existingMap.get(key);
            if (old != null) {
                updateFn.accept(req, old);
            } else {
                T newTrans = createFn.apply(req);
                parent.addTranslation(newTrans);
            }
        });
    }
}
