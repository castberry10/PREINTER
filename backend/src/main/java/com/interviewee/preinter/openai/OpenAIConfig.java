package com.interviewee.preinter.openai;

import com.interviewee.preinter.SecretFetcher;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class OpenAIConfig {
//    @Value("${OPENAI_API_KEY}")
//    private String apiKey;

    private final SecretFetcher secrets;
    private final String secretName;

    public OpenAIConfig(
            SecretFetcher secrets,
            @Value("${aws.openai-secret-name}") String secretName) {
        this.secrets = secrets;
        this.secretName = secretName;
    }

    @Bean
    public OpenAIClient openAIClient() throws IOException {
        // Secrets Manager 에서 OPENAI_API_KEY 키를 꺼내 사용
        String apiKey = secrets.getSecretValue(secretName, "OPENAI_API_KEY");
        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }
}
