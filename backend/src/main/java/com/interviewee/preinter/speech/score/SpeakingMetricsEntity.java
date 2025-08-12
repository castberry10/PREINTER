package com.interviewee.preinter.speech.score;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Data
@RedisHash("speaking:metrics")
public class SpeakingMetricsEntity {

    @Id
    private String id;            // = sessionId

    private boolean available;
    private double score;         // 0~100
    private double articulationRate;
    private double pauseRatio;
    private int    longPauseCount;
    private String algoVersion;

    @TimeToLive
    private Long ttlSec;          // 초 단위 TTL (null이면 무한)
}

