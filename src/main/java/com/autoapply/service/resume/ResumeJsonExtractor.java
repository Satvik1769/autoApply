package com.autoapply.service.resume;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ResumeJsonExtractor {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9+_.\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(\\+?\\d[\\d\\s()\\-.]{7,}\\d)");
    private static final Pattern LINKEDIN_PATTERN =
            Pattern.compile("(?i)linkedin\\.com/in/[a-zA-Z0-9_\\-]+");
    private static final Pattern DATE_RANGE_PATTERN =
            Pattern.compile("(?i)(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|\\d{4})[\\w\\s,]*" +
                    "([-–]|to)[\\w\\s,]*(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|\\d{4}|Present|Current|Now)");

    // Section heading detection patterns
    private static final Map<String, Pattern> SECTION_PATTERNS = new LinkedHashMap<>();

    static {
        SECTION_PATTERNS.put("summary",        Pattern.compile("(?i)^\\s*(SUMMARY|PROFILE|OBJECTIVE|ABOUT ME|PROFESSIONAL SUMMARY)\\s*$"));
        SECTION_PATTERNS.put("experience",     Pattern.compile("(?i)^\\s*(EXPERIENCE|WORK EXPERIENCE|WORK HISTORY|EMPLOYMENT|PROFESSIONAL EXPERIENCE|CAREER)\\s*$"));
        SECTION_PATTERNS.put("education",      Pattern.compile("(?i)^\\s*(EDUCATION|ACADEMIC|QUALIFICATION|ACADEMICS)\\s*$"));
        SECTION_PATTERNS.put("skills",         Pattern.compile("(?i)^\\s*(SKILLS|TECHNICAL SKILLS|CORE SKILLS|KEY SKILLS|TECHNOLOGIES|TECH STACK)\\s*$"));
        SECTION_PATTERNS.put("certifications", Pattern.compile("(?i)^\\s*(CERTIFICATIONS?|CERTIFICATES?|LICENSES?|CREDENTIALS?)\\s*$"));
        SECTION_PATTERNS.put("projects",       Pattern.compile("(?i)^\\s*(PROJECTS?|PORTFOLIO|PERSONAL PROJECTS?)\\s*$"));
    }

    public ResumeJson extract(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ResumeJson();
        }

        List<String> lines = Arrays.asList(rawText.split("\n"));
        ResumeJson.Contact contact = extractContact(lines);
        Map<String, List<String>> sections = splitIntoSections(lines);

        return ResumeJson.builder()
                .contact(contact)
                .sections(ResumeJson.Sections.builder()
                        .summary(extractSummary(sections.get("summary")))
                        .experience(extractExperience(sections.get("experience")))
                        .education(extractEducation(sections.get("education")))
                        .skills(extractSkills(sections.get("skills")))
                        .certifications(extractCertifications(sections.get("certifications")))
                        .projects(extractProjects(sections.get("projects")))
                        .build())
                .build();
    }

    private ResumeJson.Contact extractContact(List<String> lines) {
        ResumeJson.Contact contact = new ResumeJson.Contact();
        int scanLimit = Math.min(15, lines.size());

        for (int i = 0; i < scanLimit; i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            // First non-empty line is likely the name (before we find email/phone)
            if (contact.getName() == null && !EMAIL_PATTERN.matcher(line).find()
                    && !PHONE_PATTERN.matcher(line).find() && line.length() < 80) {
                contact.setName(line);
            }

            Matcher emailMatcher = EMAIL_PATTERN.matcher(line);
            if (contact.getEmail() == null && emailMatcher.find()) {
                contact.setEmail(emailMatcher.group());
            }

            Matcher phoneMatcher = PHONE_PATTERN.matcher(line);
            if (contact.getPhone() == null && phoneMatcher.find()) {
                String phone = phoneMatcher.group().trim();
                if (phone.replaceAll("[^\\d]", "").length() >= 7) {
                    contact.setPhone(phone);
                }
            }

            Matcher linkedinMatcher = LINKEDIN_PATTERN.matcher(line);
            if (contact.getLinkedin() == null && linkedinMatcher.find()) {
                contact.setLinkedin(linkedinMatcher.group());
            }
        }
        return contact;
    }

    private Map<String, List<String>> splitIntoSections(List<String> lines) {
        Map<String, List<String>> sections = new LinkedHashMap<>();
        String currentSection = null;
        List<String> currentLines = null;

        for (String line : lines) {
            String detected = detectSection(line);
            if (detected != null) {
                if (currentSection != null) {
                    sections.put(currentSection, currentLines);
                }
                currentSection = detected;
                currentLines = new ArrayList<>();
            } else if (currentSection != null) {
                currentLines.add(line);
            }
        }
        if (currentSection != null && currentLines != null) {
            sections.put(currentSection, currentLines);
        }
        return sections;
    }

    private String detectSection(String line) {
        for (Map.Entry<String, Pattern> entry : SECTION_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(line).matches()) {
                return entry.getKey();
            }
        }
        return null;
    }

    private String extractSummary(List<String> lines) {
        if (lines == null || lines.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) sb.append(trimmed).append(" ");
        }
        String result = sb.toString().trim();
        return result.isEmpty() ? null : result;
    }

    private List<ResumeJson.Experience> extractExperience(List<String> lines) {
        if (lines == null || lines.isEmpty()) return List.of();

        List<ResumeJson.Experience> experiences = new ArrayList<>();
        ResumeJson.Experience current = null;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            if (DATE_RANGE_PATTERN.matcher(trimmed).find()) {
                if (current != null) experiences.add(current);
                current = ResumeJson.Experience.builder().dates(extractDateRange(trimmed)).build();
                // Try to get title/company from same line
                String remainder = DATE_RANGE_PATTERN.matcher(trimmed).replaceAll("").trim().replaceAll("[|@,–-]+$", "").trim();
                if (!remainder.isEmpty()) {
                    String[] parts = remainder.split("\\s+(?:at|@|,)\\s+|\\s{2,}");
                    if (parts.length >= 2) {
                        current.setTitle(parts[0].trim());
                        current.setCompany(parts[1].trim());
                    } else {
                        current.setTitle(parts[0].trim());
                    }
                }
            } else if (current != null) {
                String bullet = trimmed.replaceFirst("^[•\\-*▪►→]\\s*", "");
                if (!bullet.isEmpty()) {
                    current.getBullets().add(bullet);
                }
            }
        }
        if (current != null) experiences.add(current);
        return experiences;
    }

    private String extractDateRange(String line) {
        Matcher m = DATE_RANGE_PATTERN.matcher(line);
        return m.find() ? m.group().trim() : line.trim();
    }

    private List<ResumeJson.Education> extractEducation(List<String> lines) {
        if (lines == null || lines.isEmpty()) return List.of();

        List<ResumeJson.Education> educations = new ArrayList<>();
        ResumeJson.Education current = null;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            if (DATE_RANGE_PATTERN.matcher(trimmed).find() || trimmed.matches("(?i).*\\b(bachelor|master|b\\.?tech|m\\.?tech|b\\.?sc|m\\.?sc|mba|phd|diploma)\\b.*")) {
                if (current != null) educations.add(current);
                current = ResumeJson.Education.builder()
                        .institution(trimmed)
                        .dates(extractDateRange(trimmed))
                        .build();
            } else if (current != null && current.getDegree() == null) {
                current.setDegree(trimmed);
            }
        }
        if (current != null) educations.add(current);
        return educations;
    }

    private List<String> extractSkills(List<String> lines) {
        if (lines == null || lines.isEmpty()) return List.of();

        List<String> skills = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim().replaceFirst("^[•\\-*]\\s*", "");
            if (trimmed.isEmpty()) continue;
            // Split comma/pipe/semicolon separated skills
            String[] parts = trimmed.split("[,|;]+");
            for (String part : parts) {
                String skill = part.trim();
                if (!skill.isEmpty() && skill.length() <= 60) {
                    skills.add(skill);
                }
            }
        }
        return skills;
    }

    private List<String> extractCertifications(List<String> lines) {
        if (lines == null || lines.isEmpty()) return List.of();
        List<String> certs = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim().replaceFirst("^[•\\-*]\\s*", "");
            if (!trimmed.isEmpty()) certs.add(trimmed);
        }
        return certs;
    }

    private List<ResumeJson.Project> extractProjects(List<String> lines) {
        if (lines == null || lines.isEmpty()) return List.of();

        List<ResumeJson.Project> projects = new ArrayList<>();
        ResumeJson.Project current = null;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            boolean isBullet = trimmed.startsWith("•") || trimmed.startsWith("-")
                    || trimmed.startsWith("*") || trimmed.startsWith("▪");

            if (!isBullet && current == null) {
                current = ResumeJson.Project.builder().name(trimmed).build();
            } else if (!isBullet && current != null && current.getDescription() == null) {
                current.setDescription(trimmed);
            } else if (isBullet && current != null) {
                current.getBullets().add(trimmed.replaceFirst("^[•\\-*▪]\\s*", ""));
            } else if (!isBullet) {
                if (current != null) projects.add(current);
                current = ResumeJson.Project.builder().name(trimmed).build();
            }
        }
        if (current != null) projects.add(current);
        return projects;
    }
}
