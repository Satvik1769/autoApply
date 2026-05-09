package com.autoapply.service.auth;

import com.autoapply.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserPrincipal implements OAuth2User, UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final UUID userId;
    @Getter
    private final String email;
    private final String name;
    private final String passwordHash;
    private final Map<String, Object> attributes;

    private UserPrincipal(UUID userId, String email, String name, String passwordHash, Map<String, Object> attributes) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.passwordHash = passwordHash;
        this.attributes = attributes;
    }

    public static UserPrincipal of(User user, Map<String, Object> attributes) {
        return new UserPrincipal(user.getId(), user.getEmail(), user.getName(), user.getPasswordHash(), attributes);
    }

    public static UserPrincipal ofLocal(User user) {
        return new UserPrincipal(user.getId(), user.getEmail(), user.getName(), user.getPasswordHash(), Map.of());
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getName() {
        return name != null ? name : email;
    }
}
