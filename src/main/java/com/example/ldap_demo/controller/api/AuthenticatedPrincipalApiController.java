package com.example.ldap_demo.controller.api;

import com.example.ldap_demo.service.LdapPrincipalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AuthenticatedPrincipalApiController {
    private final LdapPrincipalService ldapPrincipalService;

    @GetMapping("/me")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    LdapPrincipalService.LdapPrincipalProfile apiMe(Authentication authentication) {
        return ldapPrincipalService.profile(authentication);
    }
}
