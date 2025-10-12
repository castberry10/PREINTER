package com.interviewee.preinter.speech.score;

import com.interviewee.preinter.dto.WhisperResponse;

import java.util.List;

public record TranscriptionResult(
        String transcript,
        List<Word> words,
        WhisperResponse wr
) { }
