package com.interviewee.preinter.speech.filler;

import java.util.List;

public final class FillerLexicon {
    private FillerLexicon() {}

    public static final List<String> TOKENS = List.of(
            "어", "음", "아", "그", "그러니까", "그게", "뭐"
    );
    // 정규식 패턴(토큰 경계 고려). 공백/쉼표/마침표/문장경계 포함 매칭 강화.
    public static final List<java.util.regex.Pattern> PATTERNS = TOKENS.stream()
            .map(t -> java.util.regex.Pattern.compile("(?:(?<=^)|(?<=\\s))" + java.util.regex.Pattern.quote(t) + "(?=\\s|$|[,.!？?…])"))
            .toList();
}
