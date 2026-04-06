package org.madridforrefugees.portfolio.find_translator_backend.handler;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.madridforrefugees.portfolio.find_translator_backend.domain.EventType;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationNeed;
import org.madridforrefugees.portfolio.find_translator_backend.repository.MatchedSessionsRepository;
import org.madridforrefugees.portfolio.find_translator_backend.repository.SessionRepository;
import org.madridforrefugees.portfolio.find_translator_backend.service.TranslatorMatchingService;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.madridforrefugees.portfolio.find_translator_backend.domain.EventType.*;

@Slf4j
public class FindTranslatorHandler extends BaseHandler<TranslationNeed> {

    private final MatchedSessionsRepository matchedSessionsRepository;

    public FindTranslatorHandler(SessionRepository<TranslationNeed> findTranslatorRepository,
                                 MatchedSessionsRepository matchedSessionsRepository,
                                 ObjectMapper objectMapper,
                                 TranslatorMatchingService translatorMatchingService) {
        super(findTranslatorRepository, objectMapper, translatorMatchingService);
        this.matchedSessionsRepository = matchedSessionsRepository;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session,
                                  @NonNull TextMessage message) {
        var messageContent = objectMapper.readTree(message.getPayload());
        var eventType = messageContent.path(EventType.PATH).stringValue();
        if (FIND_TRANSLATOR.text().equals(eventType)) {
            handleFindTranslator(session, messageContent);
        } else if (ACCEPT_TRANSLATOR.text().equals(eventType)) {
            handleAcceptTranslator(session, message);
        } else if (REGISTER_CANDIDATE.text().equals(eventType)) {
            if (messageContent.hasNonNull(EventType.ICE_PATH)) {
                handleRegisterCandidate(session, message);
            } else {
                handleEndCandidates(session);
            }
        }
    }

    private void handleFindTranslator(WebSocketSession session,
                                      JsonNode messageContent) {
        var translationNeed = objectMapper.treeToValue(messageContent.path(TranslationNeed.PATH), TranslationNeed.class);
        var sessionData = sessionRepository.sessions().get(session);
        if (sessionData != null && translationNeed.isValid()) {
            sessionData.setTranslationInfo(translationNeed);
            log.info("New {} set in find translator sessions (current size={})",
                    translationNeed, sessionRepository.sessions().size());

            translatorMatchingService.matchOnFind(session, sessionData);
        }
    }

    private void handleAcceptTranslator(WebSocketSession session,
                                        TextMessage message) {
        var data = matchedSessionsRepository.matchedSessions().get(session);
        if (data != null) {
            data.getLeft().setRtcMessage(message);

            if (data.getLeft().getCandidateMessages().contains(null)) {
                translatorMatchingService.exchangeAccept(data);
            }
        }
    }

    private void handleRegisterCandidate(WebSocketSession session,
                                         TextMessage message) {
        var data = matchedSessionsRepository.matchedSessions().get(session);
        if (data != null) {
            data.getLeft().addCandidateMessage(message);
        }
    }

    private void handleEndCandidates(WebSocketSession session) {
        var data = matchedSessionsRepository.matchedSessions().get(session);
        if (data != null) {
            data.getLeft().addCandidateMessage(null); // null element indicates end of candidates

            if (data.getLeft().getRtcMessage() != null) {
                translatorMatchingService.exchangeAccept(data);
            }
        }
    }

}
