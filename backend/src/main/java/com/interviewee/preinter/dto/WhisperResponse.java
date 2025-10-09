package com.interviewee.preinter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WhisperResponse(
        String text,
        List<Segment> segments,
        Info info
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Info(
            String task,
            String language,
            String model
            // 필요하면 더 추가 (device, fp16 등)
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Segment(
            int id,
            double start,
            double end,
            String text,
            List<Word> words
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Word(
            String word,
            Double start,
            Double end,
            Double probability
    ) {}
}
