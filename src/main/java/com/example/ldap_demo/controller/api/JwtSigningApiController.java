package com.example.ldap_demo.controller.api;

import com.example.ldap_demo.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jwt")
@RequiredArgsConstructor
public class JwtSigningApiController {
    private final JwtService jwtService;

    @org.springframework.beans.factory.annotation.Value("${app.jwt.cookie-name}")
    private String cookieName;

    @org.springframework.beans.factory.annotation.Value("${app.sso.cookie.domain:}")
    private String cookieDomain;

    @org.springframework.beans.factory.annotation.Value("${app.sso.cookie.secure:false}")
    private boolean cookieSecure;

    @org.springframework.beans.factory.annotation.Value("${app.sso.cookie.same-site:Lax}")
    private String cookieSameSite;

    @org.springframework.beans.factory.annotation.Value("${app.sso.cookie.max-age:3600}")
    private long cookieMaxAge;

    @GetMapping("/assign")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<Void> getAssignJwt(Authentication authentication) {
        final String token = jwtService.generateJws(
                authentication.getName(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList());

        final ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(cookieMaxAge)
                .sameSite(cookieSameSite);
        if (!cookieDomain.isBlank()) {
            cookieBuilder.domain(cookieDomain);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString())
                .body(null);
    }
}
