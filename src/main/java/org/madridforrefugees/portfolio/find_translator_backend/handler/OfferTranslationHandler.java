package org.madridforrefugees.portfolio.find_translator_backend.handler;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationCapability;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationNeed;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class OfferTranslationHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Map<WebSocketSession, Pair<TextMessage, TranslationCapability>> sessions = new ConcurrentHashMap<>();

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session,
                                  @NonNull TextMessage message) {
        var translationCapability = objectMapper.readValue(message.getPayload(), TranslationCapability.class);
        if (translationCapability.isValid()) {
            sessions.put(session, Pair.of(message, translationCapability));
        }
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.put(session, null);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session,
                                      @NonNull CloseStatus status) {
        if (sessions.remove(session) == null) {
            sessions.keySet().removeIf(s -> s.getId().equals(session.getId()));
        }
    }

}
