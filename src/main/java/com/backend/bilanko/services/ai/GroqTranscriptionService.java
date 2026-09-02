package com.backend.bilanko.services.ai;

import com.backend.bilanko.DTO.ai.ProductRecognitionResponseDTO;
import com.backend.bilanko.models.object.product.Category;
import com.backend.bilanko.repository.product.CategoryRepository;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroqTranscriptionService {

    private final ChatClient groqChatClient;
    private final CategoryRepository categoryRepository;
    private final RestClient restClient = RestClient.create();

    @Value("${groq.api-key}")
    private String apiKey;

    public ProductRecognitionResponseDTO recognizeFromAudio(MultipartFile audio) {
        String transcript = transcribe(audio);
        return extractFromTranscript(transcript);
    }

    private String transcribe(MultipartFile audio) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", audio.getResource());
        body.add("model", "whisper-large-v3-turbo");
        body.add("language", "fr");
        body.add("response_format", "json");

        JsonNode response = restClient.post()
                .uri("https://api.groq.com/openai/v1/audio/transcriptions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        assert response != null;
        return response.path("text").asText();
    }

    private ProductRecognitionResponseDTO extractFromTranscript(String transcript) {
        List<String> existingCategories = categoryRepository.findAll()
                .stream()
                .map(Category::getName)
                .toList();

        return groqChatClient.prompt()
                .user(buildPrompt(existingCategories, transcript))
                .call()
                .entity(ProductRecognitionResponseDTO.class);
    }

    private String buildPrompt(List<String> existingCategories, String transcript) {
        return """
                Tu es un assistant qui extrait les informations d'un produit à partir de la description
                orale d'un marchand camerounais.
                Catégories déjà existantes dans le système : %s

                Transcription : "%s"

                Extrais : nom, catégories correspondantes, nouvelles catégories éventuelles,
                prix de vente, prix d'achat, quantité, référence.
                Laisse null ce qui n'est pas mentionné clairement, n'invente rien.
                """.formatted(existingCategories, transcript);
    }
}