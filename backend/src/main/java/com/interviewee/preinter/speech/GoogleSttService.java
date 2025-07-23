package com.interviewee.preinter.speech;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Collectors;

@Service
public class GoogleSttService {
    private final SpeechClient speechClient;

    public GoogleSttService() throws IOException {
        this.speechClient = SpeechClient.create();
    }

    /**
     * 음성 파일을 텍스트로 변환합니다.
     *
     * @param audioFile 클라이언트에서 전송한 MultipartFile (WAV, FLAC 등)
     * @return 인식된 한국어 텍스트
     */
    public String transcribe(MultipartFile audioFile) throws IOException {
        // 파일을 바이트로 읽어들임
        ByteString content = ByteString.copyFrom(audioFile.getBytes());

        // 오디오 구성: 인코딩, 샘플링, 언어
        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setLanguageCode("ko-KR")
                .build();
        RecognitionAudio audio = RecognitionAudio.newBuilder()
                .setContent(content)
                .build();

        // 동기식 인식 호출
        RecognizeResponse response = speechClient.recognize(config, audio);

        // 가장 높은 신뢰도의 대체안들만 이어붙여 반환
        return response.getResultsList().stream()
                .map(SpeechRecognitionResult::getAlternativesList)
                .flatMap(alts -> alts.stream().limit(1))
                .map(a -> a.getTranscript())
                .collect(Collectors.joining(" "));
    }
}
