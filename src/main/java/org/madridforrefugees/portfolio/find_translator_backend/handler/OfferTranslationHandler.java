package org.madridforrefugees.portfolio.find_translator_backend.handler;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.madridforrefugees.portfolio.find_translator_backend.domain.EventType;
import org.madridforrefugees.portfolio.find_translator_backend.domain.SessionData;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationCapability;
import org.madridforrefugees.portfolio.find_translator_backend.repository.SessionRepository;
import org.madridforrefugees.portfolio.find_translator_backend.service.TranslatorMatchingService;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.madridforrefugees.portfolio.find_translator_backend.domain.EventType.OFFER_TRANSLATION;
import static org.madridforrefugees.portfolio.find_translator_backend.domain.EventType.REGISTER_CANDIDATE;

@RequiredArgsConstructor
public class OfferTranslationHandler extends TextWebSocketHandler {

    private final SessionRepository<TranslationCapability> offerTranslationRepository;
    private final ObjectMapper objectMapper;
    private final TranslatorMatchingService translatorMatchingService;

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session,
                                  @NonNull TextMessage message) {
        var messageContent = objectMapper.readTree(message.getPayload());
        var eventType = messageContent.path(EventType.PATH).stringValue();
        if (OFFER_TRANSLATION.name().equals(eventType)) {
            handleOfferTranslation(session, message, messageContent);
        } else if (REGISTER_CANDIDATE.name().equals(eventType)) {
            handleRegisterCandidate(session, message);
        }
    }

    private void handleOfferTranslation(WebSocketSession session,
                                        TextMessage message,
                                        JsonNode messageContent) {
        var translationCapability = objectMapper.treeToValue(messageContent.path(TranslationCapability.PATH), TranslationCapability.class);
        if (translationCapability.isValid()) {
            offerTranslationRepository.sessions().put(session, new SessionData<>(translationCapability, message, null));
            translatorMatchingService.matchOnOffer(translationCapability, session, message);
        }
    }

    private void handleRegisterCandidate(WebSocketSession session,
                                         TextMessage message) {
        SessionData<TranslationCapability> sessionData = offerTranslationRepository.sessions().get(session);
        if (sessionData != null) {
            sessionData.candidateMessage(message);
        }
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        offerTranslationRepository.sessions().put(session, new SessionData<>(null, null, null));
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session,
                                      @NonNull CloseStatus status) {
        if (offerTranslationRepository.sessions().remove(session) == null) {
            offerTranslationRepository.sessions().keySet().removeIf(s -> s.getId().equals(session.getId()));
        }
    }

}
