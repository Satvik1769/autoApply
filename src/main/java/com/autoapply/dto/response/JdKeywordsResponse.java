package com.autoapply.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JdKeywordsResponse {
    private List<String> roles;
    private List<String> technicalSkills;
    private List<String> softSkills;
    private List<String> mustHaveKeywords;
    private List<String> niceToHaveKeywords;
    private List<String> resumeKeywords;
    private String experienceRequired;
    private boolean cachedResult;
}
