package com.interviewee.preinter.dto;

import com.interviewee.preinter.analytics.FillerFrequencyService;
import com.interviewee.preinter.analytics.FillerPositionService;
import com.interviewee.preinter.analytics.ThinkingTimeService;
import lombok.Builder;

import java.util.List;

@Builder
public record AnalyticsResponse(
        String sessionId,

        // 1) 생각 시작 지연 요약
        ThinkingTimeService.Result thinkingTime,

        // 2) 간투사 위치 분석
        FillerPositionService.Result fillerPositions,

        // 3) 간투사 빈도/상위 토큰
        FillerFrequencyService.Result fillerFrequency,

        // 4) Top 단어 (추가 예정)
        TopWordsResult topWords,

        // 5) 기존 속도/침묵 요약(운영 중인 SpeakingMetrics 그대로 포함)
        // 확인 필요
        double AR
) {
    // 자리만 잡아둔 DTO들 — 서비스 구현 시 실제 필드 확정/교체
//    public record FillerFrequencyResult(
//            int totalCount,
//            List<Item> topFillers // ex) [{"token":"음","count":12}, ...]
//    ) {
//        public record Item(String token, int count) {
//        }
//    }

    public record TopWordsResult(
            List<Item> topWords // ex) [{"word":"프로젝트","count":23}, ...]
    ) {
        public record Item(String word, int count) {
        }
    }

}
