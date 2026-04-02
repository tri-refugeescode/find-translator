package org.madridforrefugees.portfolio.find_translator_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.madridforrefugees.portfolio.find_translator_backend.domain.SessionData;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationCapability;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationNeed;
import org.madridforrefugees.portfolio.find_translator_backend.repository.MatchedSessionsRepository;
import org.madridforrefugees.portfolio.find_translator_backend.repository.SessionRepository;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class TranslatorMatchingService {

    private final SessionRepository<TranslationNeed> findTranslatorRepository;
    private final SessionRepository<TranslationCapability> offerTranslationRepository;
    private final MatchedSessionsRepository matchedSessionsRepository;

    public void matchOnOffer(WebSocketSession offerSession,
                             SessionData<TranslationCapability> offerData) {
        var matchedFindSession = findTranslatorRepository.sessions().entrySet().stream()
                .filter(e -> e.getValue().hasInfo())
                .filter(e -> translationPossible(e.getValue().getTranslationInfo(), offerData.getTranslationInfo()))
                .findAny();
        if (matchedFindSession.isPresent()) {
            var findSession = matchedFindSession.get().getKey();
            var findData = matchedFindSession.get().getValue();

            moveToMatchedSessions(findSession, findData, offerSession, offerData);
            exchangeOffer(findSession, offerData);
        }
    }

    public void matchOnFind(WebSocketSession findSession,
                            SessionData<TranslationNeed> findData) {
        var matchedOfferSession = offerTranslationRepository.sessions().entrySet().stream()
                .filter(e -> e.getValue().isComplete())
                .filter(e -> translationPossible(findData.getTranslationInfo(), e.getValue().getTranslationInfo()))
                .findAny();
        if (matchedOfferSession.isPresent()) {
            var offerSession = matchedOfferSession.get().getKey();
            var offerData = matchedOfferSession.get().getValue();

            moveToMatchedSessions(findSession, findData, offerSession, offerData);
            exchangeOffer(findSession, offerData);
        }
    }

    private boolean translationPossible(TranslationNeed need, TranslationCapability capability) {
        return capability.proficientLanguages().stream().anyMatch(l -> need.understoodLanguages().contains(l))
                && capability.proficientLanguages().stream().anyMatch(l -> need.requiredLanguages().contains(l));
    }

    private void moveToMatchedSessions(WebSocketSession findSession,
                                       SessionData<TranslationNeed> findData,
                                       WebSocketSession offerSession,
                                       SessionData<TranslationCapability> offerData) {
        findTranslatorRepository.sessions().remove(findSession);
        offerTranslationRepository.sessions().remove(offerSession);
        matchedSessionsRepository.matchedSessions().put(findSession, Triple.of(findData, offerSession, offerData));
    }

    private void exchangeOffer(WebSocketSession findSession,
                               SessionData<TranslationCapability> offerData) {
        if (findSession.isOpen()) {
            try {
                findSession.sendMessage(offerData.getRtcMessage());
                for (var candidate : filterOutNulls(offerData.getCandidateMessages())) {
                    findSession.sendMessage(candidate);
                }
            } catch (IOException e) {
                log.error("Error exchanging offer message to find session: {}", findSession.getId(), e);
            }
        }
    }

    public void exchangeAccept(Triple<SessionData<TranslationNeed>, WebSocketSession, SessionData<TranslationCapability>> data) {
        if (data.getMiddle().isOpen()) {
            var offerSession = data.getMiddle();
            var findData = data.getLeft();
            try {
                offerSession.sendMessage(findData.getRtcMessage());
                for (var candidate : filterOutNulls(findData.getCandidateMessages())) {
                    offerSession.sendMessage(candidate);
                }
            } catch (IOException e) {
                log.error("Error exchanging accept message to offer session: {}", offerSession.getId(), e);
            }
        }
    }

    private List<TextMessage> filterOutNulls(List<TextMessage> candidateMessages) {
        return candidateMessages.stream().filter(Objects::nonNull).toList();
    }

}
