package com.autoapply.service.ats.scorer;

import com.autoapply.service.jd.JdKeywords;
import com.autoapply.service.resume.ResumeJson;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ActionVerbScorer implements AtsScorer {

    private static final int MAX_SCORE = 15;

    private static final Set<String> STRONG_VERBS = Set.of(
        "led", "built", "designed", "architected", "developed", "implemented",
        "delivered", "deployed", "reduced", "increased", "improved", "optimized",
        "automated", "migrated", "refactored", "launched", "managed", "owned",
        "collaborated", "integrated", "spearheaded", "drove", "established",
        "streamlined", "accelerated", "transformed", "championed", "mentored",
        "scaled", "secured", "resolved", "analyzed", "researched", "coordinated",
        "created", "initiated", "oversaw", "supervised", "maintained", "supported",
        "enhanced", "boosted", "generated", "facilitated", "negotiated", "proposed",
        "executed", "monitored", "evaluated", "trained", "reviewed", "audited",
        "configured", "provisioned", "orchestrated", "onboarded", "published",
        "contributed", "planned", "documented", "standardized", "consolidated"
    );

    @Override
    public CategoryScore score(ResumeJson resume, JdKeywords jd, int yearsExperience) {
        List<String> allBullets = resume.getSections().getExperience().stream()
                .flatMap(exp -> exp.getBullets().stream())
                .toList();

        if (allBullets.isEmpty()) {
            return new CategoryScore("ACTION_VERBS", 0, MAX_SCORE,
                    "No experience bullets found. Add bullet points starting with strong action verbs.");
        }

        long strongCount = allBullets.stream().filter(this::startsWithStrongVerb).count();
        int pts = (int) Math.round((double) strongCount / allBullets.size() * MAX_SCORE);

        String recommendation;
        if (pts == MAX_SCORE) {
            recommendation = "Excellent! All bullets start with strong action verbs.";
        } else {
            long weak = allBullets.size() - strongCount;
            recommendation = String.format(
                    "+%d pts if you rewrite %d bullet(s) to start with strong verbs (e.g., 'Led', 'Built', 'Reduced').",
                    MAX_SCORE - pts, weak);
        }

        return new CategoryScore("ACTION_VERBS", pts, MAX_SCORE, recommendation);
    }

    private boolean startsWithStrongVerb(String bullet) {
        if (bullet == null || bullet.isBlank()) return false;
        String firstWord = bullet.trim().split("\\s+")[0].toLowerCase().replaceAll("[^a-z]", "");
        return STRONG_VERBS.contains(firstWord);
    }
}
