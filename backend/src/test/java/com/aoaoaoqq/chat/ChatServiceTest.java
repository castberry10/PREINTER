package com.aoaoaoqq.chat;

import com.interviewee.preinter.openai.ChatService;
import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatServiceTest {
    private OpenAIClient mockClient;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        // client.chat().completions().create(...) 체인을 한 번에 모킹
        mockClient  = mock(OpenAIClient.class, RETURNS_DEEP_STUBS);
        chatService = new ChatService(mockClient);
    }

    @Test
    void ask_shouldReturnFirstChoiceContent() {
        // 1) 가짜 ChatCompletion
        ChatCompletion mockCompletion = mock(ChatCompletion.class);

        // 2) 가짜 Choice (deep stub) 생성 → message().content() 체인 스텁
        ChatCompletion.Choice mockChoice = mock(ChatCompletion.Choice.class, RETURNS_DEEP_STUBS);
        when(mockChoice.message().content()).thenReturn(Optional.of("응답내용"));

        // 3) mockCompletion.choices() → [mockChoice]
        when(mockCompletion.choices()).thenReturn(List.of(mockChoice));

        // 4) mockClient.chat().completions().create(...) → mockCompletion
        when(mockClient
                .chat()
                .completions()
                .create(any(ChatCompletionCreateParams.class)))
                .thenReturn(mockCompletion);

        // 5) 실제 호출 및 검증
        String result = chatService.ask("테스트 질문");
        assertThat(result).isEqualTo("응답내용");
    }
}