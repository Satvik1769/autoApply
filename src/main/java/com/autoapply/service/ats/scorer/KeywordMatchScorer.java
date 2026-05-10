package com.autoapply.service.ats.scorer;

import com.autoapply.service.jd.JdKeywords;
import com.autoapply.service.resume.ResumeJson;
import com.autoapply.util.TextNormalizer;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class KeywordMatchScorer implements AtsScorer {

    private static final int MAX_SCORE = 35;
    private static final int MAX_MUST_HAVE_PTS = 20;
    private static final int MAX_NICE_TO_HAVE_PTS = 15;

    @Override
    public CategoryScore score(ResumeJson resume, JdKeywords jd, int yearsExperience) {
        Set<String> resumeTerms = buildResumeTermSet(resume);

        List<String> mustHave = coalesce(jd.getMustHaveKeywords(), jd.getTechnicalSkills());
        List<String> niceToHave = coalesce(jd.getNiceToHaveKeywords(), jd.getSoftSkills());

        List<String> missedMust = new ArrayList<>();
        int mustPts = 0;
        for (String kw : mustHave) {
            String norm = TextNormalizer.normalize(kw);
            if (resumeTerms.contains(norm)) {
                mustPts += 2;
            } else {
                missedMust.add(kw);
            }
        }
        mustPts = Math.min(mustPts, MAX_MUST_HAVE_PTS);

        int nicePts = 0;
        for (String kw : niceToHave) {
            String norm = TextNormalizer.normalize(kw);
            if (resumeTerms.contains(norm)) nicePts += 1;
        }
        nicePts = Math.min(nicePts, MAX_NICE_TO_HAVE_PTS);

        int total = mustPts + nicePts;

        String recommendation;
        if (missedMust.isEmpty()) {
            recommendation = "Great keyword coverage! All key skills are present.";
        } else {
            List<String> top3 = missedMust.stream().limit(3).toList();
            recommendation = "Add these missing keywords to improve score: " + String.join(", ", top3) + ".";
        }

        return new CategoryScore("KEYWORD_MATCH", total, MAX_SCORE, recommendation);
    }

    private Set<String> buildResumeTermSet(ResumeJson resume) {
        Stream<String> skillTerms = resume.getSections().getSkills().stream();
        Stream<String> bulletTerms = resume.getSections().getExperience().stream()
                .flatMap(exp -> exp.getBullets().stream())
                .flatMap(bullet -> Arrays.stream(bullet.split("\\s+")));
        return Stream.concat(skillTerms, bulletTerms)
                .map(TextNormalizer::normalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    private List<String> coalesce(List<String> primary, List<String> fallback) {
        if (primary != null && !primary.isEmpty()) return primary;
        return fallback != null ? fallback : List.of();
    }
}
