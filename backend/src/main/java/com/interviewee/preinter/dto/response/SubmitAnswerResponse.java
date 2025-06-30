package com.interviewee.preinter.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitAnswerResponse {
    // 잘 받았다는 표시
    private String status;

    public SubmitAnswerResponse(String status) {
        this.status = status;
    }
}
