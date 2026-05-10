package com.autoapply.service.ats;

import com.autoapply.dto.request.AtsScoreRequest;
import com.autoapply.entity.AtsScore;
import com.autoapply.entity.Resume;
import com.autoapply.entity.User;
import com.autoapply.exception.ResourceNotFoundException;
import com.autoapply.repository.AtsScoreRepository;
import com.autoapply.repository.ResumeRepository;
import com.autoapply.repository.UserRepository;
import com.autoapply.service.ats.scorer.AtsScorer;
import com.autoapply.service.ats.scorer.CategoryScore;
import com.autoapply.service.jd.JdExtractionService;
import com.autoapply.service.jd.JdKeywords;
import com.autoapply.service.resume.ResumeJson;
import com.autoapply.util.HashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AtsScoreService {

    private final AtsScoreRepository atsScoreRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final JdExtractionService jdExtractionService;
    private final List<AtsScorer> scorers;
    private final ObjectMapper objectMapper;

    @Transactional
    public AtsScore score(AtsScoreRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Resume resume = resumeRepository.findByIdAndUserId(request.getResumeId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        if (!"PARSED".equals(resume.getParseStatus())) {
            throw new IllegalStateException("Resume has not been parsed yet — status: " + resume.getParseStatus());
        }

        ResumeJson resumeJson = deserializeResume(resume.getParsedJson());
        JdKeywords jdKeywords = jdExtractionService.extract(request.getJdText()).keywords();

        int yearsExperience = user.getYearsExperience() != null ? user.getYearsExperience() : 0;

        List<CategoryScore> breakdown = scorers.stream()
                .map(scorer -> scorer.score(resumeJson, jdKeywords, yearsExperience))
                .toList();

        int total = breakdown.stream().mapToInt(CategoryScore::rawScore).sum();
        String jdHash = HashUtil.sha256(request.getJdText());
        String snippet = request.getJdText().substring(0, Math.min(500, request.getJdText().length()));

        AtsScore atsScore = AtsScore.builder()
                .user(user)
                .resume(resume)
                .jdHash(jdHash)
                .jdTextSnippet(snippet)
                .totalScore((short) total)
                .maxScore((short) 100)
                .categoryBreakdown(serializeBreakdown(breakdown))
                .build();

        return atsScoreRepository.save(atsScore);
    }

    public List<AtsScore> listForUser(UUID userId) {
        return atsScoreRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public AtsScore getForUser(UUID id, UUID userId) {
        return atsScoreRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ATS score not found"));
    }

    private ResumeJson deserializeResume(String json) {
        try {
            return objectMapper.readValue(json, ResumeJson.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize resume JSON", ex);
        }
    }

    private String serializeBreakdown(List<CategoryScore> breakdown) {
        try {
            return objectMapper.writeValueAsString(breakdown);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize category breakdown", ex);
        }
    }
}
