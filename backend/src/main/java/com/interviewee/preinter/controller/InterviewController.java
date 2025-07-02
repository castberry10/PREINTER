package com.interviewee.preinter.controller;

import com.interviewee.preinter.dto.request.*;
import com.interviewee.preinter.dto.response.*;
import com.interviewee.preinter.interview.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /** 1) 인터뷰 시작 */
    @PostMapping("/start")
    public ResponseEntity<StartInterviewResponse> startInterview(
            @RequestBody StartInterviewRequest request
    ) {
        StartInterviewResponse response = interviewService.startInterview(request);
        return ResponseEntity.ok(response);
    }

    /** 2) 다음 질문 요청 */
    @PostMapping("/question")
    public ResponseEntity<GetNextQuestionResponse> getNextQuestion(
            @RequestBody GetNextQuestionRequest request
    ) {
        GetNextQuestionResponse response = interviewService.getNextQuestion(request);
        return ResponseEntity.ok(response);
    }

    /** 3) 답변 제출 */
    @PostMapping("/answer")
    public ResponseEntity<SubmitAnswerResponse> submitAnswer(
            @RequestBody SubmitAnswerRequest request
    ) {
        SubmitAnswerResponse response = interviewService.submitAnswer(request);
        return ResponseEntity.ok(response);
    }

    /** 4) 결과(요약) 요청 */
    @PostMapping("/result")
    public ResponseEntity<GetResultResponse> getResult(
            @RequestBody GetResultRequest request
    ) {
        GetResultResponse response = interviewService.getResult(request);
        return ResponseEntity.ok(response);
    }
}
