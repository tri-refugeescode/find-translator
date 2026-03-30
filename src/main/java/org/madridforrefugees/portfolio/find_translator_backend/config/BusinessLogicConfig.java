package org.madridforrefugees.portfolio.find_translator_backend.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationCapability;
import org.madridforrefugees.portfolio.find_translator_backend.domain.TranslationNeed;
import org.madridforrefugees.portfolio.find_translator_backend.handler.FindTranslatorHandler;
import org.madridforrefugees.portfolio.find_translator_backend.handler.OfferTranslationHandler;
import org.madridforrefugees.portfolio.find_translator_backend.repository.FindTranslatorRepository;
import org.madridforrefugees.portfolio.find_translator_backend.repository.OfferTranslationRepository;
import org.madridforrefugees.portfolio.find_translator_backend.service.TranslatorMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

@Configuration
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class BusinessLogicConfig {

    private final ObjectMapper objectMapper;
    private final TranslatorMatchingProperties translatorMatchingProperties;

    @Bean
    public FindTranslatorRepository findTranslatorRepository() {
        Cache<WebSocketSession, Pair<TextMessage, TranslationNeed>> cache = Caffeine.newBuilder()
                .expireAfterWrite(translatorMatchingProperties.findActiveTime())
                .maximumSize(translatorMatchingProperties.maxFindSessions())
                .evictionListener(BusinessLogicConfig::closeSessionOnEviction)
                .weakKeys()
                .build();
        return new FindTranslatorRepository(cache.asMap());
    }

    @Bean
    public OfferTranslationRepository offerTranslationRepository() {
        Cache<WebSocketSession, Pair<TextMessage, TranslationCapability>> cache = Caffeine.newBuilder()
                .expireAfterWrite(translatorMatchingProperties.offerActiveTime())
                .maximumSize(translatorMatchingProperties.maxOfferSessions())
                .evictionListener(BusinessLogicConfig::closeSessionOnEviction)
                .weakKeys()
                .build();
        return new OfferTranslationRepository(cache.asMap());
    }

    @Bean
    public TranslatorMatchingService translatorMatchingService(FindTranslatorRepository findTranslatorRepository,
                                                               OfferTranslationRepository offerTranslationRepository) {
        return new TranslatorMatchingService(findTranslatorRepository, offerTranslationRepository);
    }

    @Bean
    public FindTranslatorHandler findTranslatorHandler(FindTranslatorRepository findTranslatorRepository,
                                                       TranslatorMatchingService translatorMatchingService) {
        return new FindTranslatorHandler(findTranslatorRepository, objectMapper, translatorMatchingService);
    }

    @Bean
    public OfferTranslationHandler offerTranslationHandler(OfferTranslationRepository offerTranslationRepository,
                                                           TranslatorMatchingService translatorMatchingService) {
        return new OfferTranslationHandler(offerTranslationRepository, objectMapper, translatorMatchingService);
    }

    private static void closeSessionOnEviction(WebSocketSession session,
                                               Pair<?, ?> message,
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
