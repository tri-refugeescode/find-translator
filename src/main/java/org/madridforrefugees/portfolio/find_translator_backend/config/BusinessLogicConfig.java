package org.madridforrefugees.portfolio.find_translator_backend.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.madridforrefugees.portfolio.find_translator_backend.domain.SessionData;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationCapability;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationNeed;
import org.madridforrefugees.portfolio.find_translator_backend.handler.FindTranslatorHandler;
import org.madridforrefugees.portfolio.find_translator_backend.handler.OfferTranslationHandler;
import org.madridforrefugees.portfolio.find_translator_backend.repository.MatchedSessionsRepository;
import org.madridforrefugees.portfolio.find_translator_backend.repository.SessionRepository;
import org.madridforrefugees.portfolio.find_translator_backend.service.TranslatorMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

@Configuration
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class BusinessLogicConfig {

    private final ObjectMapper objectMapper;
    private final TranslatorMatchingProperties translatorMatchingProperties;

    @Bean
    public SessionRepository<TranslationNeed> findTranslatorRepository() {
        Cache<WebSocketSession, SessionData<TranslationNeed>> cache = Caffeine.newBuilder()
                .expireAfterWrite(translatorMatchingProperties.findActiveTime())
                .maximumSize(translatorMatchingProperties.maxFindSessions())
                .evictionListener(BusinessLogicConfig::closeSessionOnEviction)
                .weakKeys()
                .build();
        return new SessionRepository<>(cache.asMap());
    }

    @Bean
    public SessionRepository<TranslationCapability> offerTranslationRepository() {
        Cache<WebSocketSession, SessionData<TranslationCapability>> cache = Caffeine.newBuilder()
                .expireAfterWrite(translatorMatchingProperties.offerActiveTime())
                .maximumSize(translatorMatchingProperties.maxOfferSessions())
                .evictionListener(BusinessLogicConfig::closeSessionOnEviction)
                .weakKeys()
                .build();
        return new SessionRepository<>(cache.asMap());
    }

    @Bean
    public MatchedSessionsRepository matchedSessionsRepository() {
        Cache<WebSocketSession, Triple<SessionData<TranslationNeed>, WebSocketSession, SessionData<TranslationCapability>>> cache = Caffeine.newBuilder()
                .expireAfterWrite(translatorMatchingProperties.matchedActiveTime())
                .maximumSize(translatorMatchingProperties.maxFindSessions())
                .weakKeys()
                .build();
        return new MatchedSessionsRepository(cache.asMap());
    }

    @Bean
    public TranslatorMatchingService translatorMatchingService(SessionRepository<TranslationNeed> findTranslatorRepository,
                                                               SessionRepository<TranslationCapability> offerTranslationRepository,
                                                               MatchedSessionsRepository matchedSessionsRepository) {
        return new TranslatorMatchingService(findTranslatorRepository, offerTranslationRepository, matchedSessionsRepository);
    }

    @Bean
    public FindTranslatorHandler findTranslatorHandler(SessionRepository<TranslationNeed> findTranslatorRepository,
                                                       MatchedSessionsRepository matchedSessionsRepository,
                                                       TranslatorMatchingService translatorMatchingService) {
        return new FindTranslatorHandler(findTranslatorRepository, matchedSessionsRepository, objectMapper, translatorMatchingService);
    }

    @Bean
    public OfferTranslationHandler offerTranslationHandler(SessionRepository<TranslationCapability> offerTranslationRepository,
                                                           TranslatorMatchingService translatorMatchingService) {
        return new OfferTranslationHandler(offerTranslationRepository, objectMapper, translatorMatchingService);
    }

    private static void closeSessionOnEviction(WebSocketSession session,
                                               SessionData<?> sessionData,
                                               RemovalCause cause) {
        if (session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                log.error("Error closing WebSocket session upon cache eviction", e);
            }
        }
    }

}
