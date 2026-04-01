package org.madridforrefugees.portfolio.find_translator_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.socket.TextMessage;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SessionData<T> {
    private T translationInfo;
    private TextMessage rtcMessage;
    private TextMessage candidateMessage;

    public boolean hasInfo() {
        return translationInfo != null;
    }

    public boolean isComplete() {
        return translationInfo != null && rtcMessage != null && candidateMessage != null;
    }

}
