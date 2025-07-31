package com.interviewee.preinter.speech;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

@Configuration
public class GoogleCloudConfig {

    /** application.yml 또는 환경변수에서 읽기 */
    @Value("${GOOGLE_APPLICATION_CREDENTIALS}")
    private String credentialsPath;

    @Bean
    public CredentialsProvider googleCredentialsProvider() throws IOException {
        GoogleCredentials creds = GoogleCredentials
                .fromStream(new FileInputStream(credentialsPath))
                // 모든 GCP API 접근 권한
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
