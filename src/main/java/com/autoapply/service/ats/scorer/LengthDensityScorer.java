package com.autoapply.service.ats.scorer;

import com.autoapply.config.AppProperties;
import com.autoapply.service.jd.JdKeywords;
import com.autoapply.service.resume.ResumeJson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class LengthDensityScorer implements AtsScorer {

    private static final int MAX_SCORE = 10;

    private final AppProperties appProperties;

    @Override
    public CategoryScore score(ResumeJson resume, JdKeywords jd, int yearsExperience) {
        String allText = buildAllText(resume);
        int wordCount = allText.isBlank() ? 0 : allText.trim().split("\\s+").length;

        AppProperties.Ats ats = appProperties.getAts();
        boolean isSenior = yearsExperience >= ats.getYearsExperienceThreshold();

        int min = isSenior ? ats.getTargetWords().getSeniorMin() : ats.getTargetWords().getJuniorMin();
        int max = isSenior ? ats.getTargetWords().getSeniorMax() : ats.getTargetWords().getJuniorMax();

        int pts;
        String recommendation;

        if (wordCount >= min && wordCount <= max) {
            pts = MAX_SCORE;
            recommendation = "Resume length is ideal (" + wordCount + " words).";
        } else if (wordCount >= min - 100 && wordCount <= max + 100) {
            pts = 7;
            recommendation = String.format(
                    "Resume length (%d words) is close to ideal. Target %d–%d words for your experience level.",
                    wordCount, min, max);
        } else if (wordCount < min) {
            pts = 3;
            recommendation = String.format(
                    "Resume is too short (%d words). Add more detail to reach %d–%d words.",
                    wordCount, min, max);
        } else {
            pts = 3;
            recommendation = String.format(
                    "Resume is too long (%d words). Trim to %d–%d words — focus on impact over volume.",
                    wordCount, min, max);
        }

        return new CategoryScore("LENGTH_DENSITY", pts, MAX_SCORE, recommendation);
    }

    private String buildAllText(ResumeJson resume) {
        StringBuilder sb = new StringBuilder();
        if (resume.getSections().getSummary() != null) sb.append(resume.getSections().getSummary()).append(" ");
        resume.getSections().getExperience().forEach(exp -> {
            if (exp.getTitle() != null) sb.append(exp.getTitle()).append(" ");
            exp.getBullets().forEach(b -> sb.append(b).append(" "));
        });
        resume.getSections().getSkills().forEach(s -> sb.append(s).append(" "));
        resume.getSections().getEducation().forEach(edu -> {
            if (edu.getDegree() != null) sb.append(edu.getDegree()).append(" ");
        });
        resume.getSections().getProjects().forEach(p -> {
            if (p.getName() != null) sb.append(p.getName()).append(" ");
            p.getBullets().forEach(b -> sb.append(b).append(" "));
        });
        return sb.toString();
    }
}
