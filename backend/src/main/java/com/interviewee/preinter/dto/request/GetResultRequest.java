package com.interviewee.preinter.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetResultRequest {
    private String sessionId;

    public GetResultRequest(String sessionId) {
        this.sessionId = sessionId;
    }
}
