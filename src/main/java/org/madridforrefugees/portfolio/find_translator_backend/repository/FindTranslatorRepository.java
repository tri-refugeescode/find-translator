package org.madridforrefugees.portfolio.find_translator_backend.repository;

import org.apache.commons.lang3.tuple.Pair;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationNeed;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

public record FindTranslatorRepository(Map<WebSocketSession, Pair<TextMessage, TranslationNeed>> sessions) {
}
