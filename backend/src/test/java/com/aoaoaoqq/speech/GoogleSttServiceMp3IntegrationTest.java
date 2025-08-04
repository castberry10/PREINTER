package com.aoaoaoqq.speech;

import com.interviewee.preinter.Application;
import com.interviewee.preinter.speech.GoogleSttService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = Application.class
)
public class GoogleSttServiceMp3IntegrationTest {

    @Autowired
    private GoogleSttService sttService;

    @Test
    void transcribe_realMp3_returnsExpectedText() throws Exception {
        // 1) 테스트 리소스에서 MP3 파일 로드
        ClassPathResource mp3Resource = new ClassPathResource("sample-ko.mp3");
        byte[] mp3Bytes = Files.readAllBytes(mp3Resource.getFile().toPath());

        // 2) MultipartFile 형태로 래핑
        MultipartFile multipart = new MockMultipartFile(
                "file",
                "sample-ko.mp3",
                "audio/mpeg",
                mp3Bytes
        );

        // 3) 실제 STT 호출
        String transcript = sttService.transcribe(multipart);

        // 4) 인식 결과 검증 ("안녕하세요" 가 포함되어야 함)
        assertThat(transcript)
                .isNotEmpty()
                .containsIgnoringCase("안녕하세요");

        System.out.println("transcript = " + transcript);
    }
}
