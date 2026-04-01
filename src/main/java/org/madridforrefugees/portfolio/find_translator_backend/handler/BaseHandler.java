package org.madridforrefugees.portfolio.find_translator_backend.handler;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.madridforrefugees.portfolio.find_translator_backend.domain.SessionData;
import org.madridforrefugees.portfolio.find_translator_backend.repository.SessionRepository;
import org.madridforrefugees.portfolio.find_translator_backend.service.TranslatorMatchingService;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
abstract class BaseHandler<T> extends TextWebSocketHandler {

    final SessionRepository<T> sessionRepository;
    final ObjectMapper objectMapper;
    final TranslatorMatchingService translatorMatchingService;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessionRepository.sessions().put(session, new SessionData<>(null, null));
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session,
                                      @NonNull CloseStatus status) {
        if (sessionRepository.sessions().remove(session) == null) {
            sessionRepository.sessions().keySet().removeIf(s -> s.getId().equals(session.getId()));
        }
    }

}
