package com.interviewee.preinter.analytics;

import com.interviewee.preinter.dto.WhisperResponse;
import com.interviewee.preinter.dto.WhisperResponse.Word;
import com.interviewee.preinter.speech.filler.FillerLexicon;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ThinkingTimeService {
// 질문 후 답변 시간 계산

    private final SttAnswerCollector collector;

    @Value("${analytics.filler.max-prefix-window-sec:6.0}")
    private double maxPrefixWindowSec;

    private static final Pattern TRIM_PUNCT = Pattern.compile("[\\p{Punct}\\p{IsPunctuation}\\s]+");
    private static final DecimalFormat DF2 = new DecimalFormat("#.##");

    public Result compute(String sessionId) {
        var answers = collector.listAll(sessionId);
        if (answers.isEmpty()) {
            return new Result(false, 0, Double.NaN, Double.NaN, Double.NaN, List.of());
        }

        List<PerAnswer> perAnswers = new ArrayList<>();
        for (var a : answers) {
            double thinkingSec = computeThinkingTimeForAnswer(a.words());
            if (!Double.isNaN(thinkingSec)) {
                perAnswers.add(new PerAnswer(a.questionNumber(), thinkingSec));
            }
        }

        if (perAnswers.isEmpty()) {
            return new Result(false, 0, Double.NaN, Double.NaN, Double.NaN, List.of());
        }

        double min = perAnswers.stream().mapToDouble(PerAnswer::thinkingSec).min().orElse(Double.NaN);
        double max = perAnswers.stream().mapToDouble(PerAnswer::thinkingSec).max().orElse(Double.NaN);
        double avg = perAnswers.stream().mapToDouble(PerAnswer::thinkingSec).average().orElse(Double.NaN);

        return new Result(true, perAnswers.size(), r2(min), r2(max), r2(avg), perAnswers);
    }


    /* ---------------- 내부 로직 ---------------- */

    private double computeThinkingTimeForAnswer(List<Word> words) {
        if (words == null || words.isEmpty()) return Double.NaN;

        for (Word w : words) {
            double start = safe(w.start());
            if (start > maxPrefixWindowSec) {
                return maxPrefixWindowSec; // 초반 구간 초과 시 upper-bound
            }

            String token = normalizeToken(w.word());
            boolean isFiller = isFillerWord(token);

            if (!isFiller) {
                return Math.max(0.0, start);
            }
        }
        return maxPrefixWindowSec; // 전부 간투사면 upper-bound 반환
    }

    /** FillerLexicon 사전을 이용한 간투사 판별 */
    private boolean isFillerWord(String token) {
        if (token.isBlank()) return true;
        for (Pattern p : FillerLexicon.PATTERNS) {
            if (p.matcher(token).matches()) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeToken(String s) {
        if (s == null) return "";
        String x = s.replace("▁", "");
        x = TRIM_PUNCT.matcher(x).replaceAll("");
        return x.trim();
    }

    private static double safe(Double d) { return d == null ? Double.NaN : d; }
    private static double r2(double v) {
        if (!Double.isFinite(v)) return v;
        return Double.parseDouble(DF2.format(v));
    }

    /* ---------------- 결과 DTO ---------------- */

    public record Result(
            boolean available,
            int answerCount,
            double minSec,
            double maxSec,
            double avgSec,
            List<PerAnswer> perAnswers
    ) {}

    public record PerAnswer(
            int questionNumber,
            double thinkingSec
    ) {}
}
