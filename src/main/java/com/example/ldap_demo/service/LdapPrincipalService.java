package com.example.ldap_demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.stereotype.Service;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LdapPrincipalService {

    private final LdapTemplate ldapTemplate;

    @Value("${app.ldap.people-search-base:ou=people}")
    private String peopleSearchBase;

    public LdapPrincipalProfile profile(Authentication authentication) {
        final Object principal = authentication.getPrincipal();
        final Map<String, Object> identity = new LinkedHashMap<>();
        identity.put("name", authentication.getName());
        identity.put("principalType", principal.getClass().getName());
        identity.put("authorities", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        if (principal instanceof LdapUserDetails ldapUserDetails) {
            identity.put("dn", ldapUserDetails.getDn());
        }

        return new LdapPrincipalProfile(identity, loadAttributes(authentication.getName()));
    }

    private Map<String, Object> loadAttributes(String username) {
        final List<Map<String, Object>> entries = ldapTemplate.search(
                LdapQueryBuilder.query()
                        .base(peopleSearchBase)
                        .where("uid")
                        .is(username),
                (ContextMapper<Map<String, Object>>) context -> attributesOf((DirContextOperations) context));
        return entries.stream().findFirst().orElseGet(LinkedHashMap::new);
    }

    private Map<String, Object> attributesOf(DirContextOperations context) throws NamingException {
        final Attributes attributes = context.getAttributes();
        final Map<String, Object> result = new LinkedHashMap<>();
        final NamingEnumeration<? extends Attribute> allAttributes = attributes.getAll();
        try {
            while (allAttributes.hasMore()) {
                final Attribute attribute = allAttributes.next();
                final List<Object> values = new ArrayList<>();
                final NamingEnumeration<?> allValues = attribute.getAll();
                try {
                    while (allValues.hasMore()) {
                        values.add(normalizeValue(allValues.next()));
                    }
                } finally {
                    allValues.close();
                }
                result.put(attribute.getID(), values);
            }
        } finally {
            allAttributes.close();
        }
        return result;
    }

    private Object normalizeValue(Object value) {
        if (value instanceof byte[] bytes) {
            return Base64.getEncoder().encodeToString(bytes);
        }
        return value;
    }

    public record LdapPrincipalProfile(
            Map<String, Object> identity,
            Map<String, Object> ldapAttributes) {
    }
}
