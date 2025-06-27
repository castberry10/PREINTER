package com.interviewee.preinter.controller;

import com.interviewee.preinter.dto.*;
import com.interviewee.preinter.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService service;

    @PostMapping("/start")
    public ResponseEntity<InterviewStartResponse> start(@RequestBody InterviewStartRequest req) {
        return ResponseEntity.ok(service.startInterview(req));
    }

    @GetMapping("/{id}/question")
    public ResponseEntity<QuestionResponse> question(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getNextQuestion(id));
    }

    @PostMapping("/{id}/answer")
    public ResponseEntity<Void> answer(@PathVariable UUID id,
                                       @RequestBody AnswerRequest req) {
        service.submitAnswer(id, req);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<ResultResponse> result(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getResult(id));
    }
}
