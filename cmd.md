# run lldap (web ui of LDAP server)
```bash
docker run -d --name lldap -p 3890:3890 -p 17170:17170 -e LLDAP_JWT_SECRET=secret123 -e LLDAP_LDAP_USER_PASS=admin123 -e LLDAP_LDAP_BASE_DN=dc=example,dc=com lldap/lldap:stable
```