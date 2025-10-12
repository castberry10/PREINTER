package com.interviewee.preinter.speech;

import com.google.cloud.speech.v2.*;
import com.google.protobuf.ByteString;
import com.interviewee.preinter.speech.score.TranscriptionResult;
import com.interviewee.preinter.speech.score.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class GoogleSttService {
    private final SpeechClient speechClient;

    private static final String STT_MODEL = "latest_long";

    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcp.speech.region:us-central1}")
    private String region;

    @Value("${gcp.speech.recognizer-id}") private String recognizerId;
    @Value("${gcp.speech.endpoint}") private String endpoint;

    private static final int SEG_SECONDS = 55;   // 60s 제한 대비 여유
    private static final double THRESHOLD_SECONDS = 58.0; // 이하면 단일 호출

    @Autowired
    public GoogleSttService(SpeechClient speechClient) {
        this.speechClient = speechClient;
    }

    @jakarta.annotation.PostConstruct
    public void logConfig() {
        System.out.println("====[Google STT Config]====");
        System.out.println("Project ID   : " + projectId);
        System.out.println("Region       : " + region);
        System.out.println("RecognizerID : " + recognizerId);
        System.out.println("Recognizer   : " + recognizerName());
        System.out.println("Endpoint     : " + endpoint);
        System.out.println("===========================");
    }

    private String recognizerName() {
        return "projects/%s/locations/%s/recognizers/%s"
                .formatted(projectId, region, recognizerId);
    }

    /**
     * 음성 파일을 텍스트로 변환합니다. (v2)
     *
     * @param audioFile 클라이언트에서 전송한 MultipartFile (WAV, FLAC 등)
     * @return 인식된 한국어 텍스트
     */
    public String transcribe(MultipartFile audioFile) throws IOException {
        TranscriptionResult tr = transcribeWithTimestamps(audioFile);
        return tr.transcript();
    }

    public TranscriptionResult transcribeWithTimestamps(MultipartFile audioFile) throws IOException {
        final double MIN_DURATION_SECONDS = 0.5;

        Path tmpDir = Files.createTempDirectory("stt_in_");
        Path src = tmpDir.resolve(safeName(audioFile.getOriginalFilename()));
        Files.write(src, audioFile.getBytes());

        try {
            double dur = probeDurationSeconds(src);
            if (dur < MIN_DURATION_SECONDS) {
                System.err.printf("[STT] Skip entire file: too short (%.2fs)%n", dur);
                return new TranscriptionResult("", List.of());
            }

            // ② 58초 이하이면 단일 호출
            if (dur <= THRESHOLD_SECONDS) {
                return recognizePath(src, 0.0);
            }

            // 분할 → 병렬 인식 → 스티칭
            Path chunksDir = Files.createTempDirectory("stt_chunks_");
            List<Path> chunks = segmentToWavChunks(src, chunksDir, SEG_SECONDS);

            // 병렬 처리 풀 (최대 4개 정도 권장)
            ExecutorService es = Executors.newFixedThreadPool(Math.min(chunks.size(), 1));
            List<Future<TranscriptionResult>> futures = new ArrayList<>();

            for (int i = 0; i < chunks.size(); i++) {
                final int idx = i;
                final Path chunkPath = chunks.get(i);
                final double chunkStartSec = idx * SEG_SECONDS; // 무오버랩 가정

                long size = Files.size(chunkPath);
                double cDur = probeDurationSeconds(chunkPath);
                System.err.printf("[STT] Try chunk #%d file=%s size=%dB len=%.2fs%n",
                        idx, chunkPath.getFileName(), size, cDur);

                // 너무 짧거나 비정상적으로 작은 청크는 스킵
                if (size <= 1024 || cDur < MIN_DURATION_SECONDS) {
                    System.err.printf("[STT] Skip chunk #%d (too small/short)%n", idx);
                    continue;
                }


                futures.add(es.submit(() -> recognizePath(chunkPath, chunkStartSec)));
            }
            es.shutdown();

            // 스티칭
            StringBuilder fullText = new StringBuilder();
            List<Word> fullWords = new ArrayList<>();

            // 만약 전부 스킵되었다면 빈 결과 반환
            if (futures.isEmpty()) {
                System.err.println("[STT] All chunks skipped (too short/small).");
                return new TranscriptionResult("", List.of());
            }

            for (int i = 0; i < futures.size(); i++) {
                try {
                    TranscriptionResult part = futures.get(i).get();
                    if (i > 0) fullText.append(' ');
                    fullText.append(part.transcript());
                    fullWords.addAll(part.words());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();   // ← 인터럽트는 이 경우에만
                    throw new IOException("Chunk recognize interrupted", ie);
                } catch (ExecutionException ee) {
                    throw new IOException("Chunk recognize failed", ee.getCause());
                }
            }

            return new TranscriptionResult(fullText.toString(), fullWords);

        } finally {
            // 임시파일 정리 (삭제 실패는 무시)
            safeDeleteRecursive(tmpDir);
        }
    }

    // ---------- 내부 헬퍼 ----------

    /** 한 파일(또는 청크)을 동기 v2 Recognize로 처리, words는 startOffset에 baseOffsetSec 가산 */
    private TranscriptionResult recognizePath(Path audioPath, double baseOffsetSec) throws IOException {
        byte[] bytes = Files.readAllBytes(audioPath);


        // 확장자 보고 디코딩 고정
        String name = audioPath.getFileName().toString().toLowerCase(Locale.ROOT);
        ExplicitDecodingConfig.Builder dec = ExplicitDecodingConfig.newBuilder()
                .setSampleRateHertz(16000)
                .setAudioChannelCount(1);

        if (name.endsWith(".wav")) {
            dec.setEncoding(ExplicitDecodingConfig.AudioEncoding.LINEAR16);
        } else if (name.endsWith(".flac")) {
            dec.setEncoding(ExplicitDecodingConfig.AudioEncoding.FLAC);
        } else {
            // 안전빵: WAV로 쪼개고 있으니 기본 LINEAR16
            dec.setEncoding(ExplicitDecodingConfig.AudioEncoding.LINEAR16);
        }


        RecognitionFeatures features = RecognitionFeatures.newBuilder()
                .setProfanityFilter(true)
                .setEnableAutomaticPunctuation(true)
                .setEnableWordTimeOffsets(true)
                .setEnableWordConfidence(true)
                .build();

        RecognitionConfig config = RecognitionConfig.newBuilder()
                .addLanguageCodes("ko-KR")
                .setExplicitDecodingConfig(dec.build())
                .setModel(STT_MODEL)
                .setFeatures(features)
                .build();

        RecognizeRequest req = RecognizeRequest.newBuilder()
                .setRecognizer(recognizerName())
                .setConfig(config)
                .setContent(ByteString.copyFrom(bytes))
                .build();

        RecognizeResponse res = speechClient.recognize(req);

        String text = res.getResultsList().stream()
                .map(r -> r.getAlternativesCount() > 0 ? r.getAlternatives(0).getTranscript() : "")
                .collect(Collectors.joining(" "));

        List<Word> words = new ArrayList<>();
        for (var r : res.getResultsList()) {
            if (r.getAlternativesCount() == 0) continue;
            for (var wi : r.getAlternatives(0).getWordsList()) {
                double start = wi.getStartOffset().getSeconds() + wi.getStartOffset().getNanos() / 1_000_000_000.0;
                double end   = wi.getEndOffset().getSeconds()   + wi.getEndOffset().getNanos()   / 1_000_000_000.0;
                words.add(new Word(wi.getWord(), start + baseOffsetSec, end + baseOffsetSec));
            }
        }
        return new TranscriptionResult(text, words);
    }

    /** ffprobe 로 길이(초) 측정 */
    private static double probeDurationSeconds(Path file) throws IOException {
        // ffprobe JSON 출력에서 duration을 더 성실하게 찾는다.
        Process p = new ProcessBuilder(
                "ffprobe", "-v", "error",
                "-print_format", "json",
                "-show_format",
                "-show_streams",
                file.toString()
        ).redirectErrorStream(true).start();

        String out;
        try (InputStream in = p.getInputStream()) {
            out = new String(in.readAllBytes());
            int code = p.waitFor();
            if (code != 0) throw new IOException("ffprobe failed: " + out);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("ffprobe interrupted", e);
        }

        // 1) format.duration : "95.02" 또는 "N/A"
        Double d = extractDurationNumber(out, "\"format\"\\s*:\\s*\\{[^}]*?\"duration\"\\s*:\\s*\"([^\"]+)\"");
        if (d != null && Double.isFinite(d) && d > 0) return d;

        // 2) streams[].duration (오디오 스트림에 들어있을 수 있음)
        d = extractDurationNumber(out, "\"streams\"\\s*:\\s*\\[[^\\]]*?\"duration\"\\s*:\\s*\"([^\"]+)\"");
        if (d != null && Double.isFinite(d) && d > 0) return d;

        // 3) tags.DURATION : "00:01:35.02" 같은 포맷일 수 있음
        String hms = extractFirst(out, "\"DURATION\"\\s*:\\s*\"([^\"]+)\"");
        if (hms != null) {
            double sec = parseHmsToSeconds(hms);
            if (sec > 0) return sec;
        }

        // 못 찾으면 알 수 없음
        return Double.NaN;
    }

    private static Double extractDurationNumber(String json, String regex) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.DOTALL).matcher(json);
        if (m.find()) {
            String val = m.group(1).trim();
            if (!val.equalsIgnoreCase("N/A")) {
                try { return Double.parseDouble(val); } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    private static String extractFirst(String json, String regex) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.DOTALL).matcher(json);
        return m.find() ? m.group(1).trim() : null;
    }

    private static double parseHmsToSeconds(String hms) {
        // "HH:MM:SS[.fff]" 또는 "MM:SS[.fff]" 지원
        String[] parts = hms.split(":");
        try {
            if (parts.length == 3) {
                double h = Double.parseDouble(parts[0]);
                double m = Double.parseDouble(parts[1]);
                double s = Double.parseDouble(parts[2]);
                return h * 3600 + m * 60 + s;
            } else if (parts.length == 2) {
                double m = Double.parseDouble(parts[0]);
                double s = Double.parseDouble(parts[1]);
                return m * 60 + s;
            }
        } catch (NumberFormatException ignored) {}
        return -1;
    }

    /** ffmpeg로 WAV(16kHz, mono, PCM) 청크로 분할 (안전) */
    private static List<Path> segmentToWavChunks(Path src, Path outDir, int segSec) throws IOException {
        Files.createDirectories(outDir);
        Path pattern = outDir.resolve("chunk_%03d.wav");

        // 핵심: 재인코딩으로 안정적 PCM WAV 청크 생성
        List<String> cmd = List.of(
                "ffmpeg", "-y",
                "-i", src.toString(),
                "-ac", "1",              // mono
                "-ar", "16000",          // 16 kHz
                "-c:a", "pcm_s16le",     // PCM 16-bit little-endian
                "-f", "segment",
                "-segment_time", String.valueOf(segSec),
                "-reset_timestamps", "1",
                pattern.toString()
        );

        Process p = new ProcessBuilder(cmd).inheritIO().start();
        try {
            int code = p.waitFor();
            if (code != 0) throw new IOException("ffmpeg segment(wav) failed (code " + code + ")");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("ffmpeg segment interrupted", e);
        }

        try (var s = Files.list(outDir)) {
            return s.filter(f -> f.getFileName().toString().startsWith("chunk_"))
                    .sorted()
                    .toList();
        }
    }

    // FLAC
    private static List<Path> segmentToFlacChunks(Path src, Path outDir, int segSec) throws IOException {
        Files.createDirectories(outDir);
        Path pattern = outDir.resolve("chunk_%03d.flac");

        List<String> cmd = List.of(
                "ffmpeg", "-y",
                "-i", src.toString(),
                "-ac", "1",           // mono
                "-ar", "16000",       // 16kHz
                "-c:a", "flac",       // ★ FLAC 인코딩
                "-f", "segment",
                "-segment_time", String.valueOf(segSec),
                "-reset_timestamps", "1",
                pattern.toString()
        );
        Process p = new ProcessBuilder(cmd).inheritIO().start();
        try {
            int code = p.waitFor();
            if (code != 0) throw new IOException("ffmpeg segment(flac) failed code=" + code);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("ffmpeg segment interrupted", e);
        }
        try (var s = Files.list(outDir)) {
            return s.filter(f -> f.getFileName().toString().startsWith("chunk_"))
                    .filter(f -> { try { return Files.size(f) > 1024; } catch (IOException e) { return false; }})
                    .sorted().toList();
        }
    }

    private static String safeName(String name) {
        if (name == null || name.isBlank()) return "audio.bin";
        return name.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static String getExt(String name) {
        int i = name.lastIndexOf('.');
        return (i > 0 && i < name.length() - 1) ? name.substring(i + 1) : "bin";
    }

    private static void safeDeleteRecursive(Path dir) {
        if (dir == null) return;
        try (var s = Files.walk(dir)) {
            s.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
    }
}
