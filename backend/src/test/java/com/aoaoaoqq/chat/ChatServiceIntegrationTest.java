package com.aoaoaoqq.chat;

import com.interviewee.preinter.Application;
import com.interviewee.preinter.openai.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = Application.class,
        properties = {
                // 세션 JDBC 자동 config 무시
                "spring.session.store-type=none"
        }
)
public class ChatServiceIntegrationTest {
    @Autowired
    private ChatService chatService;

    @Test
    void 실제GPT엔진에_요청을_보내보고_응답이_비어있지_않은지_확인() {
        String prompt = "Say hello in Korean.";
        String resp = chatService.ask(prompt);
        assertThat(resp)
                .isNotBlank()
                .contains("안녕하세요");
    }
}
