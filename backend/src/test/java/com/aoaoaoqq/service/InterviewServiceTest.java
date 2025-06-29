package com.aoaoaoqq.service;

import com.interviewee.preinter.dto.*;
import com.interviewee.preinter.openai.ChatService;
import com.interviewee.preinter.service.InterviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InterviewServiceTest {

    private ChatService mockChatService;
    private InterviewService service;

    @BeforeEach
    void setUp() {
        mockChatService = mock(ChatService.class);
        service = new InterviewService(mockChatService);
    }

    @Test
    void fullInterviewFlow_shouldWork() {
        // 1) 시작
        InterviewStartRequest startReq = new InterviewStartRequest();
        startReq.setResume("이력서 내용");
        UUID id = service.startInterview(startReq).getInterviewId();

        // 2) 첫 질문
        when(mockChatService.ask(contains("첫 질문")))
                .thenReturn("첫 질문 내용");
        QuestionResponse q1 = service.getNextQuestion(id);
        assertThat(q1.getQuestion()).isEqualTo("첫 질문 내용");

        // 3) 첫 답변
        AnswerRequest ans1 = new AnswerRequest();
        ans1.setQuestionNumber(q1.getQuestionNumber());
        ans1.setAnswer("답변1");
        service.submitAnswer(id, ans1);

        // 4) 두번째 질문
        when(mockChatService.ask(contains("대화 기록")))
                .thenReturn("두번째 질문 내용");
        QuestionResponse q2 = service.getNextQuestion(id);
        assertThat(q2.getQuestion()).isEqualTo("두번째 질문 내용");

        // 5) 두번째 답변
        AnswerRequest ans2 = new AnswerRequest();
        ans2.setQuestionNumber(q2.getQuestionNumber());
        ans2.setAnswer("답변2");
        service.submitAnswer(id, ans2);

        // 6) 최종 평가
        when(mockChatService.ask(contains("이력서를 참고")))
                .thenReturn("종합 평가 결과");
        ResultResponse result = service.getResult(id);
        assertThat(result.getResultSummary()).isEqualTo("종합 평가 결과");
    }
}