package com.interviewee.preinter.speech.score;


import com.interviewee.preinter.speech.GoogleSttService;
import com.interviewee.preinter.speech.WhisperSttService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpeakingMetricsService {

    private static final String ALGO_VERSION = "speed-v1.1";

//    private final GoogleSttService sttService;
    private final WhisperSttService sttService;
    private final SpeedScoreService speedScoreService;
    private final SpeakingMetricsStoreService store;

    /** 음성 답변 업로드 시 호출 (결과는 Redis 저장, 컨트롤러 응답 포맷은 그대로) */
    public void computeAndStore(String sessionId, TranscriptionResult tr) throws IOException {
        List<Word> words = tr.words();
        var s = speedScoreService.score(words);
        var metrics = new SpeakingMetrics(
                true, s.score(), s.articulationRate(), s.pauseRatio(), s.longPauseCount(), ALGO_VERSION
        );
        store.put(sessionId, metrics);
    }

    /** 텍스트 모드 등 사용불가 표시 */
    public void markUnavailable(String sessionId) {
        store.put(sessionId, SpeakingMetrics.unavailable());
    }

    /** 평가 직전 조회 */
    public SpeakingMetrics getForEvaluation(String sessionId) {
        return store.getOrNull(sessionId);
    }
}
