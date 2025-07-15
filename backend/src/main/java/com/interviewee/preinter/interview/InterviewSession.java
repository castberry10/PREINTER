package com.interviewee.preinter.interview;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.*;

@Getter
@RedisHash(value = "interviewSession", timeToLive = 300)  // TTL: 5분 (300초)
@NoArgsConstructor
public class InterviewSession implements Serializable {
    @Getter
    @Id
    private String id;
    private String resumeText;
    private List<String> history = new ArrayList<>();
    private int questionCount = 0;
    @Getter
    private boolean finished = false;

    public InterviewSession(String id, String resumeText) {
        this.id = id;
        this.resumeText = resumeText;
    }

    public void recordQuestion(String question) {
        history.add("Q: " + question);
        questionCount++;
    }

    public void recordAnswer(String answer) {
        history.add("A: " + answer);
    }

    public void finish() {
        this.finished = true;
    }
}
