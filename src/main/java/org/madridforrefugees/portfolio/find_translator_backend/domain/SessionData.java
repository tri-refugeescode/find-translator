package org.madridforrefugees.portfolio.find_translator_backend.domain;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.web.socket.TextMessage;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SessionData<T> {
    private T translationInfo;
    private TextMessage rtcMessage;
    @Setter(AccessLevel.NONE)
    private final List<TextMessage> candidateMessages = new LinkedList<>();

    public void addCandidateMessage(TextMessage candidateMessage) {
        this.candidateMessages.add(candidateMessage);
    }

    public boolean hasInfo() {
        return translationInfo != null;
    }

    public boolean isComplete() {
        return translationInfo != null && rtcMessage != null && !candidateMessages.isEmpty();
    }

}
