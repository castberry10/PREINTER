package com.interviewee.preinter.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitAnswerRequest {
    private String sessionId;
    private int questionNumber;
    private String answer;
}
