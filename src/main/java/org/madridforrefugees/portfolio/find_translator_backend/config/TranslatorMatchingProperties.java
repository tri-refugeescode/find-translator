package org.madridforrefugees.portfolio.find_translator_backend.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "find-translator.matching")
public record TranslatorMatchingProperties(@NotNull Duration findActiveTime,
                                           @Min(1) int maxFindSessions,
                                           @NotNull Duration offerActiveTime,
                                           @Min(1) int maxOfferSessions) {
}
