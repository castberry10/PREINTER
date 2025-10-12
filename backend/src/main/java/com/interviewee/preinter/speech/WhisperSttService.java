package com.interviewee.preinter.speech;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewee.preinter.dto.WhisperResponse;
import com.interviewee.preinter.speech.score.TranscriptionResult;
import com.interviewee.preinter.speech.score.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class WhisperSttService {

    private final WebClient webClient;
    private final ObjectMapper om = new ObjectMapper();


    @Autowired
    public WhisperSttService(WebClient.Builder builder,
                             @Value("${whisper.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public String transcribe(WhisperResponse wr) throws IOException {
        if (wr == null) return "";

        if (wr.text() != null && !wr.text().isBlank()) {
            return wr.text().trim();
        }

        if (wr.segments() != null && !wr.segments().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (var s : wr.segments()) {
                if (s.text() != null && !s.text().isBlank()) {
                    if (!sb.isEmpty()) sb.append(' ');
                    sb.append(s.text().trim());
                }
            }
            return sb.toString().trim();
        }

        return "";
    }

    public TranscriptionResult transcribeWithTimestamps(MultipartFile audioFile) throws IOException {
        WhisperResponse wr = callWhisper(audioFile);
        if (wr == null) return new TranscriptionResult("", List.of(), null);

        String transcript = transcribe(wr); // 위 로직 재사용

        // words 생성 (간투어/추임새 그대로 보존)
        List<Word> words = new ArrayList<>();
        if (wr.segments() != null) {
            for (var seg : wr.segments()) {
                if (seg.words() == null) continue;
                for (var w : seg.words()) {
                    // 시작/끝 타임스탬프가 없는 토큰은 스킵
                    if (w.start() == null || w.end() == null) continue;
                    String token = w.word() == null ? "" : w.word();
                    words.add(new Word(token, w.start(), w.end()));
                }
            }
        }

        return new TranscriptionResult(transcript, words, wr);
    }

    private WhisperResponse callWhisper(MultipartFile audioFile) throws IOException {
        try {
            MultipartBodyBuilder mb = new MultipartBodyBuilder();
            mb.part("file", audioFile.getResource())
                    .filename(audioFile.getOriginalFilename())
                    .contentType(MediaType.MULTIPART_FORM_DATA);

            // 필요 옵션 추가하고 싶으면:
            // mb.part("language", "ko");
            // mb.part("initial_prompt", "면접 자기소개 관련 발화입니다.");

            String raw = webClient.post()
                    .uri("/transcribe")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(mb.build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> {
                        e.printStackTrace();
                        return Mono.just("");
                    })
                    .block();

            if (raw == null || raw.isBlank()) return null;
            return om.readValue(raw, WhisperResponse.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
