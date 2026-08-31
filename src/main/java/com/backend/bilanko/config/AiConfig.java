package com.backend.bilanko.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.ChatClient;
@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build(); // transforme le builder en client utilisable
    }
}
