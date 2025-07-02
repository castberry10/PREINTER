package com.interviewee.preinter.dto.request;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartInterviewRequest {
    private MultipartFile resumeFile;

    public StartInterviewRequest(MultipartFile resumeFile) {
        this.resumeFile = resumeFile;
    }
}
