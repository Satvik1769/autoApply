package com.autoapply.service.ats.scorer;

import com.autoapply.service.jd.JdKeywords;
import com.autoapply.service.resume.ResumeJson;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class QuantificationScorer implements AtsScorer {

    private static final int MAX_SCORE = 10;

    private static final Pattern METRIC_PATTERN = Pattern.compile(
            "\\d+%|\\$\\d+|\\d+x|\\d+\\.?\\d*\\s*(million|billion|thousand|M|B|K)\\b|" +
            "\\d+\\s*(users|customers|engineers|servers|requests|transactions|ms|seconds|minutes|hours|days|months|years|TB|GB|MB)\\b|" +
            "\\b\\d{2,}\\b"
    );

    @Override
    public CategoryScore score(ResumeJson resume, JdKeywords jd, int yearsExperience) {
        List<String> allBullets = resume.getSections().getExperience().stream()
                .flatMap(exp -> exp.getBullets().stream())
                .toList();

        if (allBullets.isEmpty()) {
            return new CategoryScore("QUANTIFICATION", 0, MAX_SCORE,
                    "Add quantified metrics to your experience bullets (e.g., '30% faster', 'served 10K users').");
        }

        long quantified = allBullets.stream()
                .filter(b -> METRIC_PATTERN.matcher(b).find())
                .count();
        int pts = (int) Math.round((double) quantified / allBullets.size() * MAX_SCORE);

        String recommendation;
        if (pts >= MAX_SCORE) {
            recommendation = "Strong use of quantified metrics throughout your experience.";
        } else {
            long missing = allBullets.size() - quantified;
            recommendation = String.format(
                    "+%d pts if you add numbers or metrics to %d more bullet(s) (e.g., percentages, user counts, time saved).",
                    MAX_SCORE - pts, missing);
        }

        return new CategoryScore("QUANTIFICATION", pts, MAX_SCORE, recommendation);
    }
}
