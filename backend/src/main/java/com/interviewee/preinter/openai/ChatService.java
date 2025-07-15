package com.interviewee.preinter.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewee.preinter.interview.InterviewSession;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.*;
import com.openai.models.evals.runs.RunListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final OpenAIClient client;

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

        // 과거 질문·답변 히스토리를 한 쌍씩
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
                
                [이력서]
                %s
                
                [대화기록]
                %s
        
                위 대화기록(인터뷰기록)을 바탕으로 이력서를 참고하여 지원자의 평가를 진행해야해. 
                평가는 지금 인터뷰과정에서의 평가이기에 이력서는 참고용도로만 사용해야하고 면접기록에 대해서 피드백과 종합적인 인터뷰 역량등을 살펴봐야해. 
                
                응답은 아래형식을 따라줘. 
                        {
                                        
                        "면접결과": "",
                        "면접관의 평가": "",
                        "면접관의 피드백":"",
                        "면접관의 면접 팁": "",
                        "면접관의 점수" : 0,
                        "면접관의 상세 점수" : {
                            "논리성": 0,
                            "전문성": 0,
                            "소통력": 0,
                            "인성": 0,
                            "창의성": 0
                         }
                        }
                
                응답을 파싱해야 하니 형식을 지켜줘.
                응답에 마크다운은 사용하지말아줘.
                위 형식에 따라 상세히 평가하고 점수를 매겨줘. 각 점수는 0부터 100까지로 해줘.
                """,
                session.getResumeText()
                ,
                historyJson
        );

        ChatCompletionCreateParams.Builder b = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addSystemMessage("넌 인터뷰를 진행하는 인사담당자야. ")
                // 이력서는 한 번만
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
