package com.interviewee.preinter.speech;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import com.interviewee.preinter.SecretFetcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Configuration
public class GoogleCloudConfig {

    private final SecretFetcher secrets;
    private final String secretName;

    public GoogleCloudConfig(
            SecretFetcher secrets,
            @Value("${aws.gcp-secret-name}") String secretName) {
        this.secrets = secrets;
        this.secretName = secretName;
    }

    @Bean
    public CredentialsProvider googleCredentialsProvider() throws IOException {
        // Secrets Manager 에서 GCP_SA_JSON 키를 꺼내 스트림으로 변환
        String saJson = secrets.getSecretValue(secretName, "GOOGLE_APPLICATION_CREDENTIALS");
        GoogleCredentials creds = GoogleCredentials
                .fromStream(new ByteArrayInputStream(saJson.getBytes(StandardCharsets.UTF_8)))
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
        return FixedCredentialsProvider.create(creds);
    }

    @Bean
    public SpeechClient speechClient(CredentialsProvider credentialsProvider) throws IOException {
        SpeechSettings settings = SpeechSettings.newBuilder()
                .setCredentialsProvider(credentialsProvider)
                .build();
        return SpeechClient.create(settings);
    }

    @Bean
    public TextToSpeechClient textToSpeechClient(CredentialsProvider credentialsProvider) throws IOException {
        TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                .setCredentialsProvider(credentialsProvider)
                .build();
        return TextToSpeechClient.create(settings);
    }
}
