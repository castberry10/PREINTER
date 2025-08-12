package com.interviewee.preinter.speech.score;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SpeedScoreService {
    // ===== 튜닝 파라미터 =====
    private static final double TARGET_AR   = 5.5;   // 목표 articulation rate(음절/초)
    private static final double BAND        = 0.5;   // 허용 대역 ±0.5
    private static final double PAUSE_MIN   = 0.20;  // 200ms 이상만 침묵 집계
    private static final double LONG_PAUSE  = 1.00;  // 1초 이상 긴 침묵
    private static final double GAP_IGNORE  = 0.08;  // 80ms 이하는 정렬오차로 무시
    private static final double BASE_FREE   = 0.15;  // 침묵비율 15% 이하는 감점 없음
    private static final double RATIO_SPAN  = 0.25;  // 그 다음 25% 구간에서 20점 감점
    private static final int    MIN_WORDS   = 2;     // 최소 단어 수
    private static final double MIN_TOTAL_S = 7.0;   // 너무 짧은 답변 방지용 임계 (선택)

    public SpeedScore score(List<Word> words) {
        if (words == null || words.size() < MIN_WORDS) {
            return empty();
        }

        // 시작 시각 기준 정렬
        List<Word> ws = new ArrayList<>(words);
        ws.sort(Comparator.comparingDouble(Word::startSec));

        double sFirst = ws.get(0).startSec();
        double eLast  = ws.get(ws.size() - 1).endSec();
        double total  = Math.max(1e-6, eLast - sFirst);

        // 단어 사이 gap → 침묵 합계/긴 침묵 개수
        double pauseSum = 0.0;
        int longCount = 0;
        for (int i = 0; i < ws.size() - 1; i++) {
            double gap = ws.get(i + 1).startSec() - ws.get(i).endSec();
            if (gap < GAP_IGNORE) gap = 0.0;     // 아주 작은 겹침/오차 무시
            if (gap >= PAUSE_MIN) {
                pauseSum += gap;
                if (gap >= LONG_PAUSE) longCount++;
            }
        }

        double speech = Math.max(1e-6, total - pauseSum);

        // 한글 완성형(가-힣) 문자 수 = 음절 수로 근사
        int syllables = 0;
        for (Word w : ws) {
            String t = w.text() == null ? "" : w.text();
            for (int i = 0; i < t.length(); i++) {
                char c = t.charAt(i);
                if (c >= '가' && c <= '힣') syllables++;
            }
        }
        syllables = Math.max(1, syllables);

        // 속도 지표
        double AR = syllables / speech; // 침묵 제외 속도
        double SR = syllables / total;  // 침묵 포함 속도
        double p  = pauseSum / total;   // 침묵 비율

        // 속도 적합도(0~80): 목표±BAND 내면 80점, 바깥은 선형 감점(1음절/초 당 -20)
        double over = Math.max(0.0, Math.abs(AR - TARGET_AR) - BAND);
        double S_rate = Math.max(0.0, 80.0 - 20.0 * over);

        // 침묵 패널티(최대 20): 비율 + 긴 침묵 개수
        double P_ratio = (p <= BASE_FREE) ? 0.0
                : Math.min(20.0, (p - BASE_FREE) / RATIO_SPAN * 20.0);
        double P_long  = Math.min(10.0, 2.0 * longCount);
        double P_total = Math.min(20.0, P_ratio + P_long);

        double score = clamp(S_rate + (20.0 - P_total), 0.0, 100.0);

        // 너무 짧은 답변이면(예: < 7초) 정책상 가중치 낮추고 싶다면 여기서 조정 가능
        if (total < MIN_TOTAL_S) {
            score = Math.min(score, 80.0);
        }

        return new SpeedScore(AR, SR, p, longCount, total, speech, syllables, score);
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static SpeedScore empty() {
        return new SpeedScore(0, 0, 0, 0, 0, 0, 0, 0);
    }
}
