package com.interviewee.preinter.interview;

import com.interviewee.preinter.document.DocumentService;
import com.interviewee.preinter.dto.request.GetNextQuestionRequest;
import com.interviewee.preinter.dto.request.GetResultRequest;
import com.interviewee.preinter.dto.request.StartInterviewRequest;
import com.interviewee.preinter.dto.request.SubmitAnswerRequest;
import com.interviewee.preinter.dto.response.GetNextQuestionResponse;
import com.interviewee.preinter.dto.response.GetResultResponse;
import com.interviewee.preinter.dto.response.StartInterviewResponse;
import com.interviewee.preinter.dto.response.SubmitAnswerResponse;
import com.interviewee.preinter.openai.ChatService;
import com.interviewee.preinter.repository.InterviewSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewService {
    private final ChatService chatService;
    private final DocumentService documentService;
    private final InterviewSessionRepository repo;

    public int getQuestionCount(String sessionId) {
        return repo.findById(sessionId).orElse(null).getQuestionCount();
    }

    /** 1) 인터뷰 시작 */
    public StartInterviewResponse startInterview(StartInterviewRequest req) {
        String sessionId = UUID.randomUUID().toString();
        // 문자열 추출
        String rawTextData = documentService.extractText(req.getResumeFile());
        // 문자열 파싱
        InterviewSession session = new InterviewSession(sessionId, chatService.summarize(rawTextData));
        repo.save(session);
        return new StartInterviewResponse(sessionId);
    }

    /** 2) 첫 질문 요청 */
    public GetNextQuestionResponse getFirstQuestion(GetNextQuestionRequest req) {
        InterviewSession session = repo.findById(req.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid session: " + req.getSessionId()));
        if (session.isFinished()) {
            return new GetNextQuestionResponse(null, true);
        }

        // ChatGPT에 질문 생성 요청
        String prompt = session.getResumeText();
        String question = chatService.ask(prompt);

        session.recordQuestion(question);
        repo.save(session);

        return new GetNextQuestionResponse(question, false);
    }

    /** 3) 답변 제출 */
    public SubmitAnswerResponse submitAnswer(SubmitAnswerRequest req) {
        InterviewSession session = repo.findById(req.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid session: " + req.getSessionId()));
        session.recordAnswer(req.getAnswer());
        repo.save(session);
        return new SubmitAnswerResponse("OK");
    }

    /** 4) 다음 질문 요청 */
    public GetNextQuestionResponse getNextQuestion(GetNextQuestionRequest req) {
        InterviewSession session = repo.findById(req.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid session: " + req.getSessionId()));
        if (session.isFinished()) {
            return new GetNextQuestionResponse(null, true);
        }

        // ChatGPT에 질문 생성 요청
        String question = chatService.askWithHistory(session);

        session.recordQuestion(question);
        repo.save(session);

        return new GetNextQuestionResponse(question, false);
    }
    /** 5) 결과(요약) 요청 */
    public GetResultResponse getResult(GetResultRequest req) {
        InterviewSession session = repo.findById(req.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid session: " + req.getSessionId()));
        session.finish();
        // 세션 정보 삭제 (optional)
        // repo.deleteById(req.getSessionId());
//
//        String prompt = session.evalPrompt();
        String summary = chatService.askEvaluation(session);

        return new GetResultResponse(summary, "면접이 종료되었습니다. 수고하셨습니다.");
    }
}

// 지금 안돼있는거
// 1. 파일받아와서 gpt한테 파싱하기 -> 완료

// 2. 질문 동적 생성하기 -> 해야됨
// 3. 결과 요약하기 -> 대충 완료