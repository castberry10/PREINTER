package com.interviewee.preinter.openai;

import com.interviewee.preinter.interview.InterviewSession;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.*;
import com.openai.models.evals.runs.RunListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final OpenAIClient client;

    // 면접 첫 질문
    public String ask(String prompt) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addSystemMessage("당신은 면접관입니다.")
                .addUserMessage(prompt)
                .build();

        ChatCompletion resp = client
                .chat()
                .completions()
                .create(params);

        return resp
                .choices()
                .get(0)
                .message()
                .content()
                .orElseThrow(() -> new IllegalStateException("GPT 응답이 없습니다."));
    }
    // 다음 질문
    public String askWithHistory(InterviewSession session) {
        ChatCompletionCreateParams.Builder b = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                // 인터뷰어 역할 고정
                .addSystemMessage("당신은 면접관입니다.")
                // (원한다면) 이력서도 한 번 첨부
                .addUserMessage(session.getResumeText());

        // 이전 질문·답변 히스토리 쌓기
        List<String> history = session.getHistory();
        for (int i = 0; i < history.size(); i++) {
            String msg = history.get(i);
            if (i % 2 == 0) {
                // 짝수 인덱스: 과거에 면접관(Assistant)이 던진 질문
                b.addMessage(ChatCompletionAssistantMessageParam.builder().content(msg).build());
            }
            else {
                // 홀수 인덱스: 지원자(User)의 답변
                b.addUserMessage(msg);
            }
        }

        // 마지막으로, 새 질문을 요청하는 프롬프트
        String systemPrompt = "다음 면접 질문을 제시해 주세요.";
        b.addSystemMessage(systemPrompt);

        // 빌드 & 호출
        ChatCompletionCreateParams params = b.build();
        ChatCompletion resp = client
                .chat()
                .completions()
                .create(params);

        return resp
                .choices()
                .get(0)
                .message()
                .content()
                .orElseThrow(() -> new IllegalStateException("GPT 응답이 없습니다."));
    }
    // 면접 총 평가
    public String askEvaluation(InterviewSession session) {
        ChatCompletionCreateParams.Builder b = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addSystemMessage("지원자 총 평가를 수행합니다.")
                // 이력서는 한 번만
                .addUserMessage(session.getResumeText());

        // 과거 질문·답변 히스토리를 한 쌍씩
        List<String> tmpList = session.getHistory();
        for (int i = 0; i < tmpList.size(); i++) {
            if (i % 2 == 0) {
                b.addMessage(ChatCompletionAssistantMessageParam.builder().content(tmpList.get(i)).build());
            } else {
                b.addUserMessage(tmpList.get(i));
            }
        }

        ChatCompletionCreateParams params = b.build();

        ChatCompletion resp = client
                .chat()
                .completions()
                .create(params);

        return resp
                .choices()
                .get(0)
                .message()
                .content()
                .orElseThrow(() -> new IllegalStateException("GPT 응답이 없습니다."));
    }
    // 문서(텍스트) 요약
    public String summarize(String text) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addSystemMessage("당신은 채용 면접관 역할을 맡은 AI 어시스턴트입니다. " +
                        "제가 이력서 원문을 보내면, \\n\\n1. 아래 항목으로 깔끔히 분류·요약해 주세요:" +
                        "\\n   - 이름, 연락처 (Contact)" +
                        "\\n   - 지원 분야 및 목표 (Objective)" +
                        "\\n   - 학력 (Education)" +
                        "\\n   - 경력 (Experience) : 회사명, 직책, 주요 업무 및 성과" +
                        "\\n   - 프로젝트 (Projects) : 기간, 기술 스택, 역할, 주요 기여" +
                        "\\n   - 기술 스택 (Skills) : 프로그래밍 언어, 프레임워크, 도구 등" +
                        "\\n   - 자격증·수상 (Certifications & Awards)" +
                        "\\n   - 언어 능력 (Languages)" +
                        "\\n   - 기타 (기타 강조할 점)")
                .addUserMessage(text)
                .build();

        ChatCompletion resp = client.chat().completions().create(params);
        return resp.choices().get(0).message().content()
                .orElseThrow(() -> new IllegalStateException("요약 결과가 없습니다."));
    }
}
