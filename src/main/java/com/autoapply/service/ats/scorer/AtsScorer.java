package com.autoapply.service.ats.scorer;

import com.autoapply.service.jd.JdKeywords;
import com.autoapply.service.resume.ResumeJson;

public interface AtsScorer {
    CategoryScore score(ResumeJson resume, JdKeywords jdKeywords, int yearsExperience);
}
