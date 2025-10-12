package com.aoaoaoqq.speech;


import com.google.cloud.speech.v2.*;
import com.interviewee.preinter.Application;
import com.interviewee.preinter.speech.GoogleSttService;
import com.interviewee.preinter.speech.score.TranscriptionResult;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;
import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(
        classes = Application.class
)
@ActiveProfiles("test")
public class SttTest {

    @Autowired
    private GoogleSttService sttService;
    @Autowired
    private SpeechClient speechClient;


    @Test
    @DisplayName("GCP v2 - 타임스탬프 포함 전사 (mp3)")
    @Timeout(60)
    void transcribe_with_timestamps_e2e() throws Exception {
        // src/test/resources/sample_ko_short.mp3
        ClassPathResource resource = new ClassPathResource("scoreTest.mp3");
        try (InputStream is = resource.getInputStream()) {
            MockMultipartFile mf = new MockMultipartFile(
                    "audioFile",
                    "scoreTest.mp3",
                    "audio/mpeg",
                    is
            );

            TranscriptionResult result = sttService.transcribeWithTimestamps(mf);

            System.out.println("==== TRANSCRIPT ====\n" + result.transcript());
            System.out.println("==== WORDS ====");
            result.words().forEach(w ->
                    System.out.printf("%s\t%.3f -> %.3f%n", w.text(), w.startSec(), w.endSec())
            );

            assertThat(result.transcript()).isNotBlank();
            assertThat(result.words()).isNotEmpty();
        }
    }
    @Test
    void sanity_check_with_public_gcs_uri() {
        String recognizer = "projects/preinter/locations/us-central1/recognizers/my-recognizer";

        RecognitionConfig config = RecognitionConfig.newBuilder()
                .addLanguageCodes("en-US")
                .setModel("latest_short")
                .setAutoDecodingConfig(AutoDetectDecodingConfig.getDefaultInstance())
                .build();

        RecognizeRequest req = RecognizeRequest.newBuilder()
                .setRecognizer(recognizer)
                .setConfig(config)
                .setUri("gs://cloud-samples-data/speech/brooklyn_bridge.wav") // 공개 샘플
                .build();

        RecognizeResponse res = speechClient.recognize(req);
        System.out.println(res);
    }
}
