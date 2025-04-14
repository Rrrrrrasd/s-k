package com.contract.backend.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // ObjectMapper에 Java 8 지원 모듈 추가 (Optional 등 처리 가능)
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        return mapper;
    }

    // PublicKeyCredentialCreationOptions용 RedisTemplate
    @Bean
    public RedisTemplate<String, PublicKeyCredentialCreationOptions> creationOptionsRedisTemplate(
            RedisConnectionFactory factory,
            ObjectMapper objectMapper
    ) {
        RedisTemplate<String, PublicKeyCredentialCreationOptions> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        return template;
    }

    // PublicKeyCredentialRequestOptions용 RedisTemplate
    @Bean
    public RedisTemplate<String, PublicKeyCredentialRequestOptions> requestOptionsRedisTemplate(
            RedisConnectionFactory factory,
            ObjectMapper objectMapper
    ) {
        RedisTemplate<String, PublicKeyCredentialRequestOptions> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        return template;
    }
}