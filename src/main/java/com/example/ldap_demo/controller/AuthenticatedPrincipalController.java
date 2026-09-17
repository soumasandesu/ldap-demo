package com.example.ldap_demo.controller;

import com.example.ldap_demo.service.LdapPrincipalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AuthenticatedPrincipalController {

    private final LdapPrincipalService ldapPrincipalService;

    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    String home(Authentication authentication, Model model) {
        model.addAttribute("profile", ldapPrincipalService.profile(authentication));
        return "home";
    }
}
