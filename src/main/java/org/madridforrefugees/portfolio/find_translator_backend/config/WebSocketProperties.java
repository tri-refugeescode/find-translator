package org.madridforrefugees.portfolio.find_translator_backend.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "find-translator.websocket")
public record WebSocketProperties(@NotEmpty List<String> allowedOrigins) {
}
