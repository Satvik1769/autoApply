package com.autoapply.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 20, message = "Maximum 20 target roles allowed")
    private String[] targetRoles;

    @Min(value = 0, message = "Years of experience cannot be negative")
    private Short yearsExperience;

    @Size(max = 20, message = "Maximum 20 preferred locations allowed")
    private String[] preferredLocations;

    @Size(max = 50, message = "Maximum 50 preferred skills allowed")
    private String[] preferredSkills;
}
