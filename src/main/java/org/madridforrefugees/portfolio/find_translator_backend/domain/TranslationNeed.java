package org.madridforrefugees.portfolio.find_translator_backend.domain;

import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;


public record TranslationNeed(Set<String> understoodLanguages, Set<String> requiredLanguages) {

    public boolean isValid() {
        return isNotEmpty(understoodLanguages) && isNotEmpty(requiredLanguages)
                && understoodLanguages.stream().noneMatch(String::isBlank)
                && requiredLanguages.stream().noneMatch(String::isBlank)
                && understoodLanguages.stream().noneMatch(requiredLanguages::contains)
                && requiredLanguages.stream().noneMatch(understoodLanguages::contains);

    }

}
