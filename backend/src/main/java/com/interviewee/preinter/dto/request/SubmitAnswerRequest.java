package com.interviewee.preinter.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitAnswerRequest {
    private String sessionId;
    private String SpeedScore;
    private String answer;

    public SubmitAnswerRequest(String sessionId, String answer) {
        this.sessionId = sessionId;
        this.answer = answer;
    }
}
