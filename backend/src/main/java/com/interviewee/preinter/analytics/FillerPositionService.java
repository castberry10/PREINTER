package com.interviewee.preinter.analytics;

import com.interviewee.preinter.dto.WhisperResponse;
import com.interviewee.preinter.speech.filler.FillerLexicon;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FillerPositionService {

    private final SttAnswerCollector collector;

    @Value("${analytics.filler.begin_threshold:0.2}")
    private double beginTh;

    @Value("${analytics.filler.end_threshold:0.8}")
    private double endTh;

    public Result compute(String sessionId) {
        var answers = collector.listAll(sessionId);
        if (answers == null || answers.isEmpty()) return Result.empty();

        int begin = 0, middle = 0, end = 0, total = 0;

        for (var a : answers) {
            List<WhisperResponse.Word> words = a.words();
            if (words == null || words.isEmpty()) continue;

            double segStart = words.stream()
                    .map(WhisperResponse.Word::start)
                    .filter(Objects::nonNull)
                    .min(Double::compareTo)
                    .orElse(0.0);
            double segEnd = words.stream()
                    .map(WhisperResponse.Word::end)
                    .filter(Objects::nonNull)
                    .max(Double::compareTo)
                    .orElse(segStart);

            double dur = Math.max(0.001, segEnd - segStart);

            for (var w : words) {
                String norm = normalize(w.word());
                if (!isFiller(norm)) continue;

                double ws = w.start() != null ? w.start() : segStart;
                double we = w.end() != null ? w.end() : segStart;
                double mid = (ws + we) / 2.0;
                double r = (mid - segStart) / dur;

                if (r < beginTh) begin++;
                else if (r > endTh) end++;
                else middle++;
                total++;
            }
        }

        return new Result(total, begin, middle, end);
    }

    private boolean isFiller(String token) {
        if (token == null) return false;
        String t = token.trim().replaceAll("[,\\.?!…]", "");
        return FillerLexicon.TOKENS.contains(t);
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("[,\\.?!…]", "");
    }

    public record Result(int total, int beginCount, int middleCount, int endCount) {
        public static Result empty() { return new Result(0,0,0,0); }
    }
}
