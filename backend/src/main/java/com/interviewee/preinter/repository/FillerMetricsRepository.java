package com.interviewee.preinter.repository;

import com.interviewee.preinter.speech.filler.dto.FillerMetricsEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FillerMetricsRepository extends CrudRepository<FillerMetricsEntity, String> {
}
