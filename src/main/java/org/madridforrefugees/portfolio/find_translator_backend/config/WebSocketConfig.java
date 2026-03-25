package org.madridforrefugees.portfolio.find_translator_backend.config;

import org.madridforrefugees.portfolio.find_translator_backend.handler.FindTranslatorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private WebSocketProperties webSocketProperties;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler(), "/find-translator-signal")
                .setAllowedOrigins(webSocketProperties.allowedOrigins().toArray(new String[0]));
    }

    @Bean
    public WebSocketHandler webSocketHandler() {
        return new FindTranslatorHandler();
    }

}
