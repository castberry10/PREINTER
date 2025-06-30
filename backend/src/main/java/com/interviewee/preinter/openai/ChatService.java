package com.interviewee.preinter.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final OpenAIClient client;

    public String ask(String prompt) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4_1)
                .addSystemMessage("당신은 면접관입니다.")
                .addUserMessage(prompt)
                .build();

        ChatCompletion resp = client
                .chat()
                .completions()
                .create(params);

        return resp
                .choices()
                .get(0)
                .message()
                .content()
                .orElseThrow(() -> new IllegalStateException("GPT 응답이 없습니다."));
    }

    public String askEvaluation(String prompt) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4_1)
                .addSystemMessage("지원자 총 평가를 수행합니다.")
                .addUserMessage(prompt)
                .build();

        ChatCompletion resp = client
                .chat()
                .completions()
                .create(params);

        return resp
                .choices()
                .get(0)
                .message()
                .content()
                .orElseThrow(() -> new IllegalStateException("GPT 응답이 없습니다."));
    }
}
