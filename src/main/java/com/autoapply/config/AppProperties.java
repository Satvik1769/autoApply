package com.autoapply.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private final Supabase supabase = new Supabase();
    private final Ats ats = new Ats();
    private final Jwt jwt = new Jwt();
    private final Ai ai = new Ai();

    @Getter
    @Setter
    public static class Ai {
        private String provider = "openai";
        private String geminiApiKey = "";
        private String geminiModel = "gemini-2.0-flash";
    }

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long expirationMs = 86400000L;
    }

    @Getter
    @Setter
    public static class Supabase {
        private String url;
        private String serviceRoleKey;
        private String anonKey;
        private final Storage storage = new Storage();

        @Getter
        @Setter
        public static class Storage {
            private String bucket = "resumes";
        }
    }

    @Getter
    @Setter
    public static class Ats {
        private final TargetWords targetWords = new TargetWords();
        private int yearsExperienceThreshold = 5;

        @Getter
        @Setter
        public static class TargetWords {
            private int juniorMin = 400;
            private int juniorMax = 600;
            private int seniorMin = 600;
            private int seniorMax = 1000;
        }
    }
}
