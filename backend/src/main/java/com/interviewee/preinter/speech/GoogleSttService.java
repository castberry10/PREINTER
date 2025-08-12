package com.interviewee.preinter.speech;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import com.interviewee.preinter.speech.score.TranscriptionResult;
import com.interviewee.preinter.speech.score.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoogleSttService {
    private final SpeechClient speechClient;

    @Autowired
    public GoogleSttService(SpeechClient speechClient) {
        this.speechClient = speechClient;
    }

    /**
     * 음성 파일을 텍스트로 변환합니다.
     *
     * @param audioFile 클라이언트에서 전송한 MultipartFile (WAV, FLAC 등)
     * @return 인식된 한국어 텍스트
     */
    public String transcribe(MultipartFile audioFile) throws IOException {
        ByteString content = ByteString.copyFrom(audioFile.getBytes());
        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.MP3)
                .setSampleRateHertz(44100)
                .setLanguageCode("ko-KR")
                .setEnableWordTimeOffsets(true)
                .build();
        RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(content).build();
        RecognizeResponse response = speechClient.recognize(config, audio);

        return response.getResultsList().stream()
                .map(SpeechRecognitionResult::getAlternativesList)
                .flatMap(alts -> alts.stream().limit(1))
                .map(SpeechRecognitionAlternative::getTranscript)
                .collect(Collectors.joining(" "));
    }

    public TranscriptionResult transcribeWithTimestamps(MultipartFile audioFile) throws IOException {
        ByteString content = ByteString.copyFrom(audioFile.getBytes());
        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.MP3)
                .setSampleRateHertz(44100)
                .setLanguageCode("ko-KR")
                .setEnableWordTimeOffsets(true)
                .build();
        RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(content).build();
        RecognizeResponse response = speechClient.recognize(config, audio);

        String transcript = response.getResultsList().stream()
                .map(SpeechRecognitionResult::getAlternativesList)
                .flatMap(alts -> alts.stream().limit(1))
                .map(SpeechRecognitionAlternative::getTranscript)
                .collect(Collectors.joining(" "));

        List<Word> words = new ArrayList<>();
        for (SpeechRecognitionResult r : response.getResultsList()) {
            if (r.getAlternativesCount() == 0) continue;
            var alt = r.getAlternatives(0);
            for (WordInfo wi : alt.getWordsList()) {
                double start = wi.getStartTime().getSeconds() + wi.getStartTime().getNanos()/1_000_000_000.0;
                double end   = wi.getEndTime().getSeconds()   + wi.getEndTime().getNanos()  /1_000_000_000.0;
                words.add(new Word(wi.getWord(), start, end));
            }
        }
        return new TranscriptionResult(transcript, words);
    }
}
