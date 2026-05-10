package com.autoapply.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final AppProperties appProperties;

    @Bean
    public WebClient supabaseStorageClient() {
        AppProperties.Supabase supabase = appProperties.getSupabase();
        return WebClient.builder()
                .baseUrl(supabase.getUrl())
                .defaultHeader("Authorization", "Bearer " + supabase.getServiceRoleKey())
                .defaultHeader("apikey", supabase.getAnonKey())
                .build();
    }
}
