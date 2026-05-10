package com.autoapply.service.ats.scorer;

public record CategoryScore(
        String category,
        int rawScore,
        int maxScore,
        String recommendation
) {}
