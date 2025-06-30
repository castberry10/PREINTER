package com.interviewee.preinter.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetResultResponse {
    private String closingMessage;
    private String evaluationSummary;

    public GetResultResponse(String evaluationSummary, String closingMessage) {
        this.evaluationSummary = evaluationSummary;
        this.closingMessage = closingMessage;
    }
}
