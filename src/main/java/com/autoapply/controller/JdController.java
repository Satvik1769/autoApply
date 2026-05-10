package com.autoapply.controller;

import com.autoapply.dto.request.JdExtractRequest;
import com.autoapply.dto.response.JdKeywordsResponse;
import com.autoapply.service.jd.JdExtractionService;
import com.autoapply.service.jd.JdExtractionService.ExtractionResult;
import com.autoapply.service.jd.JdKeywords;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jd")
@RequiredArgsConstructor
public class JdController {

    private final JdExtractionService jdExtractionService;

    @PostMapping("/extract")
    public ResponseEntity<JdKeywordsResponse> extract(@Valid @RequestBody JdExtractRequest request) {
        ExtractionResult result = jdExtractionService.extract(request.getJdText());
        return ResponseEntity.ok(toResponse(result.keywords(), result.cached()));
    }

    private JdKeywordsResponse toResponse(JdKeywords kw, boolean cached) {
        return JdKeywordsResponse.builder()
                .roles(kw.getRoles())
                .technicalSkills(kw.getTechnicalSkills())
                .softSkills(kw.getSoftSkills())
                .mustHaveKeywords(kw.getMustHaveKeywords())
                .niceToHaveKeywords(kw.getNiceToHaveKeywords())
                .resumeKeywords(kw.getResumeKeywords())
                .experienceRequired(kw.getExperienceRequired())
                .cachedResult(cached)
                .build();
    }
}
