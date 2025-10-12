package com.interviewee.preinter.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.interviewee.preinter.analytics.FillerFrequencyService;
import com.interviewee.preinter.analytics.FillerPositionService;
import com.interviewee.preinter.analytics.SttAnswerCollector;
import com.interviewee.preinter.analytics.ThinkingTimeService;
import com.interviewee.preinter.dto.AnalyticsResponse;
import com.interviewee.preinter.dto.request.*;
import com.interviewee.preinter.dto.response.*;
import com.interviewee.preinter.interview.InterviewService;
import com.interviewee.preinter.speech.GoogleTtsService;
import com.interviewee.preinter.speech.WhisperSttService;
import com.interviewee.preinter.speech.filler.FillerMetricsService;
import com.interviewee.preinter.speech.score.SpeakingMetrics;
import com.interviewee.preinter.speech.score.SpeakingMetricsService;
import com.interviewee.preinter.speech.score.TranscriptionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final GoogleTtsService ttsService;
//    private final GoogleSttService sttService;
    private final WhisperSttService sttService;
    private final SpeakingMetricsService speakingMetricsService;
    private final FillerMetricsService fillerMetricsService;
    private final SttAnswerCollector sttAnswerCollector;
    private final ThinkingTimeService thinkingTimeService;
    private final FillerFrequencyService fillerFrequencyService;
    private final FillerPositionService fillerPositionService;

    /** 1) 인터뷰 시작 */
//    @PostMapping(value = "/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<StartInterviewResponse> startInterview(
//            @RequestPart("resumeFile") MultipartFile resumeFile
//    ) {
//        StartInterviewResponse response = interviewService.startInterview(resumeFile);
//        return ResponseEntity.ok(response);
//    }
    @PostMapping("/start")
    public ResponseEntity<StartInterviewResponse> startInterview(
            @RequestBody StartInterviewRequest request
    ) {
        StartInterviewResponse response = interviewService.startInterview(request);
        return ResponseEntity.ok(response);
    }

    /** 2) 다음 질문 요청 - 텍스트 */
    @PostMapping("/question/text")
    public ResponseEntity<String> getNextQuestionText(
            @RequestBody GetNextQuestionRequest request
    ) throws JsonProcessingException {
        GetNextQuestionResponse response;

        response = interviewService.getNextQuestion(request);

        return ResponseEntity.ok(response.getQuestion());
    }

    /** 2) 다음 질문 요청 - 음성 */
    @PostMapping(value = "/question/audio", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getNextQuestionAudio(
            @RequestBody GetNextQuestionRequest request
    ) throws Exception {
        // 1) 기존 인터뷰 로직 호출
        GetNextQuestionResponse dto = interviewService.getNextQuestion(request);

        // 2) 질문 텍스트를 음성으로 합성
        byte[] audioBytes = ttsService.synthesize(dto.getQuestion());

        // 3) 오디오 바이트 스트림으로 응답
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(audioBytes);
    }

    /** 3) 답변 제출 - text */
    @PostMapping("/answer/text")
    public ResponseEntity<SubmitAnswerResponse> submitAnswerText(
            @RequestBody SubmitAnswerRequest request
    ) {
        SubmitAnswerResponse response = interviewService.submitAnswer(request);
        return ResponseEntity.ok(response);
    }

    /** 3) 답변 제출 - audio */
    @PostMapping(value = "/answer/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubmitAnswerResponse> submitAnswerAudio(
            @RequestParam("sessionId") String sessionId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        // 1) STT: 음성 파일을 텍스트로 변환
        TranscriptionResult tr = sttService.transcribeWithTimestamps(file);
        System.out.println(tr.words());
        System.out.println(tr.transcript());
        // 1-2) 말하기 점수 추가
        speakingMetricsService.computeAndStore(sessionId, tr);
        // 1-3) 간투어
        fillerMetricsService.computeAndStore(sessionId, tr);
        // 2) 인터뷰 서비스에 전달할 DTO 생성
        SubmitAnswerRequest req = new SubmitAnswerRequest(sessionId, tr.transcript());
        // 3) Analytics를 위한 raw 데이터 저장
        sttAnswerCollector.collect(sessionId, tr.wr());
        // 4) Response
        SubmitAnswerResponse resp = interviewService.submitAnswer(req);

        return ResponseEntity.ok(resp);
    }

    /** 4) 결과(요약) 요청 */
    @PostMapping("/result")
    public ResponseEntity<GetResultResponse> getResult(
            @RequestBody GetResultRequest request
    ) throws JsonProcessingException {
        GetResultResponse response = interviewService.getResult(request);
        return ResponseEntity.ok(response);
    }

    /** 4) Analytics 요청 */
    @PostMapping("/analytics")
    public ResponseEntity<AnalyticsResponse> getAnalyze(
            @RequestParam("sessionId") String sessionId
    ) {
        // 1) 생각 시작 지연 요약
        ThinkingTimeService.Result thinking = thinkingTimeService.compute(sessionId);

        // 2) 간투사 위치 분석(문장 초/중/후반 등)
        FillerPositionService.Result fillerPositions = fillerPositionService.compute(sessionId);

        // 3) 간투사 빈도/상위 토큰 — 서비스 준비되면 주입해서 채우기
        FillerFrequencyService.Result fillerFreq = fillerFrequencyService.compute(sessionId);

        // 4) Top 단어 — 서비스 준비되면 주입해서 채우기
        // TopWordsService.Result topWords = topWordsService.compute(sessionId);

        // 5) 전체 발화 속도/침묵 요약 — 기존 SpeakingMetrics 재사용
        // 한번 봐야함
        double ar = speakingMetricsService.getForEvaluation(sessionId).articulationRate();

        AnalyticsResponse resp = AnalyticsResponse.builder()
                .sessionId(sessionId)
                .thinkingTime(thinking)
                .fillerPositions(fillerPositions)
                .fillerFrequency(fillerFreq)
                .topWords(null)        // TODO: 채우기
                .AR(ar)
                .build();
        return ResponseEntity.ok(resp);
    }
}
