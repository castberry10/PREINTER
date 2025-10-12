package com.interviewee.preinter.speech.filler.dto;

import java.util.List;

public record FillerMetricsResult(
        int totalFillers,               // 총 간투어 개수(occurrence)
        double fillersPerMinute,        // 분당 간투어
        double fillerRatioPerWord,      // (간투어 토큰수 / 전체 단어수)
        double fillerDensityPerSecond,  // (간투어 토큰수 / 유효 발화시간초)
        int maxConsecutiveFillers,      // 최대 연속 간투어 길이z
        List<FillerTopItem> topFillers, // 많이 쓰는 간투어 TOP N
        List<FillerOccurrence> occurrences // 원본 구간별 발생 목록
) {
}
