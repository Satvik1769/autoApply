package com.autoapply.controller;

import com.autoapply.dto.request.LoginRequest;
import com.autoapply.dto.request.RegisterRequest;
import com.autoapply.dto.response.UserResponse;
import com.autoapply.entity.User;
import com.autoapply.repository.UserRepository;
import com.autoapply.service.auth.LocalAuthService;
import com.autoapply.service.auth.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final LocalAuthService localAuthService;
    private final HttpSessionSecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        User user = localAuthService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest req,
                                               HttpServletRequest servletRequest,
                                               HttpServletResponse servletResponse) {
        Authentication auth = localAuthService.login(req);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, servletRequest, servletResponse);

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB"));
        return ResponseEntity.ok(toResponse(user));
    }

    @GetMapping("/providers")
    public ResponseEntity<?> providers() {
        return ResponseEntity.ok(Map.of("providers", List.of(
            Map.of("name", "google",  "displayName", "Sign in with Google",  "url", "/oauth2/authorization/google"),
            Map.of("name", "github",  "displayName", "Sign in with GitHub",  "url", "/oauth2/authorization/github"),
            Map.of("name", "local",   "displayName", "Email & Password",     "url", "/api/v1/auth/login")
        )));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB"));
        return ResponseEntity.ok(toResponse(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest servletRequest) {
        servletRequest.getSession(false);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .provider(user.getProvider())
                .build();
    }
}
