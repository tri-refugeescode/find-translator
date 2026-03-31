package org.madridforrefugees.portfolio.find_translator_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.madridforrefugees.portfolio.find_translator_backend.domain.SessionData;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationCapability;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationNeed;
import org.madridforrefugees.portfolio.find_translator_backend.repository.SessionRepository;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class TranslatorMatchingService {

    private final SessionRepository<TranslationNeed> findTranslatorRepository;
    private final SessionRepository<TranslationCapability> offerTranslationRepository;

    public boolean matchOnOffer(WebSocketSession offerSession,
                                SessionData<TranslationCapability> offerData) {
        var matchedFindSession = findTranslatorRepository.sessions().entrySet().stream()
                .filter(e -> e.getValue().getTranslationInfo() != null)
                .filter(e -> translationPossible(e.getValue().getTranslationInfo(), offerData.getTranslationInfo()))
                .findAny();
        if (matchedFindSession.isPresent()) {
            var findSession = matchedFindSession.get().getKey();
            var findData = matchedFindSession.get().getValue();

            exchangeMessages(findSession, findData, offerSession, offerData);
            return true;
        } else {
            return false;
        }
    }

    public boolean matchOnFind(WebSocketSession findSession,
                               SessionData<TranslationNeed> findData) {
        var matchedOfferSession = offerTranslationRepository.sessions().entrySet().stream()
                .filter(e -> e.getValue().getTranslationInfo() != null)
                .filter(e -> translationPossible(findData.getTranslationInfo(), e.getValue().getTranslationInfo()))
                .findAny();
        if (matchedOfferSession.isPresent()) {
            var offerSession = matchedOfferSession.get().getKey();
            var offerData = matchedOfferSession.get().getValue();

            exchangeMessages(findSession, findData, offerSession, offerData);
            return true;
        } else {
            return false;
        }
    }

    private void exchangeMessages(WebSocketSession findSession,
                                  SessionData<TranslationNeed> findData,
                                  WebSocketSession offerSession,
                                  SessionData<TranslationCapability> offerData) {
        if (findSession.isOpen() && offerSession.isOpen()) {
            findTranslatorRepository.sessions().remove(findSession);
            offerTranslationRepository.sessions().remove(offerSession);

            try {
                findSession.sendMessage(offerData.getInfoMessage());
                offerSession.sendMessage(findData.getInfoMessage());
                findSession.sendMessage(offerData.getCandidateMessage());
                offerSession.sendMessage(findData.getCandidateMessage());
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
