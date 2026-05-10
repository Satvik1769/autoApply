package com.autoapply.service.jd;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JdKeywords {
    private List<String> roles;
    private List<String> technicalSkills;
    private List<String> softSkills;
    private List<String> mustHaveKeywords;
    private List<String> niceToHaveKeywords;
    private List<String> resumeKeywords;
    private String experienceRequired;
}
