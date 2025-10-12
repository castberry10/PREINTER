package com.interviewee.preinter.speech.filler.dto;

public record FillerOccurrence(
        String token,
        double startSec,
        double endSec,
        int segmentId
) {
}
