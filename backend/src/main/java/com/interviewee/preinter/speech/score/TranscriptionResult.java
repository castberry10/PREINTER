package com.interviewee.preinter.speech.score;

import java.util.List;

public record TranscriptionResult(
        String transcript,
        List<Word> words
) { }
