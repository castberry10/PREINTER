package com.interviewee.preinter.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetNextQuestionResponse {
    private String sessionId;
    private int questionNumber;
    private String question;
    private boolean interviewEnded;

    public GetNextQuestionResponse(String question, boolean interviewEnded) {
        this.question = question;
        this.interviewEnded = interviewEnded;
    }
}
