package com.example.ldap_demo.service;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JwtService {

    // 實務上由 Security Config 或 App Properties Inject Key
    private final SecretKey key;

    public String generateJws(String username, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600 * 1000)) // 1 hour
                .signWith(key)
                .compact();
    }
}