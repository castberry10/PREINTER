package com.aoaoaoqq;

import com.interviewee.preinter.Application;
import com.interviewee.preinter.document.DocumentService;
import com.interviewee.preinter.dto.request.GetNextQuestionRequest;
import com.interviewee.preinter.dto.request.GetResultRequest;
import com.interviewee.preinter.dto.request.StartInterviewRequest;
import com.interviewee.preinter.dto.request.SubmitAnswerRequest;
import com.interviewee.preinter.dto.response.GetResultResponse;
import com.interviewee.preinter.dto.response.SubmitAnswerResponse;
import com.interviewee.preinter.interview.InterviewService;
import com.interviewee.preinter.openai.ChatService;
import com.interviewee.preinter.repository.InterviewSessionRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;


@SpringBootTest(classes = Application.class)
@Transactional
public class InterviewE2ETest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private InterviewService service;

    @Autowired
    private InterviewSessionRepository repo;


    @Test
    void fullFlow_withRealRedis() throws Exception {

        // 1) 이력서 파일 읽고
        // src/test/resources/sample.pdf 를 읽어오기
        ClassPathResource pdfResource = new ClassPathResource("Profile.pdf");

        try (InputStream is = pdfResource.getInputStream()) {

            MockMultipartFile resume = new MockMultipartFile(
                    "resumeFile",               // 폼 필드 이름
                    "sample.pdf",               // 원본 파일명
                    "application/pdf",          // MIME 타입
                    is                          // PDF 바이트스트림
            );

            // 2) 문서 추출
            String extracted = documentService.extractText(resume);
            System.out.println("[Extracted] " + extracted);

            // 3) 요약
            String summary = chatService.summarize(extracted);
            System.out.println("[Summary] " + summary);

            // 4) 인터뷰 시작 → Redis 저장
            String sessionId = service
                    .startInterview(new StartInterviewRequest(resume))
                    .getSessionId();
            System.out.println("[SessionId] " + sessionId);

            // 5) 첫 질문
            String q1 = service
                    .getNextQuestion(new GetNextQuestionRequest(sessionId))
                    .getQuestion();
            System.out.println("[Q1] " + q1);

            // 6) 답변
            service.submitAnswer(new SubmitAnswerRequest(sessionId, "저는 풀업이 가장 좋은 운동이라고 생각합니다."));

            // 7) 두 번째 질문
            String q2 = service
                    .getNextQuestion(new GetNextQuestionRequest(sessionId))
                    .getQuestion();
            System.out.println("[Q2] " + q2);

            // 8) 답변
            service.submitAnswer(new SubmitAnswerRequest(sessionId, "저는 회원을 최고로 중요하게 생각하여, 회사에 이바지 하겠습니다."));

            // 9) 최종 평가
            GetResultResponse result = service.getResult(new GetResultRequest(sessionId));
            System.out.println("[Result] " + result.getEvaluationSummary());
        }
    }
}
