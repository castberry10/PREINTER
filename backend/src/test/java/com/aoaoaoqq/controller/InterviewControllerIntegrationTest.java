package com.aoaoaoqq.controller;

import com.interviewee.preinter.Application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.session.store-type=none"
        }
)
public class InterviewControllerIntegrationTest {
//    @LocalServerPort
//    int port;
//
//    private final RestTemplate rest = new RestTemplate();
//
//    private String url(String path) {
//        return "http://localhost:" + port + "/api/interview" + path;
//    }
//
//    @Test
//    void 전체_API플로우_테스트() {
//        // 1) start
//        InterviewStartRequest startReq = new InterviewStartRequest();
//        startReq.setResume("홍길동은 백앤드개발자가 되고싶다.");
//        ResponseEntity<InterviewStartResponse> startRes = rest.exchange(
//                url("/start"),
//                HttpMethod.POST,
//                new HttpEntity<>(startReq),
//                InterviewStartResponse.class
//        );
//        assertThat(startRes.getStatusCode()).isEqualTo(HttpStatus.OK);
//        UUID id = startRes.getBody().getInterviewId();
//
//        // 2) 질문/답변
//        for (int i = 0; i < 2; i++) {
//            ResponseEntity<QuestionResponse> qRes = rest.getForEntity(
//                    url("/" + id + "/question"),
//                    QuestionResponse.class
//            );
//            assertThat(qRes.getStatusCode()).isEqualTo(HttpStatus.OK);
//            assertThat(qRes.getBody().getQuestion()).isNotBlank();
//
//            AnswerRequest aReq = new AnswerRequest();
//            aReq.setAnswer("Controller 테스트 답변");
//            ResponseEntity<Void> aRes = rest.exchange(
//                    url("/" + id + "/answer"),
//                    HttpMethod.POST,
//                    new HttpEntity<>(aReq),
//                    Void.class
//            );
//            assertThat(aRes.getStatusCode()).isEqualTo(HttpStatus.OK);
//        }
//
//        // 3) result
//        ResponseEntity<ResultResponse> rRes = rest.getForEntity(
//                url("/" + id + "/result"),
//                ResultResponse.class
//        );
//        assertThat(rRes.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(rRes.getBody().getResultSummary()).isNotEmpty();
//    }
}
