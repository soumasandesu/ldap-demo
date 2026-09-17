package com.example.ldap_demo.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class KeysConfig {
    @Bean
    SecretKey key(@Value("${app.jwt.secret}") String key) {
        return Keys.hmacShaKeyFor(key.getBytes());
    }
}
