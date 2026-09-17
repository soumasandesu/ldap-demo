package com.example.ldap_demo.controller;

import com.example.ldap_demo.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SingleSignOnLoginApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${app.sso.cookie.name}")
    private String cookieName;

    @Value("${app.sso.cookie.domain:}")
    private String cookieDomain = "";

    @Value("${app.sso.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.sso.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${app.sso.cookie.max-age:3600}")
    private long cookieMaxAge;

    @GetMapping("/login")
    String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password.");
        }
        return "login";
    }

    @PostMapping("/login")
    ResponseEntity<Void> login(@RequestParam String username, @RequestParam String password) {
        try {
            final Authentication authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(username, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            final List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
            final String token = jwtService.generateJws(authentication.getName(), roles);
            return ResponseEntity.status(303).header(HttpHeaders.LOCATION, "/").header(HttpHeaders.SET_COOKIE, cookieBuilder(token).build().toString()).build();
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(303).header(HttpHeaders.LOCATION, "/login?error").build();
        }
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<Void> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(303).header(HttpHeaders.LOCATION, "/login").header(HttpHeaders.SET_COOKIE, cookieBuilder("").maxAge(0).build().toString()).build();
    }

    private ResponseCookie.ResponseCookieBuilder cookieBuilder(String value) {
        final ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, value).httpOnly(true).secure(cookieSecure).path("/").maxAge(cookieMaxAge).sameSite(cookieSameSite);
        if (!cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        return builder;
    }
}
