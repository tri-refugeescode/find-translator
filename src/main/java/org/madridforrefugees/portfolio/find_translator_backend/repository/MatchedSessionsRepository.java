package org.madridforrefugees.portfolio.find_translator_backend.repository;

import org.apache.commons.lang3.tuple.Triple;
import org.madridforrefugees.portfolio.find_translator_backend.domain.SessionData;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationCapability;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationNeed;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

public record MatchedSessionsRepository(
        Map<WebSocketSession, Triple<SessionData<TranslationNeed>, WebSocketSession, SessionData<TranslationCapability>>> matchedSessions) {
}
