package org.madridforrefugees.portfolio.find_translator_backend.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;


public record TranslationNeed(Set<String> understoodLanguages, Set<String> requiredLanguages) {
    public static final String PATH = "translationNeed";

    public boolean isValid() {
        return isNotEmpty(understoodLanguages) && isNotEmpty(requiredLanguages)
                && understoodLanguages.stream().noneMatch(StringUtils::isBlank)
                && requiredLanguages.stream().noneMatch(StringUtils::isBlank)
                && understoodLanguages.stream().noneMatch(requiredLanguages::contains)
                && requiredLanguages.stream().noneMatch(understoodLanguages::contains);

    }

}
