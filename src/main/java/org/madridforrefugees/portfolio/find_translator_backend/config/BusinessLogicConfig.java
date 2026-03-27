package org.madridforrefugees.portfolio.find_translator_backend.config;

import lombok.RequiredArgsConstructor;
import org.madridforrefugees.portfolio.find_translator_backend.handler.FindTranslatorHandler;
import org.madridforrefugees.portfolio.find_translator_backend.handler.OfferTranslationHandler;
import org.madridforrefugees.portfolio.find_translator_backend.repository.FindTranslatorRepository;
import org.madridforrefugees.portfolio.find_translator_backend.repository.OfferTranslationRepository;
import org.madridforrefugees.portfolio.find_translator_backend.service.TranslatorMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;

@Configuration
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class BusinessLogicConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public FindTranslatorRepository findTranslatorRepository() {
        return new FindTranslatorRepository(new HashMap<>());
    }

    @Bean
    public OfferTranslationRepository offerTranslationRepository() {
        return new OfferTranslationRepository(new HashMap<>());
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

}
