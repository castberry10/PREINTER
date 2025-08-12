package com.interviewee.preinter.speech.score;

import com.interviewee.preinter.repository.SpeakingMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpeakingMetricsStoreService {
    private final SpeakingMetricsRepository repo;

    @Value("${speaking.metrics.ttl-sec:3600}") // 1hour
    private long defaultTtlSec;

    public void put(String sessionId, SpeakingMetrics m) {
        SpeakingMetricsEntity e = new SpeakingMetricsEntity();
        e.setId(sessionId);
        e.setAvailable(m.available());
        e.setScore(m.score());
        e.setArticulationRate(m.articulationRate());
        e.setPauseRatio(m.pauseRatio());
        e.setLongPauseCount(m.longPauseCount());
        e.setAlgoVersion(m.algoVersion());
        e.setTtlSec(defaultTtlSec);
        repo.save(e);
    }

    public SpeakingMetrics getOrNull(String sessionId) {
        Optional<SpeakingMetricsEntity> opt = repo.findById(sessionId);
        return opt.map(x -> new SpeakingMetrics(
                x.isAvailable(),
                x.getScore(),
                x.getArticulationRate(),
                x.getPauseRatio(),
                x.getLongPauseCount(),
                x.getAlgoVersion()
        )).orElse(null);
    }

    public void delete(String sessionId) {
        repo.deleteById(sessionId);
    }
}
