package com.interviewee.preinter.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResultResponse {
    private UUID interviewId;
    private String resultSummary;
}
