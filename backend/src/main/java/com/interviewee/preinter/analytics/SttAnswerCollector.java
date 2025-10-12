package com.interviewee.preinter.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewee.preinter.dto.WhisperResponse;
import com.interviewee.preinter.dto.WhisperResponse.Word;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SttAnswerCollector {

    private final StringRedisTemplate redis;
    private static final ObjectMapper M = new ObjectMapper();

    @Value("${stt.answer.ttl-sec:3600}")
    private long ttlSec;

    /**
     * Whisper 응답(large-v3 등)을 Redis에 저장
     * - Hash key:  stt:ans:{sessionId}:{qNo}
     * - Index key: stt:ans:index:{sessionId}
     */
    public void collect(String sessionId, WhisperResponse wr) {
        int qNo = resolveQuestionNumber(sessionId);

        String hashKey  = "stt:ans:%s:%d".formatted(sessionId, qNo);
        String indexKey = "stt:ans:index:%s".formatted(sessionId);

        String transcript = buildTranscript(wr);
        String language   = wr.info() != null ? n(wr.info().language()) : "";
        String model      = wr.info() != null ? n(wr.info().model())    : "";
        double duration   = estimateDuration(wr);

        List<Word> words = toWords(wr);

        Map<String, String> map = new LinkedHashMap<>();
        map.put("sessionId", sessionId);
        map.put("questionNumber", Integer.toString(qNo));
        map.put("language", language);
        map.put("model", model);
        map.put("durationSec", Double.isFinite(duration) ? Double.toString(duration) : "");
        map.put("transcript", transcript);
        map.put("segmentsJson", toJsonSafe(wr.segments()));
        map.put("wordsJson", toJsonSafe(words));
        map.put("infoJson", toJsonSafe(wr.info()));
        map.put("rawResponseJson", toJsonSafe(wr));
        map.put("createdAt", Long.toString(System.currentTimeMillis()));

        // 저장 및 인덱스 관리
        redis.opsForHash().putAll(hashKey, map);
        redis.expire(hashKey, Duration.ofSeconds(ttlSec));

        redis.opsForZSet().add(indexKey, hashKey, qNo);
        redis.expire(indexKey, Duration.ofSeconds(ttlSec));
    }

    /** 세션의 모든 답변 가져오기 */
    public List<StoredAnswer> listAll(String sessionId) {
        String indexKey = "stt:ans:index:%s".formatted(sessionId);
        var members = redis.opsForZSet().range(indexKey, 0, -1);
        if (members == null || members.isEmpty()) return List.of();

        List<StoredAnswer> out = new ArrayList<>();
        for (String hashKey : members) {
            Map<Object, Object> h = redis.opsForHash().entries(hashKey);
            if (h.isEmpty()) continue;

            int qNo = parseIntSafe((String) h.getOrDefault("questionNumber", "-1"));
            String transcript = (String) h.getOrDefault("transcript", "");
            String wordsJson  = (String) h.getOrDefault("wordsJson", "[]");

            List<Word> words = parseWords(wordsJson);
            out.add(new StoredAnswer(qNo, transcript, words, hashKey));
        }
        out.sort(Comparator.comparingInt(StoredAnswer::questionNumber));
        return out;
    }

    /* ================= 내부 유틸 ================= */

    private int resolveQuestionNumber(String sessionId) {
        List<String> candidateKeys = List.of(
                "session:%s".formatted(sessionId),
                "interview:session:%s".formatted(sessionId),
                "preinter:session:%s".formatted(sessionId)
        );
        for (String key : candidateKeys) {
            Object v = redis.opsForHash().get(key, "questionNumber");
            if (v != null) return parseIntSafe(v.toString());
        }
        String s = redis.opsForValue().get("session:%s:questionNumber".formatted(sessionId));
        if (s != null) return parseIntSafe(s);
        System.err.printf("[SttAnswerCollector] questionNumber not found for session=%s%n", sessionId);
        return -1;
    }

    private static String buildTranscript(WhisperResponse wr) {
        if (wr == null) return "";
        if (wr.text() != null && !wr.text().isBlank()) return wr.text().trim();
        if (wr.segments() != null && !wr.segments().isEmpty()) {
            return wr.segments().stream().map(WhisperResponse.Segment::text)
                    .collect(Collectors.joining(" ")).trim();
        }
        return "";
    }

    private static double estimateDuration(WhisperResponse wr) {
        if (wr == null || wr.segments() == null || wr.segments().isEmpty()) return Double.NaN;
        return wr.segments().stream().map(WhisperResponse.Segment::end)
                .mapToDouble(Double::doubleValue)
                .max().orElse(Double.NaN);
    }

    private static List<Word> toWords(WhisperResponse wr) {
        if (wr == null || wr.segments() == null) return List.of();
        List<Word> out = new ArrayList<>();
        for (var seg : wr.segments()) {
            if (seg.words() == null) continue;
            for (var w : seg.words()) {
                out.add(new Word(n(w.word()), nz(w.start()), nz(w.end())));
            }
        }
        return out;
    }

    private static String toJsonSafe(Object o) {
        try { return M.writeValueAsString(o); } catch (Exception e) { return "null"; }
    }
    private static String n(String s) { return s == null ? "" : s; }
    private static double nz(Double d) { return d == null ? Double.NaN : d; }
    private static int parseIntSafe(String s) { try { return Integer.parseInt(s.trim()); } catch (Exception e) { return -1; } }
    private static List<Word> parseWords(String json) {
        try {
            var type = M.getTypeFactory().constructCollectionType(List.class, Word.class);
            return M.readValue(json, type);
        } catch (Exception e) { return List.of(); }
    }

    /** 조회용 DTO */
    public record StoredAnswer(int questionNumber, String transcript, List<Word> words, String redisKey) {}
}
