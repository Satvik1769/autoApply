package com.autoapply.controller;

import com.autoapply.dto.request.AtsScoreRequest;
import com.autoapply.dto.response.AtsScoreResponse;
import com.autoapply.entity.AtsScore;
import com.autoapply.service.ats.AtsScoreService;
import com.autoapply.service.ats.scorer.CategoryScore;
import com.autoapply.service.auth.UserPrincipal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ats")
@RequiredArgsConstructor
public class AtsController {

    private final AtsScoreService atsScoreService;
    private final ObjectMapper objectMapper;

    @PostMapping("/score")
    public ResponseEntity<AtsScoreResponse> score(
            @Valid @RequestBody AtsScoreRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        AtsScore atsScore = atsScoreService.score(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(atsScore));
    }

    @GetMapping("/scores")
    public ResponseEntity<List<AtsScoreResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        List<AtsScoreResponse> responses = atsScoreService.listForUser(principal.getUserId())
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/scores/{id}")
    public ResponseEntity<AtsScoreResponse> get(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(toResponse(atsScoreService.getForUser(id, principal.getUserId())));
    }

    private AtsScoreResponse toResponse(AtsScore atsScore) {
        List<CategoryScore> breakdown = List.of();
        if (atsScore.getCategoryBreakdown() != null) {
            try {
                breakdown = objectMapper.readValue(atsScore.getCategoryBreakdown(),
                        new TypeReference<>() {});
            } catch (Exception ignored) {}
        }

        return AtsScoreResponse.builder()
                .id(atsScore.getId())
                .resumeId(atsScore.getResume().getId())
                .totalScore(atsScore.getTotalScore())
                .maxScore(atsScore.getMaxScore())
                .jdTextSnippet(atsScore.getJdTextSnippet())
                .categoryBreakdown(breakdown)
                .createdAt(atsScore.getCreatedAt())
                .build();
    }
}
