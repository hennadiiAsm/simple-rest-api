package com.example.simple_rest_api.securiry;

import org.springframework.security.core.GrantedAuthority;


public enum Authority implements GrantedAuthority {
    READ,
    WRITE;

    @Override
    public String getAuthority() {
        return this.name();
    }
}
