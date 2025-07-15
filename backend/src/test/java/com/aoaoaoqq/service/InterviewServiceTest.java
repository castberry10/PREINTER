package com.aoaoaoqq.service;

import com.interviewee.preinter.Application;
import com.interviewee.preinter.document.DocumentService;
import com.interviewee.preinter.dto.request.*;
import com.interviewee.preinter.dto.response.*;
import com.interviewee.preinter.interview.InterviewService;
import com.interviewee.preinter.openai.ChatService;
import com.interviewee.preinter.repository.InterviewSessionRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = Application.class)
@Transactional
class InterviewServiceTest {
    @Autowired
    private InterviewService service;

    @Autowired
    private InterviewSessionRepository repo;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private ChatService chatService;

    @Test
    void testFullInterviewFlow_andPrintSessionState() throws Exception {
        // 목(mock) 동작 정의
        when(documentService.extractText(any())).thenReturn("이력서 텍스트");
        when(chatService.summarize(any())).thenReturn("이력서 텍스트");
        when(chatService.askWithHistory(any())).thenReturn("첫 질문입니다.");
        when(chatService.askWithHistory(any())).thenReturn("두번째 질문입니다.");
        when(chatService.askEvaluation(any())).thenReturn("최종 요약입니다.");

        // 1) 인터뷰 시작
        MockMultipartFile resume = new MockMultipartFile(
                "resumeFile", "resume.txt", "text/plain", "경력 내용".getBytes()
        );
        StartInterviewResponse startResp = service.startInterview(new StartInterviewRequest(resume));

        // 2) 다음 질문 요청
        GetNextQuestionResponse q1Resp = service.getNextQuestion(
                new GetNextQuestionRequest(startResp.getSessionId())
        );

        // 3) 답변 제출
        SubmitAnswerResponse ans1Resp = service.submitAnswer(
                new SubmitAnswerRequest(startResp.getSessionId(), "제 첫번째 답변입니다.")
        );

        // 4) 다음 질문 요청
        GetNextQuestionResponse q2Resp = service.getNextQuestion(
                new GetNextQuestionRequest(startResp.getSessionId())
        );

        // 5) 답변 제출
        SubmitAnswerResponse ans2Resp = service.submitAnswer(
                new SubmitAnswerRequest(startResp.getSessionId(), "제 두번째 답변입니다.")
        );

        // 6) 결과 요청
        GetResultResponse resultResp = service.getResult(
                new GetResultRequest(startResp.getSessionId())
        );
        System.out.println("[session Id] " + repo.findById(startResp.getSessionId()).orElse(null).getId());
        System.out.println("[session Resume] " + repo.findById(startResp.getSessionId()).orElse(null).getResumeText());
        System.out.println("[session History] " + repo.findById(startResp.getSessionId()).orElse(null).getHistory());
    }
}