package com.example.simple_rest_api.securiry;

import org.springframework.security.core.GrantedAuthority;


public enum Role implements GrantedAuthority {
    BASIC,
    ADMIN;

    public String prefixed() {
        return "ROLE_" + name();
    }

    @Override
    public String getAuthority() {
        return name();
    }
}
