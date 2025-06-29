package com.aoaoaoqq.controller;

import com.interviewee.preinter.dto.AnswerRequest;
import com.interviewee.preinter.dto.InterviewStartRequest;
import com.interviewee.preinter.dto.QuestionResponse;
import com.interviewee.preinter.dto.ResultResponse;
import com.interviewee.preinter.openai.ChatService;
import com.interviewee.preinter.service.InterviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InterviewControllerTest {
    private ChatService mockChat;
    private InterviewService service;

    @BeforeEach
    void setUp() {
        mockChat = mock(ChatService.class);
        service = new InterviewService(mockChat);
    }

    @Test
    void fullInterviewFlow_shouldWork() {
        // Start
        InterviewStartRequest startReq = new InterviewStartRequest();
        startReq.setResume("이력서 내용");
        UUID id = service.startInterview(startReq).getInterviewId();

        // 첫 질문 모킹
        when(mockChat.ask(contains("첫 질문"))).thenReturn("첫 질문 내용");
        QuestionResponse q1 = service.getNextQuestion(id);
        assertThat(q1.getQuestion()).isEqualTo("첫 질문 내용");

        // 첫 답변
        AnswerRequest ans1 = new AnswerRequest();
        ans1.setQuestionNumber(q1.getQuestionNumber());
        ans1.setAnswer("답변1");
        service.submitAnswer(id, ans1);

        // 두번째 질문 모킹
        when(mockChat.ask(contains("대화 기록"))).thenReturn("두번째 질문 내용");
        QuestionResponse q2 = service.getNextQuestion(id);
        assertThat(q2.getQuestion()).isEqualTo("두번째 질문 내용");

        // 두번째 답변
        AnswerRequest ans2 = new AnswerRequest();
        ans2.setQuestionNumber(q2.getQuestionNumber());
        ans2.setAnswer("답변2");
        service.submitAnswer(id, ans2);

        // 결과 평가 모킹
        when(mockChat.ask(contains("이력서를 참고"))).thenReturn("종합 평가 결과");
        ResultResponse result = service.getResult(id);
        assertThat(result.getResultSummary()).isEqualTo("종합 평가 결과");
    }
}
