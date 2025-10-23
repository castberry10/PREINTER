package com.interviewee.preinter.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalyticsRequest {
    private String sessionId;

    public AnalyticsRequest(String sessionId){
        this.sessionId = sessionId;
    }
}
