package com.interviewee.preinter.analytics;

import com.interviewee.preinter.dto.WhisperResponse;
import com.interviewee.preinter.dto.WhisperResponse.Word;
import com.interviewee.preinter.speech.filler.FillerLexicon;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FillerFrequencyService {

    private final SttAnswerCollector collector;

    @Value("${analytics.filler.top-n:10}")
    private int topN;

    public Result compute(String sessionId) {
        var answers = collector.listAll(sessionId);
        if (answers == null || answers.isEmpty()) {
            return new Result(0, List.of(), Map.of());
        }

        // 전체 transcript 합치기(백업용 텍스트 스캔), 그리고 word 타임스탬프 기반 1차 카운트
        Map<String, Integer> counter = new HashMap<>();
        int total = 0;

        for (var a : answers) {
            // 1) word 기반(정밀)
            if (a.words() != null) {
                for (Word w : a.words()) {
                    String norm = normalize(w.word());
                    if (isFiller(norm)) {
                        counter.merge(norm, 1, Integer::sum);
                        total++;
                    }
                }
            }

            // 2) 세이프티: transcript 정규식 스캔(토큰화가 빈약하게 올 경우 보완)
            if (a.transcript() != null && !a.transcript().isBlank()) {
                // 문장 내 토큰 경계 고려된 패턴 사용
                for (Pattern p : FillerLexicon.PATTERNS) {
                    var m = p.matcher(a.transcript());
                    while (m.find()) {
                        String hit = normalize(m.group());
                        if (isFiller(hit)) {
                            counter.merge(hit, 1, Integer::sum);
                            total++;
                        }
                    }
                }
            }
        }

        // TOP N 추출
        List<Item> top = counter.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(topN)
                .map(e -> new Item(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // 비율(%) 맵도 추가로 제공
        int finalTotal = total;
        Map<String, Double> ratios = counter.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> finalTotal == 0 ? 0d : (e.getValue() * 100.0 / finalTotal)
                ));

        return new Result(finalTotal, top, ratios);
    }

    private static boolean isFiller(String token) {
        // 사전(TOKENS)은 원형 기준, normalize 로 쉼표/마침표 제거 후 비교
        return FillerLexicon.TOKENS.contains(token);
    }

    private static String normalize(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        // 전각/특수문자 제거(쉼표, 마침표 등), 공백 정리
        s = s.replaceAll("[\\p{Punct}…·ㆍ,，。！？!？]", "");
        // 한국어 단일 음절(예: “음”, “어”, “아”)은 그대로, 복합어도 그대로
        return s;
    }

    // === 결과 DTO ===
    public record Result(
            int totalCount,
            List<Item> topFillers,
            Map<String, Double> ratios // token -> usage percent
    ) {}

    public record Item(String token, int count) {}
}
