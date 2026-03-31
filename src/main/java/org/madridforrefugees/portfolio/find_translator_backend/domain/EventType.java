package org.madridforrefugees.portfolio.find_translator_backend.domain;

import lombok.Getter;

@Getter
public enum EventType {
    FIND_TRANSLATOR("find-translator"),
    OFFER_TRANSLATION("offer-translation"),
    REGISTER_CANDIDATE("register-candidate");

    public static final String PATH = "eventType";
    private final String name;

    EventType(String name) {
        this.name = name;
    }
}
