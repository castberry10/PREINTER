package com.interviewee.preinter.speech.score;

public record SpeakingMetrics(
        boolean available,
        double score,
        double articulationRate,
        double pauseRatio,
        int    longPauseCount,
        String algoVersion
) {
    public static SpeakingMetrics unavailable() {
        return new SpeakingMetrics(false, 0, 0, 0, 0, "speed-v1.0");
    }
}
