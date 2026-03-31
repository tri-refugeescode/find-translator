package org.madridforrefugees.portfolio.find_translator_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationCapability;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationNeed;
import org.madridforrefugees.portfolio.find_translator_backend.repository.SessionRepository;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class TranslatorMatchingService {

    private final SessionRepository<TranslationNeed> findTranslatorRepository;
    private final SessionRepository<TranslationCapability> offerTranslationRepository;

    public boolean matchOnOffer(TranslationCapability capability,
                                WebSocketSession offerSession,
                                TextMessage offerMessage) {
        var matchedFindSession = findTranslatorRepository.sessions().entrySet().stream()
                .filter(e -> e.getValue().translationInfo() != null)
                .filter(e -> translationPossible(e.getValue().translationInfo(), capability))
                .findAny();
        if (matchedFindSession.isPresent()) {
            var findSession = matchedFindSession.get().getKey();
            var findMessage = matchedFindSession.get().getValue().infoMessage();

            exchangeMessages(offerSession, offerMessage, findSession, findMessage);
            return true;
        } else {
            return false;
        }
    }

    public boolean matchOnFind(TranslationNeed need,
                               WebSocketSession findSession,
                               TextMessage findMessage) {
        var matchedOfferSession = offerTranslationRepository.sessions().entrySet().stream()
                .filter(e -> e.getValue().translationInfo() != null)
                .filter(e -> translationPossible(need, e.getValue().translationInfo()))
                .findAny();
        if (matchedOfferSession.isPresent()) {
            var offerSession = matchedOfferSession.get().getKey();
            var offerMessage = matchedOfferSession.get().getValue().infoMessage();

            exchangeMessages(offerSession, offerMessage, findSession, findMessage);
            return true;
        } else {
            return false;
        }
    }

    private void exchangeMessages(WebSocketSession offerSession,
                                  TextMessage offerMessage,
                                  WebSocketSession findSession,
                                  TextMessage findMessage) {
        if (findSession.isOpen() && offerSession.isOpen()) {
            findTranslatorRepository.sessions().remove(findSession);
            offerTranslationRepository.sessions().remove(offerSession);

            try {
                findSession.sendMessage(offerMessage);
                offerSession.sendMessage(findMessage);
            } catch (IOException e) {
                log.error("Error exchanging messages between find & offer sessions: {} and {}",
                        findSession.getId(), offerSession.getId(), e);
            }
        }
    }

    private boolean translationPossible(TranslationNeed need, TranslationCapability capability) {
        return capability.proficientLanguages().stream().anyMatch(l -> need.understoodLanguages().contains(l))
                && capability.proficientLanguages().stream().anyMatch(l -> need.requiredLanguages().contains(l));
    }

}
