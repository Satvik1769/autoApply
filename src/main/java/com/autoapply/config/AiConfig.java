package com.autoapply.config;

import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    /** Default provider: OpenAI */
    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai", matchIfMissing = true)
    public ChatClient openAiChatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    /** Anthropic (Claude) */
    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "anthropic")
    public ChatClient anthropicChatClient(AnthropicChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    /**
     * Gemini via Google's OpenAI-compatible endpoint.
     * Requires: app.ai.gemini-api-key (GEMINI_API_KEY env var)
     * Docs: https://ai.google.dev/gemini-api/docs/openai
     */
    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
    public ChatClient geminiChatClient(AppProperties appProperties) {
        String apiKey = appProperties.getAi().getGeminiApiKey();
        String model  = appProperties.getAi().getGeminiModel();

        var openAiClient = OpenAIOkHttpClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/openai/")
                .apiKey(apiKey)
                .build();

        OpenAiChatModel geminiModel = OpenAiChatModel.builder()
                .openAiClient(openAiClient)
                .options(OpenAiChatOptions.builder().model(model).build())
                .build();

        return ChatClient.builder(geminiModel).build();
    }
}
