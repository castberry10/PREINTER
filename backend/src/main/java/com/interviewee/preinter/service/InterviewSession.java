package com.interviewee.preinter.service;

import com.interviewee.preinter.dto.QuestionResponse;
import lombok.Getter;

import java.util.*;

public class InterviewSession {
    private final String resume;
    private final List<String> history = new ArrayList<>();
    @Getter
    private int questionCount = 0;

    public InterviewSession(String resume) {
        this.resume = resume;
    }

    public String buildQuestionPrompt() {
        if (history.isEmpty()) {
            return "지원자 이력서:\n" + resume + "\n" +
                    "인사 후 첫 질문을 한국어로 해주세요.";
        }
        return "지원자 이력서:\n" + resume + "\n" +
                "대화 기록:\n" + String.join("\n", history) + "\n" +
                "다음 질문을 한국어로 해주세요.";
    }

    public void recordQuestion(String question) {
        history.add("Q: " + question);
        questionCount++;
    }

    public void recordAnswer(String answer) {
        history.add("A: " + answer);
    }

    public String buildEvaluationPrompt() {
        return "지원자 이력서:\n" + resume + "\n" +
                "대화 기록:\n" + String.join("\n", history) + "\n" +
                "이력서를 참고해 지원자를 평가하고, 인터뷰를 한국어로 마무리해 주세요.";
    }
}
