package com.interviewee.preinter.service;

import com.interviewee.preinter.dto.*;
import com.interviewee.preinter.openai.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewService {
    private final ChatService chatService;
    private final Map<UUID, InterviewSession> sessions = new HashMap<>();


    public InterviewStartResponse startInterview(InterviewStartRequest req) {
        UUID id = UUID.randomUUID();
        sessions.put(id, new InterviewSession(req.getResume()));
        InterviewStartResponse res = new InterviewStartResponse();
        res.setInterviewId(id);
        return res;
    }

    public QuestionResponse getNextQuestion(UUID interviewId) {
        InterviewSession session = sessions.get(interviewId);
        String prompt = session.buildQuestionPrompt();
        String question = chatService.ask(prompt);
        session.recordQuestion(question);
        QuestionResponse resp = new QuestionResponse();
        resp.setQuestionNumber(session.getQuestionCount());
        resp.setQuestion(question);
        return resp;
    }

    public void submitAnswer(UUID interviewId, AnswerRequest answerReq) {
        InterviewSession session = sessions.get(interviewId);
        session.recordAnswer(answerReq.getAnswer());
    }

    public ResultResponse getResult(UUID interviewId) {
        InterviewSession session = sessions.remove(interviewId);
        String evalPrompt = session.buildEvaluationPrompt();
        String summary = chatService.ask(evalPrompt);
        ResultResponse res = new ResultResponse();
        res.setInterviewId(interviewId);
        res.setResultSummary(summary);
        return res;
    }
}
