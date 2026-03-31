package org.madridforrefugees.portfolio.find_translator_backend.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.size;

public record TranslationCapability(Set<String> proficientLanguages) {
    public static final String PATH = "translationCapability";

    public boolean isValid() {
        return size(proficientLanguages) >= 2
                && proficientLanguages.stream().noneMatch(StringUtils::isBlank);

    }

}
