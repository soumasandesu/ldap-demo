package com.example.ldap_demo.controller;

import com.example.ldap_demo.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleSignOnLoginApiControllerTest {

    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private SingleSignOnLoginApiController controller;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        controller = new SingleSignOnLoginApiController(authenticationManager, jwtService);
        ReflectionTestUtils.setField(controller, "cookieName", "SSO_TOKEN");
        ReflectionTestUtils.setField(controller, "cookieSecure", false);
        ReflectionTestUtils.setField(controller, "cookieSameSite", "Lax");
        ReflectionTestUtils.setField(controller, "cookieMaxAge", 3600L);
    }

    @Test
    void rendersLoginPage() {
        ExtendedModelMap model = new ExtendedModelMap();

        assertThat(controller.loginPage("error", model)).isEqualTo("login");
        assertThat(model.get("error")).isEqualTo("Invalid username or password.");
    }

    @Test
    void issuesSsoCookieAfterSuccessfulLogin() {
        Authentication authenticated = new UsernamePasswordAuthenticationToken(
                "alice", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any())).thenReturn(authenticated);
        when(jwtService.generateJws("alice", List.of("ROLE_USER"))).thenReturn("signed-token");

        var response = controller.login("alice", "password");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SEE_OTHER);
        assertThat(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo("/");
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("SSO_TOKEN=signed-token")
                .contains("HttpOnly")
                .contains("SameSite=Lax");
    }

    @Test
    void redirectsToLoginAfterFailedLogin() {
        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        var response = controller.login("alice", "wrong");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SEE_OTHER);
        assertThat(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo("/login?error");
    }

    @Test
    void clearsSsoCookieOnLogout() {
        var response = controller.logout();

        assertThat(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo("/login");
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("SSO_TOKEN=")
                .contains("Max-Age=0");
    }
}
