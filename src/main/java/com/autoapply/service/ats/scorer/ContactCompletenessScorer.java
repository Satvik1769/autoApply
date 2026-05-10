package com.autoapply.service.ats.scorer;

import com.autoapply.service.jd.JdKeywords;
import com.autoapply.service.resume.ResumeJson;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ContactCompletenessScorer implements AtsScorer {

    private static final int MAX_SCORE = 10;

    @Override
    public CategoryScore score(ResumeJson resume, JdKeywords jd, int yearsExperience) {
        ResumeJson.Contact contact = resume.getContact();
        List<String> missing = new ArrayList<>();
        int pts = 0;

        if (contact.getEmail() != null && !contact.getEmail().isBlank()) {
            pts += 4;
        } else {
            missing.add("email");
        }
        if (contact.getPhone() != null && !contact.getPhone().isBlank()) {
            pts += 3;
        } else {
            missing.add("phone number");
        }
        if (contact.getLinkedin() != null && !contact.getLinkedin().isBlank()) {
            pts += 3;
        } else {
            missing.add("LinkedIn URL");
        }

        String recommendation = missing.isEmpty()
                ? "Contact section is complete."
                : "Add missing contact info: " + String.join(", ", missing) + ".";

        return new CategoryScore("CONTACT_COMPLETENESS", pts, MAX_SCORE, recommendation);
    }
}
