package org.madridforrefugees.portfolio.find_translator_backend.config;

import lombok.RequiredArgsConstructor;
import org.madridforrefugees.portfolio.find_translator_backend.handler.FindTranslatorHandler;
import org.madridforrefugees.portfolio.find_translator_backend.handler.OfferTranslationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketProperties webSocketProperties;
    private final ObjectMapper objectMapper;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        var allowedOrigins = webSocketProperties.allowedOrigins().toArray(new String[0]);

        registry.addHandler(findTranslatorHandler(), "/find-translator-signal")
                .setAllowedOrigins(allowedOrigins);
        registry.addHandler(offerTranslationHandler(), "/offer-translation-signal")
                .setAllowedOrigins(allowedOrigins);
    }

    @Bean
    public WebSocketHandler findTranslatorHandler() {
        return new FindTranslatorHandler(objectMapper);
    }

    @Bean
    public WebSocketHandler offerTranslationHandler() {
        return new OfferTranslationHandler(objectMapper);
    }

}
