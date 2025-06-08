package com.contract.backend.common.config;

import com.contract.backend.common.util.jwt.JwtAuthenticationFilter;
import com.contract.backend.common.util.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider) throws Exception {
        http
            .cors()
            .and()
            .csrf().disable()
            .authorizeHttpRequests()
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers("/api/users/search").authenticated()
            .requestMatchers("/api/contracts/files/preview/**").authenticated() // 파일 미리보기 경로 추가
            .requestMatchers("/api/contracts/files/download/**").authenticated() // 파일 다운로드 경로 추가
            .requestMatchers("/api/contracts/files/**").authenticated() // 기타 파일 관련 API
            .requestMatchers("/api/contracts/**").authenticated() // 모든 계약서 API
            .requestMatchers("/api/folders/**").authenticated() //폴더
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://localhost:5173")); // React 개발 서버
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type","X-XSRF-TOKEN","Authorization", "Range")); // Range 헤더 추가
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Content-Range", "Accept-Ranges", "Content-Length")); // Range 관련 헤더 노출

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}