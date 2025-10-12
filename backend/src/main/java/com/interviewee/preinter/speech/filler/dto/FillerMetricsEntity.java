package com.interviewee.preinter.speech.filler.dto;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.List;

@Getter
@Setter
@RedisHash("fillerMetrics")
public class FillerMetricsEntity {
    @Id
    private String id;            // sessionId

    private boolean available;    // 결과 유효 플래그
    private String algoVersion;   // "filler-v1" 등

    private String payloadJson;   // FillerMetricsResult 전체 JSON

    @TimeToLive
    private Long ttlSec;
}
