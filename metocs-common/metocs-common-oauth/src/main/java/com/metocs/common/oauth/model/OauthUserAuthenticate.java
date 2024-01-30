package com.metocs.common.oauth.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.Collection;
import java.util.Map;

public class OauthUserAuthenticate implements OAuth2AuthenticatedPrincipal {

    private final Map<String, Object> attributes;
    private final Collection<GrantedAuthority> authorities;
    private final String name;

    public OauthUserAuthenticate(Map<String, Object> attributes, Collection<GrantedAuthority> authorities, String name) {
        this.attributes = attributes;
        this.authorities = authorities;
        this.name = name;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
