package com.autoapply.service.auth;

import com.autoapply.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserPrincipal implements OidcUser, OAuth2User, UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final UUID userId;
    @Getter
    private final String email;
    private final String name;
    private final String passwordHash;
    private final Map<String, Object> attributes;

    // Non-null only for OIDC (Google) logins
    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;

    private UserPrincipal(UUID userId, String email, String name, String passwordHash,
                          Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.passwordHash = passwordHash;
        this.attributes = attributes;
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    public static UserPrincipal of(User user, Map<String, Object> attributes) {
        return new UserPrincipal(user.getId(), user.getEmail(), user.getName(),
                user.getPasswordHash(), attributes, null, null);
    }

    public static UserPrincipal ofOidc(User user, Map<String, Object> attributes,
                                       OidcIdToken idToken, OidcUserInfo userInfo) {
        return new UserPrincipal(user.getId(), user.getEmail(), user.getName(),
                user.getPasswordHash(), attributes, idToken, userInfo);
    }

    public static UserPrincipal ofLocal(User user) {
        return new UserPrincipal(user.getId(), user.getEmail(), user.getName(),
                user.getPasswordHash(), Map.of(), null, null);
    }

    // OidcUser
    @Override
    public Map<String, Object> getClaims() {
        return idToken != null ? idToken.getClaims() : attributes;
    }

    @Override
    public OidcUserInfo getUserInfo() { return userInfo; }

    @Override
    public OidcIdToken getIdToken() { return idToken; }

    // OAuth2User
    @Override
    public Map<String, Object> getAttributes() { return attributes; }

    // UserDetails + shared
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return email; }

    @Override
    public String getName() { return name != null ? name : email; }
}
