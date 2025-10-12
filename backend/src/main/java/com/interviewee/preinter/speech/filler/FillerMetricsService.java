package com.interviewee.preinter.speech.filler;

import com.interviewee.preinter.dto.WhisperResponse;
import com.interviewee.preinter.speech.filler.dto.FillerMetricsResult;
import com.interviewee.preinter.speech.filler.dto.FillerOccurrence;
import com.interviewee.preinter.speech.filler.dto.FillerTopItem;
import com.interviewee.preinter.speech.score.TranscriptionResult;
import com.interviewee.preinter.speech.score.Word;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FillerMetricsService {

    private final FillerMetricsStoreService store;


    public FillerMetricsResult compute(TranscriptionResult tr) {
        String fullText = tr.transcript() == null ? "" : tr.transcript();
        List<Word> words = tr.words() == null ? List.of() : tr.words();

        // 1) 기본 값 준비
        double speechDurationSec = estimateDurationFromWords(words); // 마지막 end
        int totalWords = words.size() > 0 ? words.size() : roughCount(fullText);

        // 2) 간투어 탐지 (word 타임스탬프 기반)
        List<FillerOccurrence> occ = new ArrayList<>();
        Map<String, Integer> counts = new HashMap<>();
        int maxRun = 0, curRun = 0;

        for (int i = 0; i < words.size(); i++) {
            Word w = words.get(i);
            String token = normalizeKo(w.text()); // 기존 Word가 단어 표면형을 가진다고 가정
            if (isFiller(token)) {
                occ.add(new FillerOccurrence(token, w.startSec(), w.endSec(), i));
                counts.merge(token, 1, Integer::sum);
                curRun++;
                maxRun = Math.max(maxRun, curRun);
            } else {
                curRun = 0;
            }
        }

        int totalFillers = occ.size();

        // 3) 지표 계산
        double minutes = Math.max(speechDurationSec, 1e-6) / 60.0;
        double fillersPerMinute = totalFillers / minutes;
        double fillerRatioPerWord = totalWords > 0 ? (double) totalFillers / totalWords : 0.0;
        double fillerDensityPerSecond = speechDurationSec > 0 ? totalFillers / speechDurationSec : 0.0;

        // 4) TOP N
        List<FillerTopItem> top = counts.entrySet().stream()
                .sorted((a,b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> new FillerTopItem(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new FillerMetricsResult(
                totalFillers,
                round2(fillersPerMinute),
                round4(fillerRatioPerWord),
                round4(fillerDensityPerSecond),
                maxRun,
                top,
                List.copyOf(occ)
        );
    }

    // 컨트롤러 변경 없이 Redis 저장까지 하고 싶으면 이 메서드를 컨트롤러에서 한 줄로 호출
    public void computeAndStore(String sessionId, TranscriptionResult tr) {
        FillerMetricsResult r = compute(tr);
        store.put(sessionId, r, "filler-v1");
    }

    // ===== helpers =====
    private static double estimateDurationFromWords(List<Word> words) {
        if (words == null || words.isEmpty()) return 0.0;
        double maxEnd = 0.0;
        for (Word w : words) {
            maxEnd = Math.max(maxEnd, w.endSec());
        }
        return maxEnd;
    }

    private static boolean isFiller(String token) {
        return FillerLexicon.TOKENS.contains(token);
    }

    private static String normalizeKo(String s) {
        if (s == null) return "";
        // 구두점 제거 + 공백 trim
        return s.replaceAll("[\\p{Punct}…·•'\"“”’‘]", "").trim();
    }

    private static int roughCount(String text) {
        if (text == null || text.isBlank()) return 0;
        return (int) Arrays.stream(text.trim().split("\\s+"))
                .filter(t -> !t.isBlank()).count();
    }

    private static double round2(double v){ return Math.round(v*100.0)/100.0; }
    private static double round4(double v){ return Math.round(v*10000.0)/10000.0; }
}
