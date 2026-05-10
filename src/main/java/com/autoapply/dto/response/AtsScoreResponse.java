package com.autoapply.dto.response;

import com.autoapply.service.ats.scorer.CategoryScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtsScoreResponse {
    private UUID id;
    private UUID resumeId;
    private int totalScore;
    private int maxScore;
    private String jdTextSnippet;
    private List<CategoryScore> categoryBreakdown;
    private OffsetDateTime createdAt;
}
