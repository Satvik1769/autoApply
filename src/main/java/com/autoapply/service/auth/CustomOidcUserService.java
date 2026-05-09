package com.autoapply.service.auth;

import com.autoapply.entity.User;
import com.autoapply.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(request);
        String provider = request.getClientRegistration().getRegistrationId(); // "google"

        String providerId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String avatarUrl = oidcUser.getPicture();

        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .map(existing -> {
                    existing.setLastLoginAt(OffsetDateTime.now());
                    if (name != null) existing.setName(name);
                    if (avatarUrl != null) existing.setAvatarUrl(avatarUrl);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .provider(provider)
                                .providerId(providerId)
                                .email(email != null ? email : provider + "_" + providerId + "@placeholder.local")
                                .name(name)
                                .avatarUrl(avatarUrl)
                                .build()
                ));

        return UserPrincipal.ofOidc(user, oidcUser.getAttributes(),
                oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}
