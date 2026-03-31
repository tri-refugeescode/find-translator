package org.madridforrefugees.portfolio.find_translator_backend.handler;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.madridforrefugees.portfolio.find_translator_backend.domain.EventType;
import org.madridforrefugees.portfolio.find_translator_backend.domain.SessionData;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationNeed;
import org.madridforrefugees.portfolio.find_translator_backend.repository.SessionRepository;
import org.madridforrefugees.portfolio.find_translator_backend.service.TranslatorMatchingService;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.madridforrefugees.portfolio.find_translator_backend.domain.EventType.FIND_TRANSLATOR;
import static org.madridforrefugees.portfolio.find_translator_backend.domain.EventType.REGISTER_CANDIDATE;

@RequiredArgsConstructor
public class FindTranslatorHandler extends TextWebSocketHandler {

    private final SessionRepository<TranslationNeed> findTranslatorRepository;
    private final ObjectMapper objectMapper;
    private final TranslatorMatchingService translatorMatchingService;

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session,
                                  @NonNull TextMessage message) {
        var messageContent = objectMapper.readTree(message.getPayload());
        var eventType = messageContent.path(EventType.PATH).stringValue();
        if (FIND_TRANSLATOR.name().equals(eventType)) {
            handleFindTranslator(session, message, messageContent);
        } else if (REGISTER_CANDIDATE.name().equals(eventType)) {
            handleRegisterCandidate(session, message);
        }
    }

    private void handleFindTranslator(WebSocketSession session,
                                      TextMessage message,
                                      JsonNode messageContent) {
        var translationNeed = objectMapper.treeToValue(messageContent.path(TranslationNeed.PATH), TranslationNeed.class);
        if (translationNeed.isValid()) {
            findTranslatorRepository.sessions().put(session, new SessionData<>(translationNeed, message, null));
            translatorMatchingService.matchOnFind(translationNeed, session, message);
        }
    }

    private void handleRegisterCandidate(WebSocketSession session,
                                         TextMessage message) {
        SessionData<TranslationNeed> sessionData = findTranslatorRepository.sessions().get(session);
        if (sessionData != null) {
            sessionData.candidateMessage(message);
        }
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        findTranslatorRepository.sessions().put(session, new SessionData<>(null, null, null));
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session,
                                      @NonNull CloseStatus status) {
        if (findTranslatorRepository.sessions().remove(session) == null) {
            findTranslatorRepository.sessions().keySet().removeIf(s -> s.getId().equals(session.getId()));
        }
    }

}
