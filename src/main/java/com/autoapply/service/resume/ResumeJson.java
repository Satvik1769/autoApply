package com.autoapply.service.resume;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResumeJson {

    @Builder.Default
    private Contact contact = new Contact();

    @Builder.Default
    private Sections sections = new Sections();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Contact {
        private String name;
        private String email;
        private String phone;
        private String linkedin;
        private String location;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Sections {
        private String summary;

        @Builder.Default
        private List<Experience> experience = new ArrayList<>();

        @Builder.Default
        private List<Education> education = new ArrayList<>();

        @Builder.Default
        private List<String> skills = new ArrayList<>();

        @Builder.Default
        private List<String> certifications = new ArrayList<>();

        @Builder.Default
        private List<Project> projects = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Experience {
        private String company;
        private String title;
        private String dates;

        @Builder.Default
        private List<String> bullets = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Education {
        private String institution;
        private String degree;
        private String field;
        private String dates;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Project {
        private String name;
        private String description;

        @Builder.Default
        private List<String> bullets = new ArrayList<>();
    }
}
