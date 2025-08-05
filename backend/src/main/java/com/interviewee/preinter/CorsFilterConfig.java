package com.interviewee.preinter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

@Configuration
public class CorsFilterConfig {
    @Bean
    public CorsFilter CorsFilterConfig() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Collections.singletonList("*"));  // 모든 Origin
        config.setAllowedMethods(Collections.singletonList("*"));  // 모든 메서드
        config.setAllowedHeaders(Collections.singletonList("*"));  // 모든 헤더
        config.setAllowCredentials(false);                         // 인증정보 허용 여부
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);           // 모든 경로에 적용
        return new CorsFilter(source);
    }
}
