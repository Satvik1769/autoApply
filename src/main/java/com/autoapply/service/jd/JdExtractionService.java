package com.autoapply.service.jd;

import com.autoapply.entity.JdExtractionCache;
import com.autoapply.exception.JdExtractionException;
import com.autoapply.repository.JdExtractionCacheRepository;
import com.autoapply.util.HashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class JdExtractionService {

    private final JdExtractionCacheRepository cacheRepository;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public record ExtractionResult(JdKeywords keywords, boolean cached) {}

    @Transactional
    public ExtractionResult extract(String jdText) {
        if (jdText == null || jdText.isBlank()) {
            throw new JdExtractionException("Job description text cannot be empty");
        }

        String hash = HashUtil.sha256(jdText);

        return cacheRepository.findByJdHash(hash)
                .map(cached -> {
                    cached.setLastAccessedAt(OffsetDateTime.now());
                    cacheRepository.save(cached);
                    return new ExtractionResult(deserialize(cached.getExtractedJson()), true);
                })
                .orElseGet(() -> {
                    JdKeywords keywords = extractFromAi(jdText);
                    cacheRepository.save(JdExtractionCache.builder()
                            .jdHash(hash)
                            .extractedJson(serialize(keywords))
                            .build());
                    return new ExtractionResult(keywords, false);
                });
    }

    private JdKeywords extractFromAi(String jdText) {
        String prompt = """
            Analyze this job description and extract structured keywords.
            Return ONLY a valid JSON object — no explanation, no markdown, no code fences.

            Use exactly these fields:
            - "roles": job titles explicitly mentioned (e.g. "Senior Software Engineer")
            - "technicalSkills": programming languages, frameworks, tools, platforms, databases, cloud services, protocols, methodologies
            - "softSkills": interpersonal and workplace skills (e.g. "communication", "teamwork", "Agile", "Scrum")
            - "mustHaveKeywords": required qualifications that don't fit the above categories
            - "niceToHaveKeywords": optional or preferred qualifications mentioned as nice-to-have or a plus
            - "resumeKeywords": action verbs and impact phrases valuable on a resume (e.g. "led", "optimized")
            - "experienceRequired": years of experience as a string (e.g. "3–6 years"), or null if not mentioned

            All values must be arrays of strings, except experienceRequired which is a string or null.

            Job Description:
            ---
            %s
            ---
            """.formatted(jdText);

        String response;
        try {
            response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception ex) {
            throw new JdExtractionException("AI API call failed: " + ex.getMessage(), ex);
        }

        return parseResponse(response);
    }

    private JdKeywords parseResponse(String response) {
        String cleaned = response.strip()
                .replaceAll("(?s)^```[a-z]*\\s*", "")
                .replaceAll("```\\s*$", "")
                .strip();
        try {
            return objectMapper.readValue(cleaned, JdKeywords.class);
        } catch (Exception ex) {
            log.warn("Could not parse AI response as JdKeywords JSON: {}", cleaned);
            throw new JdExtractionException("Failed to parse AI response as structured keywords", ex);
        }
    }

    private String serialize(JdKeywords keywords) {
        try {
            return objectMapper.writeValueAsString(keywords);
        } catch (Exception ex) {
            throw new JdExtractionException("Failed to serialize JdKeywords", ex);
        }
    }

    private JdKeywords deserialize(String json) {
        try {
            return objectMapper.readValue(json, JdKeywords.class);
        } catch (Exception ex) {
            throw new JdExtractionException("Failed to deserialize cached JdKeywords", ex);
        }
    }
}
