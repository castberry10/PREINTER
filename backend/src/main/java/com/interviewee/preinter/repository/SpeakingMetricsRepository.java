package com.interviewee.preinter.repository;

import com.interviewee.preinter.speech.score.SpeakingMetricsEntity;
import org.springframework.data.repository.CrudRepository;

public interface SpeakingMetricsRepository extends CrudRepository<SpeakingMetricsEntity, String> {
}
