package com.autoapply.controller;

import com.autoapply.dto.request.UpdateProfileRequest;
import com.autoapply.dto.response.UserProfileResponse;
import com.autoapply.entity.User;
import com.autoapply.exception.ResourceNotFoundException;
import com.autoapply.repository.UserRepository;
import com.autoapply.service.auth.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(toProfileResponse(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getTargetRoles() != null)       user.setTargetRoles(request.getTargetRoles());
        if (request.getYearsExperience() != null)   user.setYearsExperience(request.getYearsExperience());
        if (request.getPreferredLocations() != null) user.setPreferredLocations(request.getPreferredLocations());
        if (request.getPreferredSkills() != null)   user.setPreferredSkills(request.getPreferredSkills());

        return ResponseEntity.ok(toProfileResponse(userRepository.save(user)));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .provider(user.getProvider())
                .targetRoles(user.getTargetRoles())
                .yearsExperience(user.getYearsExperience())
                .preferredLocations(user.getPreferredLocations())
                .preferredSkills(user.getPreferredSkills())
                .build();
    }
}
