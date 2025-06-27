package com.interviewee.preinter.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {
    @Value("${openai.api.key}")
    private String apiKey;

    @Bean
    public OpenAIClient openAIClient() {
        // 환경변수 OPENAI_API_KEY 사용, 또는 .apiKey("키") 직접 삽입
        return OpenAIOkHttpClient.builder()
                .fromEnv()
                .build();
    }
}
