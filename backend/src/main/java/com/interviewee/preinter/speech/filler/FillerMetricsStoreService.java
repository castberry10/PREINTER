package com.interviewee.preinter.speech.filler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewee.preinter.repository.FillerMetricsRepository;
import com.interviewee.preinter.speech.filler.dto.FillerMetricsEntity;
import com.interviewee.preinter.speech.filler.dto.FillerMetricsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FillerMetricsStoreService {

    private final FillerMetricsRepository repo;
    private final ObjectMapper objectMapper;

    @Value("${filler.metrics.ttl-sec:3600}")
    private long defaultTtlSec;

    public void put(String sessionId, FillerMetricsResult result, String algoVersion) {
        FillerMetricsEntity e = new FillerMetricsEntity();
        e.setId(sessionId);
        e.setAvailable(result != null);
        e.setAlgoVersion(algoVersion);

        try {
            String json = objectMapper.writeValueAsString(result);
            e.setPayloadJson(json);
        } catch (JsonProcessingException ex) {
            // 직렬화 실패 시 available=false로 저장하거나 예외 던질지 선택
            e.setAvailable(false);
            e.setPayloadJson(null);
        }

        e.setTtlSec(defaultTtlSec);
        repo.save(e);
    }

    public FillerMetricsResult getOrNull(String sessionId) {
        Optional<FillerMetricsEntity> opt = repo.findById(sessionId);
        if (opt.isEmpty()) return null;

        FillerMetricsEntity e = opt.get();
        if (!e.isAvailable() || e.getPayloadJson() == null) return null;

        try {
            return objectMapper.readValue(e.getPayloadJson(), FillerMetricsResult.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public void delete(String sessionId) {
        repo.deleteById(sessionId);
    }
}
