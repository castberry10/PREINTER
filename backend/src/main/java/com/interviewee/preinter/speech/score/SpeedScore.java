package com.interviewee.preinter.speech.score;

public record SpeedScore(
        double articulationRate, // AR: 침묵 제외 속도(음절/초)
        double speechRate,       // SR: 침묵 포함 속도(음절/초)
        double pauseRatio,       // 총 침묵 / 전체
        int    longPauseCount,   // >= 1.0s 긴 침묵 개수
        double totalSec,         // 전체 발화 길이
        double speechSec,        // 실제 발화 시간(침묵 제외)
        int    syllables,        // 추정 음절 수(한글 완성형 문자 수)
        double score             // 최종 점수(0~100)
) { }