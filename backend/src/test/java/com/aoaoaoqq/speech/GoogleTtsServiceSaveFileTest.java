package com.aoaoaoqq.speech;

import com.interviewee.preinter.Application;
import com.interviewee.preinter.speech.GoogleTtsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = Application.class
)
public class GoogleTtsServiceSaveFileTest {

    @Autowired
    private GoogleTtsService ttsService;

    @Test
    void synthesizeAndSaveMp3() throws Exception {
        // given
        String text = "안녕하세요, 직접 들어보는 테스트입니다.";

        // when
        byte[] audioBytes = ttsService.synthesize(text);

        // then
        assertNotNull(audioBytes);
        assertTrue(audioBytes.length > 0);

        // 파일로 저장
        Path output = Path.of("build", "tts-output.mp3");
        Files.createDirectories(output.getParent());
        Files.write(output, audioBytes);
        System.out.println("MP3 파일 저장 완료: " + output.toAbsolutePath());

        // (IDE 에서 Tests → “Run synthesizeAndSaveMp3” 실행 후
        //  프로젝트 폴더의 build/tts-output.mp3 를 더블클릭 or 재생기로 열어 보세요.)
    }
}
