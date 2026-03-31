package org.madridforrefugees.portfolio.find_translator_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.socket.TextMessage;

@Getter
@Setter
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class SessionData<T> {
    private T translationInfo;
    private TextMessage infoMessage;
    private TextMessage candidateMessage;
}
