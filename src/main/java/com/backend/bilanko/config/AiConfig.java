package com.backend.bilanko.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient openRouterChatClient(
            @Value("${openrouter.api-key}") String apiKey,
            @Value("${openrouter.base-url}") String baseUrl,
            @Value("${openrouter.model}") String model) {

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .apiKey(apiKey)
                        .baseUrl(baseUrl)
                        .model(model)
                        .maxTokens(1000)
                        .extraBody(Map.of(
                                "models", List.of(
                                        "qwen/qwen2.5-vl-72b-instruct:free", // fallback gratuit si le payant échoue
                                        "meta-llama/llama-3.2-90b-vision-instruct:free"
                                )
                        ))
                        .build())
                .build();

        return ChatClient.create(chatModel);
    }

    @Bean
    public ChatClient groqChatClient(
            @Value("${groq.api-key}") String apiKey,
            @Value("${groq.base-url}") String baseUrl,
            @Value("${groq.model}") String model) {

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .apiKey(apiKey)
                        .baseUrl(baseUrl)
                        .model(model)
                        .build())
                .build();

        return ChatClient.create(chatModel);
    }
}