package com.interviewee.preinter.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewee.preinter.interview.InterviewSession;
import com.interviewee.preinter.speech.score.SpeakingMetricsService;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.*;
import com.openai.models.evals.runs.RunListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final OpenAIClient client;
    private final SpeakingMetricsService speakingMetricsService;

    private static double round1(double v){ return Math.round(v*10.0)/10.0; }
    private static double round2(double v){ return Math.round(v*100.0)/100.0; }
    private static double round3(double v){ return Math.round(v*1000.0)/1000.0; }

    // 질문
    public String askWithHistory(InterviewSession session) throws JsonProcessingException {

        List<String> history = session.getHistory();
        List<Map<String, String>> historyWithRole = new ArrayList<>();
        for (int i = 0; i < history.size(); i++) {
            String msg = history.get(i);
            String role = (i % 2 == 0) ? "assistant" : "user";
            Map<String, String> entry = new HashMap<>();
            entry.put("role", role);
            entry.put("content", msg);
            historyWithRole.add(entry);
        }


        ObjectMapper mapper = new ObjectMapper();
        String historyJson = mapper.writeValueAsString(historyWithRole);


        String prompt = String.format(
                """
                넌 인터뷰를 진행하는 인사담당자야.
        
                [대화기록]
                %s
        
                위 대화기록을 바탕으로 다음 질문 1개만 생성해줘
                질문은 ""로 감싸 문자열 형태로 만들어줘. 파싱해야 하니 형식을 지켜줘.
                예시 : "저희 회사의 지원해서 하고자 하는일은 무엇인가요?"
                """,
                historyJson
        );


        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addSystemMessage("당신은 면접관입니다.")
                .addUserMessage(session.getResumeText())
                .addUserMessage(prompt)
                .build();

        ChatCompletion resp = client.chat().completions().create(params);
        return resp.choices()
                .get(0)
                .message()
                .content()
                .orElseThrow(() -> new IllegalStateException("GPT 응답이 없습니다."));
    }

    // 면접 총 평가
    public String askEvaluation(InterviewSession session) throws JsonProcessingException {

        // (기존) 히스토리 JSON
        List<String> history = session.getHistory();
        List<Map<String, String>> historyWithRole = new ArrayList<>();
        for (int i = 0; i < history.size(); i++) {
            String msg = history.get(i);
            String role = (i % 2 == 0) ? "assistant" : "user";
            Map<String, String> entry = new HashMap<>();
            entry.put("role", role);
            entry.put("content", msg);
            historyWithRole.add(entry);
        }
        ObjectMapper mapper = new ObjectMapper();
        String historyJson = mapper.writeValueAsString(historyWithRole);

        // (신규) Redis에서 스피킹 지표 조회
        var metrics = speakingMetricsService.getForEvaluation(session.getId());
        Map<String, Object> speaking = new LinkedHashMap<>();
        if (metrics == null) {
            speaking.put("available", false);
        } else {
            speaking.put("available", metrics.available());
            if (metrics.available()) {
                speaking.put("score", round1(metrics.score()));
                speaking.put("articulationRate", round2(metrics.articulationRate()));
                speaking.put("pauseRatio", round3(metrics.pauseRatio()));
                speaking.put("longPauseCount", metrics.longPauseCount());
            }
        }
        String speakingJson = mapper.writeValueAsString(speaking);

        String prompt = String.format(
                """
                [이력서]
                %s
    
                [대화기록]
                %s
    
                [발화지표]
                %s
    
                위 대화기록(인터뷰기록)을 바탕으로 이력서를 참고하여 지원자의 평가를 진행해야해. 
                평가는 지금 인터뷰과정에서의 평가이기에 이력서는 참고용도로만 사용하고,
                발화지표(speaking)는 available이 true일 때만 '소통력'과 총점에 보조신호로 사용해.
                
                응답은 아래 형식을 정확히 지켜서 JSON으로만 반환해. 마크다운 금지.
                {
                  "면접결과": "",
                  "면접관의 평가": "",
                  "면접관의 피드백": "",
                  "면접관의 면접 팁": "",
                  "면접관의 점수": 0,
                  "면접관의 상세 점수": {
                    "논리성": 0,
                    "전문성": 0,
                    "소통력": 0,
                    "인성": 0,
                    "창의성": 0
                  }
                }
                각 점수는 0부터 100까지로 매겨.
                """,
                session.getResumeText(),
                historyJson,
                speakingJson
        );

        ChatCompletionCreateParams.Builder b = ChatCompletionCreateParams.builder()
                .model(ChatModel.O4_MINI)
                .addSystemMessage("넌 인터뷰 평가자야. 발화지표가 있으면 소통력/총점 산정에 참고하되, 내용·논리·전문성을 우선 평가해.")
                .addUserMessage(prompt);

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
                .addSystemMessage("당신은 채용 면접관의 서류작업을 도와줄 알바을 맡은 AI 어시스턴트입니다.\n" +
                        "{\n" +
                        "\"이름\" : ,\n" +
                        "\"지원분야\" : \n" +
                        "\"학력\" :\n" +
                        "\"경력\": [\" \" , \" \" , ...]\n" +
                        "\"프로젝트\" : [\n" +
                        "    {\n" +
                        "        \"프로젝트명\" : \"\",\n" +
                        "        \"기간\" : \"\",\n" +
                        "        \"역할\" : \"\",\n" +
                        "        \"설명\" : \"\"\n" +
                        "        ...\n" +
                        "    },\n" +
                        "],\n" +
                        "\"자격증\": [\"\" , \"\" , ...],\n" +
                        "\"수상\": [\"\" , \"\" , ...],\n" +
                        "\"외국어 자격증\": [\"\" , \"\" , ...],\n" +
                        "\"자기소개서\" : [\n" +
                        "    {\n" +
                        "        \"제목\" : \"\",\n" +
                        "        \"내용\" : \"\"\n" +
                        "    },...\n" +
                        "],\n" +
                        "\"기타 특이사항 \" : \"\",\n" +
                        "\n" +
                        "}\n" +
                        "\n" +
                        "위 형식에 따라 이력서를 요약, 정리해줘 \n" +
                        "기타특이사항에는 각 분야마다 이 정해진 형식에 맞춰서 넣을 수 없는 이야기를 작성해줘 \n" +
                        "저 형식 항목중 적을게 없는 내용은 \"\"으로 냅둬줘 \n" +
                        "\n" +
                        "프롬프트를 파싱해야하니 꼭 위 json 형식에 맞춰서 작성해줘")
                .addUserMessage(text)
                .build();

        ChatCompletion resp = client.chat().completions().create(params);
        return resp.choices().get(0).message().content()
                .orElseThrow(() -> new IllegalStateException("요약 결과가 없습니다."));
    }
}
