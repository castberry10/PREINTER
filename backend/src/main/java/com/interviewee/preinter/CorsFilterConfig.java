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
        // 모든 도메인·포트 허용
        config.setAllowedOriginPatterns(List.of("*"));
        // 또는 setAllowedOrigins(List.of("*")) 도 가능합니다만,
        // 스프링 6.1+에선 와일드카드 패턴을 지원하는 setAllowedOriginPatterns가 권장됩니다.

        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // 인증정보(쿠키·Authorization 헤더) 필요 없으면 false, 필요하면 true로 설정
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
