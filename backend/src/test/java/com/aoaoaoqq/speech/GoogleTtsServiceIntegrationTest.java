package com.aoaoaoqq.speech;

import com.interviewee.preinter.Application;
import com.interviewee.preinter.speech.GoogleTtsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = Application.class
)
public class GoogleTtsServiceIntegrationTest {

    @Autowired
    private GoogleTtsService ttsService;

    @Test
    void synthesize_realText_returnsMp3Bytes() throws Exception {
        // given
        String text = "안녕하세요, 통합 테스트입니다.";

        // when
        byte[] audioBytes = ttsService.synthesize(text);

        // then
        assertNotNull(audioBytes);
        assertTrue(audioBytes.length > 0);

        // 첫 바이트가 'I'(ID3) 또는 0xFF(MP3 frame sync)여야 한다
        byte first = audioBytes[0];
        assertTrue(
                first == (byte)'I' || (first & 0xFF) == 0xFF,
                String.format("첫 바이트가 'I' 또는 0xFF여야 하는데, 실제 값: 0x%02X", first & 0xFF)
        );
    }
}
