package com.interviewee.preinter.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartInterviewResponse {
    private String sessionId;

    public StartInterviewResponse(String sessionId) {
        this.sessionId = sessionId;
    }
}
