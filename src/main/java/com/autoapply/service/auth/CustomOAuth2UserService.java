package com.autoapply.service.auth;

import com.autoapply.entity.User;
import com.autoapply.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        String provider = request.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String providerId = extractProviderId(provider, attributes);
        String email = extractEmail(provider, attributes);
        String name = (String) attributes.get("name");
        String avatarUrl = extractAvatar(provider, attributes);

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

        return UserPrincipal.of(user, attributes);
    }

    private String extractProviderId(String provider, Map<String, Object> attrs) {
        if ("github".equals(provider)) {
            Object id = attrs.get("id");
            return id != null ? String.valueOf(id) : null;
        }
        return (String) attrs.get("sub");
    }

    private String extractEmail(String provider, Map<String, Object> attrs) {
        String email = (String) attrs.get("email");
        if (email == null && "github".equals(provider)) {
            // GitHub may not expose email in primary endpoint — handled gracefully with placeholder
            return null;
        }
        return email;
    }

    private String extractAvatar(String provider, Map<String, Object> attrs) {
        if ("github".equals(provider)) {
            return (String) attrs.get("avatar_url");
        }
        return (String) attrs.get("picture");
    }
}
