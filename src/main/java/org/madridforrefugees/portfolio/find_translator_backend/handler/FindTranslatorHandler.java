package org.madridforrefugees.portfolio.find_translator_backend.handler;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationNeed;
import org.madridforrefugees.portfolio.find_translator_backend.repository.FindTranslatorRepository;
import org.madridforrefugees.portfolio.find_translator_backend.service.TranslatorMatchingService;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public class FindTranslatorHandler extends TextWebSocketHandler {

    private final FindTranslatorRepository findTranslatorRepository;
    private final ObjectMapper objectMapper;
    private final TranslatorMatchingService translatorMatchingService;

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session,
                                  @NonNull TextMessage message) {
        var translationNeed = objectMapper.readValue(message.getPayload(), TranslationNeed.class);
        if (translationNeed.isValid()) {
            findTranslatorRepository.sessions().put(session, Pair.of(message, translationNeed));
            translatorMatchingService.matchOnFind(translationNeed, session, message);
        }
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        findTranslatorRepository.sessions().put(session, null);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session,
                                      @NonNull CloseStatus status) {
        if (findTranslatorRepository.sessions().remove(session) == null) {
            findTranslatorRepository.sessions().keySet().removeIf(s -> s.getId().equals(session.getId()));
        }
    }

}
