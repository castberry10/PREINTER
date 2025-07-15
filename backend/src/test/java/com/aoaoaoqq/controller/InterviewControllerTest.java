package com.aoaoaoqq.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewee.preinter.controller.InterviewController;
import com.interviewee.preinter.dto.request.GetNextQuestionRequest;
import com.interviewee.preinter.dto.request.GetResultRequest;
import com.interviewee.preinter.dto.request.StartInterviewRequest;
import com.interviewee.preinter.dto.request.SubmitAnswerRequest;
import com.interviewee.preinter.dto.response.GetNextQuestionResponse;
import com.interviewee.preinter.dto.response.GetResultResponse;
import com.interviewee.preinter.dto.response.StartInterviewResponse;
import com.interviewee.preinter.dto.response.SubmitAnswerResponse;
import com.interviewee.preinter.interview.InterviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InterviewController.class)
@ContextConfiguration(classes = {InterviewController.class})
@AutoConfigureMockMvc(addFilters = false)
public class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InterviewService interviewService;

    @Test
    @DisplayName("POST /api/interview/start - success")
    void startInterview() throws Exception {
        StartInterviewRequest req = new StartInterviewRequest(null);
        StartInterviewResponse resp = new StartInterviewResponse("sess-123");
        when(interviewService.startInterview(any(StartInterviewRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/interview/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("sess-123"));

        verify(interviewService).startInterview(any(StartInterviewRequest.class));
    }
    // 이거 첫 질문만 테스트 완료로 두번째 질문도 해야함
    @Test
    @DisplayName("POST /api/interview/question - success")
    void getNextQuestion() throws Exception {
        GetNextQuestionRequest req = new GetNextQuestionRequest("sess-123");
        GetNextQuestionResponse resp = new GetNextQuestionResponse("질문입니다", false);
        when(interviewService.getNextQuestion(any(GetNextQuestionRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/interview/question")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("질문입니다"))
                .andExpect(jsonPath("$.interviewEnded").value(false));

        verify(interviewService).getNextQuestion(any(GetNextQuestionRequest.class));
    }

    @Test
    @DisplayName("POST /api/interview/answer - success")
    void submitAnswer() throws Exception {
        SubmitAnswerRequest req = new SubmitAnswerRequest("sess-123", "답변입니다");
        SubmitAnswerResponse resp = new SubmitAnswerResponse("OK");
        when(interviewService.submitAnswer(any(SubmitAnswerRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/interview/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));

        verify(interviewService).submitAnswer(any(SubmitAnswerRequest.class));
    }

    @Test
    @DisplayName("POST /api/interview/result - success")
    void getResult() throws Exception {
        GetResultRequest req = new GetResultRequest("sess-123");
        GetResultResponse resp = new GetResultResponse("최종 요약", "면접이 종료되었습니다. 수고하셨습니다.");
        when(interviewService.getResult(any(GetResultRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/interview/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluationSummary").value("최종 요약"))
                .andExpect(jsonPath("$.closingMessage").value("면접이 종료되었습니다. 수고하셨습니다."));

        verify(interviewService).getResult(any(GetResultRequest.class));
    }

}
