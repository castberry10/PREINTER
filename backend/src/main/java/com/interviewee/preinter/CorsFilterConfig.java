package com.interviewee.preinter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsFilterConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ── ① cors 설정 람다 방식으로 바꾸기 ─────────────────────────────────────────
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource())
                )
                // ── ② CSRF 비활성화, 필요에 따라 제거하세요 ─────────────────────────────────
                .csrf(csrf -> csrf.disable())
                // ── ③ 인증/인가 설정 ───────────────────────────────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()  // pre-flight 허용
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 개발 중엔 "*" 사용 가능, 운영에선 정확한 도메인 명시하세요
        config.setAllowedOriginPatterns(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // config.setAllowCredentials(true);  // 쿠키·인증헤더 허용 시 활성화

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
