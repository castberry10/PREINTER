package com.interviewee.preinter.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetNextQuestionRequest {
    private String sessionId;

    public GetNextQuestionRequest(String sessionId) {
        this.sessionId = sessionId;
    }
}
