package org.madridforrefugees.portfolio.find_translator_backend.handler;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationCapability;
import org.madridforrefugees.portfolio.find_translator_backend.repository.OfferTranslationRepository;
import org.madridforrefugees.portfolio.find_translator_backend.service.TranslatorMatchingService;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public class OfferTranslationHandler extends TextWebSocketHandler {

    private final OfferTranslationRepository offerTranslationRepository;
    private final ObjectMapper objectMapper;
    private final TranslatorMatchingService translatorMatchingService;

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session,
                                  @NonNull TextMessage message) {
        var messageContent = objectMapper.readTree(message.getPayload());
        var eventType = messageContent.path("eventType").stringValue();
        if (eventType.equals("offer-translation")) {
            handleOfferTranslation(session, message, messageContent);
        } else if (eventType.equals("register-candidate")) {
            handleRegisterCandidate(session, message);
        }
    }

    private void handleOfferTranslation(WebSocketSession session,
                                        TextMessage message,
                                        JsonNode messageContent) {
        var translationCapability = objectMapper.treeToValue(messageContent.path("translationCapability"), TranslationCapability.class);
        if (translationCapability.isValid()) {
            offerTranslationRepository.sessions().put(session, Pair.of(message, translationCapability));
            translatorMatchingService.matchOnOffer(translationCapability, session, message);
        }
    }

    private void handleRegisterCandidate(WebSocketSession session,
                                         TextMessage message) {
        translatorMatchingService.forwardIceCandidate(session, false, message);
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        offerTranslationRepository.sessions().put(session, Pair.of(null, null));
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session,
                                      @NonNull CloseStatus status) {
        if (offerTranslationRepository.sessions().remove(session) == null) {
            offerTranslationRepository.sessions().keySet().removeIf(s -> s.getId().equals(session.getId()));
        }
    }

}
