package com.aoaoaoqq.service;

import com.interviewee.preinter.Application;
import com.interviewee.preinter.document.DocumentService;
import com.interviewee.preinter.dto.request.GetNextQuestionRequest;
import com.interviewee.preinter.dto.request.GetResultRequest;
import com.interviewee.preinter.dto.request.StartInterviewRequest;
import com.interviewee.preinter.dto.request.SubmitAnswerRequest;
import com.interviewee.preinter.dto.response.GetNextQuestionResponse;
import com.interviewee.preinter.interview.InterviewService;
import com.interviewee.preinter.interview.InterviewSession;
import com.interviewee.preinter.openai.ChatService;
import com.interviewee.preinter.repository.InterviewSessionRepository;
import com.openai.client.OpenAIClient;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class)
@Transactional
public class InterviewServiceIntegrationTest {

        @Autowired
        private InterviewService service;

        @Autowired
        private InterviewSessionRepository repo;

        @TestConfiguration
        static class StubBeans {
                @Bean
                public DocumentService documentService() {
                        return new DocumentService() {
                                @Override
                                public String extractText(MultipartFile resumeFile) {
                                        return "STUBBED_RESUME";
                                }
                        };
                }

                @Bean
                public ChatService chatService(OpenAIClient client) {
                        return new ChatService(client) {
                                @Override
                                public String summarize(String text) {
                                        return "STUB_SUMMARY";
                                }

                                @Override
                                public String ask(String prompt) {
                                        return "Q1";
                                }

                                @Override
                                public String askWithHistory(InterviewSession session) {
                                        return "Q2";
                                }

                                @Override
                                public String askEvaluation(InterviewSession session) {
                                        return "RESULT";
                                }
                        };
                }
        }
        @Test
        void fullFlow_integrated() {
                // 1) 시작
                var start = service.startInterview(new StartInterviewRequest(
                        new org.springframework.mock.web.MockMultipartFile(
                                "resumeFile", "x.txt", "text/plain", "dummy".getBytes()
                        )
                ));
                String id = start.getSessionId();

                // 2) 첫 질문
                GetNextQuestionResponse q1 = service.getFirstQuestion(new GetNextQuestionRequest(id));
                assertThat(q1.getQuestion()).isEqualTo("Q1");
                assertThat(q1.isInterviewEnded()).isFalse();

                // 3) 첫 답변
                service.submitAnswer(new SubmitAnswerRequest(id, "A1"));

                // 4) 두 번째 질문
                GetNextQuestionResponse q2 = service.getNextQuestion(new GetNextQuestionRequest(id));
                assertThat(q2.getQuestion()).isEqualTo("Q2");
                assertThat(q2.isInterviewEnded()).isFalse();

                // 5) 두 번째 답변
                service.submitAnswer(new SubmitAnswerRequest(id, "A2"));

                // 6) 결과
                var result = service.getResult(new GetResultRequest(id));
                assertThat(result.getClosingMessage()).isEqualTo("RESULT");
                assertThat(result.getClosingMessage()).contains("종료");

                // 7) 저장된 세션 검증
                InterviewSession stored = repo.findById(id).orElseThrow();
                List<String> history = stored.getHistory();
                assertThat(history).containsExactly("Q1", "A1", "Q2", "A2");
                assertThat(stored.getResumeText()).isEqualTo("STUBBED_RESUME");
        }
}
