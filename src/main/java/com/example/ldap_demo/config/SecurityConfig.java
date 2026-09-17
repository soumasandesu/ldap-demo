package com.example.ldap_demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .securityContext(context -> context.requireExplicitSave(false))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(apiAuthenticationEntryPoint(), new AntPathRequestMatcher("/api/**"))
                        .defaultAuthenticationEntryPointFor(webAuthenticationEntryPoint(), AnyRequestMatcher.INSTANCE)
                        .defaultAccessDeniedHandlerFor(apiAccessDeniedHandler(), new AntPathRequestMatcher("/api/**")));
        return http.build();
    }

    private AuthenticationEntryPoint apiAuthenticationEntryPoint() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"unauthorized\"}");
        };
    }

    private AuthenticationEntryPoint webAuthenticationEntryPoint() {
        return (request, response, exception) -> response.sendRedirect("/login");
    }

    private AccessDeniedHandler apiAccessDeniedHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"forbidden\"}");
        };
    }

    @Bean
    AuthenticationManager authenticationManager(
            @Value("${spring.ldap.urls}") String ldapUrl,
            @Value("${spring.ldap.base}") String ldapBase,
            @Value("${app.ldap.people-search-base:ou=people}") String peopleSearchBase) throws Exception {
        DefaultSpringSecurityContextSource contextSource =
                new DefaultSpringSecurityContextSource(ldapUrl + "/" + ldapBase);
        contextSource.afterPropertiesSet();

        LdapBindAuthenticationManagerFactory factory =
                new LdapBindAuthenticationManagerFactory(contextSource);
        factory.setUserSearchBase(peopleSearchBase);
        factory.setUserSearchFilter("(uid={0})");
        return factory.createAuthenticationManager();
    }
}
