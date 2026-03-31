package org.madridforrefugees.portfolio.find_translator_backend.handler;

import org.jspecify.annotations.NonNull;
import org.madridforrefugees.portfolio.find_translator_backend.domain.EventType;
import org.madridforrefugees.portfolio.find_translator_backend.domain.SessionData;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationCapability;
import org.madridforrefugees.portfolio.find_translator_backend.repository.SessionRepository;
import org.madridforrefugees.portfolio.find_translator_backend.service.TranslatorMatchingService;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.madridforrefugees.portfolio.find_translator_backend.domain.EventType.OFFER_TRANSLATION;
import static org.madridforrefugees.portfolio.find_translator_backend.domain.EventType.REGISTER_CANDIDATE;

public class OfferTranslationHandler extends BaseHandler<TranslationCapability> {


    public OfferTranslationHandler(SessionRepository<TranslationCapability> offerTranslationRepository,
                                   ObjectMapper objectMapper,
                                   TranslatorMatchingService translatorMatchingService) {
        super(offerTranslationRepository, objectMapper, translatorMatchingService);
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session,
                                  @NonNull TextMessage message) {
        var messageContent = objectMapper.readTree(message.getPayload());
        var eventType = messageContent.path(EventType.PATH).stringValue();
        if (OFFER_TRANSLATION.text().equals(eventType)) {
            handleOfferTranslation(session, message, messageContent);
        } else if (REGISTER_CANDIDATE.text().equals(eventType)) {
            handleRegisterCandidate(session, message);
        }
    }

    private void handleOfferTranslation(WebSocketSession session,
                                        TextMessage message,
                                        JsonNode messageContent) {
        var translationCapability = objectMapper.treeToValue(messageContent.path(TranslationCapability.PATH), TranslationCapability.class);
        if (translationCapability.isValid()) {
            sessionRepository.sessions().put(session, new SessionData<>(translationCapability, message, null));
        }
    }

    void handleRegisterCandidate(WebSocketSession session,
                                 TextMessage message) {
        SessionData<TranslationCapability> sessionData = sessionRepository.sessions().get(session);
        if (sessionData != null) {
            sessionData.setCandidateMessage(message);
            translatorMatchingService.matchOnOffer(session, sessionData);
        }
    }

}
