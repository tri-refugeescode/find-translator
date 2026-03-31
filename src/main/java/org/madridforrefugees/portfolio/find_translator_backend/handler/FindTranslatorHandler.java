package org.madridforrefugees.portfolio.find_translator_backend.handler;

import org.jspecify.annotations.NonNull;
import org.madridforrefugees.portfolio.find_translator_backend.domain.EventType;
import org.madridforrefugees.portfolio.find_translator_backend.domain.SessionData;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationNeed;
import org.madridforrefugees.portfolio.find_translator_backend.repository.SessionRepository;
import org.madridforrefugees.portfolio.find_translator_backend.service.TranslatorMatchingService;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.madridforrefugees.portfolio.find_translator_backend.domain.EventType.FIND_TRANSLATOR;
import static org.madridforrefugees.portfolio.find_translator_backend.domain.EventType.REGISTER_CANDIDATE;

public class FindTranslatorHandler extends BaseHandler<TranslationNeed> {

    public FindTranslatorHandler(SessionRepository<TranslationNeed> findTranslatorRepository,
                                 ObjectMapper objectMapper,
                                 TranslatorMatchingService translatorMatchingService) {
        super(findTranslatorRepository, objectMapper, translatorMatchingService);
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session,
                                  @NonNull TextMessage message) {
        var messageContent = objectMapper.readTree(message.getPayload());
        var eventType = messageContent.path(EventType.PATH).stringValue();
        if (FIND_TRANSLATOR.text().equals(eventType)) {
            handleFindTranslator(session, message, messageContent);
        } else if (REGISTER_CANDIDATE.text().equals(eventType)) {
            handleRegisterCandidate(session, message);
        }
    }

    private void handleFindTranslator(WebSocketSession session,
                                      TextMessage message,
                                      JsonNode messageContent) {
        var translationNeed = objectMapper.treeToValue(messageContent.path(TranslationNeed.PATH), TranslationNeed.class);
        if (translationNeed.isValid()) {
            sessionRepository.sessions().put(session, new SessionData<>(translationNeed, message, null));
        }
    }

    void handleRegisterCandidate(WebSocketSession session,
                                 TextMessage message) {
        SessionData<TranslationNeed> sessionData = sessionRepository.sessions().get(session);
        if (sessionData != null) {
            sessionData.setCandidateMessage(message);
            translatorMatchingService.matchOnFind(session, sessionData);
        }
    }

}
