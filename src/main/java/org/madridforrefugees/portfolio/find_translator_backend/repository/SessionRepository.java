package org.madridforrefugees.portfolio.find_translator_backend.repository;

import org.madridforrefugees.portfolio.find_translator_backend.domain.SessionData;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

public record SessionRepository<T>(Map<WebSocketSession, SessionData<T>> sessions) {
}
