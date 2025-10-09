package com.aoaoaoqq.speech.score;


import com.interviewee.preinter.Application;
import com.interviewee.preinter.interview.InterviewSession;
import com.interviewee.preinter.openai.ChatService;
import com.interviewee.preinter.speech.WhisperSttService;
import com.interviewee.preinter.speech.score.SpeakingMetrics;
import com.interviewee.preinter.speech.score.SpeakingMetricsService;
import com.interviewee.preinter.speech.score.SpeakingMetricsStoreService;
import com.interviewee.preinter.speech.score.TranscriptionResult;
import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.services.blocking.chat.ChatCompletionService;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.*;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;


@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class SpeakingFlowIntegrationTest {
    private static final String SESSION_ID = "it-session-voice-001";

    @Autowired private SpeakingMetricsService speakingMetricsService;   // 실제 GCP 호출 포함
    @Autowired private SpeakingMetricsStoreService store;               // 실제 Redis
    @Autowired private ChatService chatService;                         //
    @Autowired private WhisperSttService sttService;

    @MockitoBean
    private OpenAIClient openAIClient;                                  // SDK 클라이언트만 모킹


    @BeforeEach
    void stubOpenAiChain() {
        // 1) SDK 체인 단계별 mock (이름 충돌 방지 위해 SDK 타입만 풀패스 사용)
        var sdkChatServiceMock =
                Mockito.mock(com.openai.services.blocking.ChatService.class);
        var sdkCompletionServiceMock =
                Mockito.mock(com.openai.services.blocking.chat.ChatCompletionService.class);

        // 2) 연결: client.chat() -> sdkChatServiceMock
        Mockito.when(openAIClient.chat()).thenReturn(sdkChatServiceMock);
        // 3) 연결: chat.completions() -> sdkCompletionServiceMock
        Mockito.when(sdkChatServiceMock.completions()).thenReturn(sdkCompletionServiceMock);

        // 4) completions.create(params) -> 가짜 응답
        String fakeMsg = """
            {
              "면접결과":"합격 가능성이 높음",
              "면접관의 평가":"전반적으로 명확하고 탄탄함",
              "면접관의 피드백":"속도는 적정, 침묵 관리 양호",
              "면접관의 면접 팁":"키워드를 먼저 말하고 근거를 붙이세요",
              "면접관의 점수": 86,
              "면접관의 상세 점수": {
                "논리성": 88, "전문성": 84, "소통력": 90, "인성": 85, "창의성": 80
              }
            }
            """;

        ChatCompletion mockResp = Mockito.mock(ChatCompletion.class, Mockito.RETURNS_DEEP_STUBS);
        ChatCompletion.Choice mockChoice = Mockito.mock(ChatCompletion.Choice.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(mockResp.choices()).thenReturn(List.of(mockChoice));
        Mockito.when(mockChoice.message().content()).thenReturn(Optional.of(fakeMsg));

        Mockito.when(sdkCompletionServiceMock.create(any(ChatCompletionCreateParams.class)))
                .thenReturn(mockResp);
    }

    @Test
    @DisplayName("실제 GCP STT → SpeedScore 저장(Redis) → askEvaluation(OpenAI 모킹)")
    void fullVoiceFlow_ok() throws Exception {
        // 세션(Q/A 교대로 기록)
        InterviewSession session = new InterviewSession(SESSION_ID, "홍길동 이력서 텍스트 ...");
        session.recordQuestion("안녕하세요, 자기소개 부탁드립니다.");
        session.recordAnswer("안녕하세요. 저는 백엔드 개발자 홍길동입니다. 최근에는 Redis 기반 세션 저장소를...");
        session.recordQuestion("최근 해결한 기술적 문제는 무엇인가요?");
        session.recordAnswer("대규모 트래픽 상황에서 세션 일관성 보장을 위해 ...");

        // 실제 GCP 호출 + SpeedScore 계산 + Redis 저장
        try (InputStream in = new ClassPathResource("scoreTestMike.mp3").getInputStream()) {
            MultipartFile file = new MockMultipartFile("file", "scoreTestMike.mp3", MediaType.APPLICATION_OCTET_STREAM_VALUE, in);
            TranscriptionResult tr = sttService.transcribeWithTimestamps(file);
            speakingMetricsService.computeAndStore(SESSION_ID, tr);
        }

        // Redis 검증(간단)
        SpeakingMetrics m = store.getOrNull(SESSION_ID);
        Assertions.assertNotNull(m);
        Assertions.assertTrue(m.score() >= 0 && m.score() <= 100);
        Assertions.assertTrue(m.articulationRate() > 0);

        // ✅ 네가 만든 ChatService 사용해서 최종 평가 호출
        String evalJson = chatService.askEvaluation(session);

        Assertions.assertNotNull(evalJson);
        Assertions.assertTrue(evalJson.contains("\"면접관의 점수\""));
        Assertions.assertTrue(evalJson.contains("\"소통력\""));

        var tmp = store.getOrNull(SESSION_ID);
        var json = new com.fasterxml.jackson.databind.ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(tmp);
        System.out.println("SpeedScore JSON:\n" + json);
//        System.out.println("score: 총점수");
//        System.out.println("articulationRate: 침묵 제외 속도(음절/초)");
//        System.out.println("pauseRatio: 총 침묵 / 전체");
//        System.out.println("longPauseCount: >= 1.0s 긴 침묵 개수");

    }
}
