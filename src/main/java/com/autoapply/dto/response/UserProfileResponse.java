package com.autoapply.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private UUID id;
    private String email;
    private String name;
    private String avatarUrl;
    private String provider;
    private String[] targetRoles;
    private Short yearsExperience;
    private String[] preferredLocations;
    private String[] preferredSkills;
}
