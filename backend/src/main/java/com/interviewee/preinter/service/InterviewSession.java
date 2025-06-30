package com.interviewee.preinter.service;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    public String nextPrompt() {
        // Implement logic to generate the next prompt based on history or resumeText
        return "Next prompt based on resume or history.";
    }

    public String evalPrompt() {
        // Implement logic to generate an evaluation prompt based on history
        return "Evaluation prompt based on interview session.";
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
