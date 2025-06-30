package com.aoaoaoqq.service;

import com.interviewee.preinter.Application;
import com.interviewee.preinter.dto.*;
import com.interviewee.preinter.service.InterviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = Application.class,
        properties = {
                "spring.session.store-type=none"
        }
)
public class InterviewServiceIntegrationTest {
    @Autowired
    private InterviewService interviewService;

    @Test
    void 인터뷰_시작부터_질문_제출_결과조회까지_플로우_확인() {
        // 1) 인터뷰 시작
        InterviewStartRequest startReq = new InterviewStartRequest();
        startReq.setResume("홍길동의 개발자 면접");
        InterviewStartResponse startRes = interviewService.startInterview(startReq);
        UUID sessionId = startRes.getInterviewId();
        assertThat(sessionId).isNotNull();

        // 2) 몇 번 질문-응답 주고받기
        for (int i = 0; i < 3; i++) {
            QuestionResponse q = interviewService.getNextQuestion(sessionId);
            assertThat(q.getQuestion()).isNotBlank();

            AnswerRequest a = new AnswerRequest();
            a.setAnswer("테스트 답변 " + i);
            interviewService.submitAnswer(sessionId, a);
        }

        // 3) 결과 조회
        ResultResponse result = interviewService.getResult(sessionId);
        assertThat(result.getResultSummary()).isNotEmpty();
    }
}
