package org.madridforrefugees.portfolio.find_translator_backend.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;


public record TranslationCapability(Set<String> proficientLanguages) {

    public boolean isValid() {
        return isNotEmpty(proficientLanguages)
                && proficientLanguages.stream().noneMatch(StringUtils::isBlank);

    }

}
