package com.aoaoaoqq;

import com.interviewee.preinter.Application;
import com.interviewee.preinter.document.DocumentService;
import com.interviewee.preinter.dto.request.GetNextQuestionRequest;
import com.interviewee.preinter.dto.request.GetResultRequest;
import com.interviewee.preinter.dto.request.StartInterviewRequest;
import com.interviewee.preinter.dto.request.SubmitAnswerRequest;
import com.interviewee.preinter.dto.response.GetResultResponse;
import com.interviewee.preinter.interview.InterviewService;
import com.interviewee.preinter.openai.ChatService;
import com.interviewee.preinter.repository.InterviewSessionRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockMultipartFile;



@SpringBootTest(classes = Application.class)
@Transactional
public class InterviewE2ETest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private InterviewService service;

    @Autowired
    private InterviewSessionRepository repo;


    @Test
    void fullFlow_withRealRedis() throws Exception {

        String test = "1. 인적사항\n" +
                "이름: 윤충호 (Yoon, Choong-Ho)\n" +
                "\n" +
                "생년월일: 1994.03.17\n" +
                "\n" +
                "연락처: 010-1234-5678\n" +
                "\n" +
                "이메일: ychfitpower@naver.com\n" +
                "\n" +
                "주소: 대전광역시 유성구\n" +
                "2. 지원 분야\n" +
                "지원 부서: 국방과학연구소 체력관리실\n" +
                "\n" +
                "지원 직무: 피트니스 트레이너 / 체력 코치\n" +
                "\n" +
                "3. 자기소개\n" +
                "국가 안보는 근육에서 시작된다고 믿는 윤충호입니다.\n" +
                "\n" +
                "10년 간의 트레이너 경력과 병영체험 강사 경험을 바탕으로, 군무원과 연구원분들의 체력 증진, 스트레스 완화, 생활 습관 개선을 도울 수 있다고 자부합니다. 헬스장은 과학보다 진실합니다. 반복, 기록, 그리고 근성으로 얻는 결과. 저는 국방과학연구소의 구성원들이 그 진실을 몸소 느끼게 하고 싶습니다.\n" +
                "\n" +
                "4. 학력\n" +
                "2012.03 ~ 2016.02\n" +
                "대전보건대학교 운동처방재활학과 졸업 (전문학사)\n" +
                "\n" +
                "5. 경력\n" +
                "2016.03 ~ 2021.12\n" +
                "바디앤소울 휘트니스 트레이너 / 팀장\n" +
                "\n" +
                "개인 PT 월 평균 150세션 진행\n" +
                "\n" +
                "공무원 체력시험 준비반 운영 (합격률 89%)\n" +
                "\n" +
                "국군체육부대 협력 프로그램 운영 경험\n" +
                "\n" +
                "2022.01 ~ 현재\n" +
                "체력강화 전문 프리랜서 트레이너\n" +
                "\n" +
                "군 간부 대상 체력 컨설팅 (예하 부대 협약)\n" +
                "\n" +
                "스트레스 완화 스트레칭/요가 그룹 클래스 운영\n" +
                "\n" +
                "국방 관련 민간연구기관 운동 프로그램 납품\n" +
                "\n" +
                "6. 자격증\n" +
                "생활스포츠지도사 2급 (보디빌딩)\n" +
                "\n" +
                "NASM CPT (미국 퍼스널트레이너 자격)\n" +
                "\n" +
                "CPR & AED 응급처치 자격\n" +
                "\n" +
                "스트레스완화명상지도사 2급\n" +
                "\n" +
                "7. 병역\n" +
                "육군 병장 만기 전역 (2014.12 제11기계화보병사단)\n" +
                "\n" +
                "GOP 복무 중 부대 체력관리 담당 경험 있음\n" +
                "\n" +
                "8. 기타사항\n" +
                "Bench Press 1RM: 160kg\n" +
                "\n" +
                "Pull-up: 27회 (정자세 기준)\n" +
                "\n" +
                "특이사항: 과학자 대상 \"헬스는 데이터다\" 강연 진행 경험 있음\n" +
                "\n" +
                "취미: 실험실 기구로 데드리프트 영상 찍기\n";

        // 1) 이력서 파일 읽고
        MockMultipartFile resume = new MockMultipartFile(
                "resumeFile","sample.txt","text/plain", test.getBytes()
        );

        // 2) 문서 추출
        String extracted = documentService.extractText(resume);
        System.out.println("[Extracted] " + extracted);

        // 3) 요약
        String summary = chatService.summarize(extracted);
        System.out.println("[Summary] " + summary);

        // 4) 인터뷰 시작 → Redis 저장
        String sessionId = service
                .startInterview(new StartInterviewRequest(resume))
                .getSessionId();
        System.out.println("[SessionId] " + sessionId);

        // 5) 첫 질문
        String q1 = service
                .getNextQuestion(new GetNextQuestionRequest(sessionId))
                .getQuestion();
        System.out.println("[Q1] " + q1);

        // 6) 답변
        service.submitAnswer(new SubmitAnswerRequest(sessionId,"답변1"));

        // 7) 두 번째 질문
        String q2 = service
                .getNextQuestion(new GetNextQuestionRequest(sessionId))
                .getQuestion();
        System.out.println("[Q2] " + q2);

        // 8) 답변
        service.submitAnswer(new SubmitAnswerRequest(sessionId,"답변2"));

        // 9) 최종 평가
        GetResultResponse result = service.getResult(new GetResultRequest(sessionId));
        System.out.println("[Result] " + result.getEvaluationSummary());
    }
}
