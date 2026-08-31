package com.backend.bilanko.services.object.product;

import com.backend.bilanko.DTO.object.product.ProductRecognitionResponseDTO;
import com.backend.bilanko.models.object.product.Category;
import com.backend.bilanko.repository.product.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiProductRecognitionService {

    private final ChatClient chatClient;
    private final CategoryRepository categoryRepository;

    public ProductRecognitionResponseDTO recognize(MultipartFile image) {
        List<String> existingCategories = categoryRepository.findAll()
                .stream()
                .map(Category::getName)
                .toList();

        Media imageMedia = new Media(
                MimeTypeUtils.parseMimeType(image.getContentType()),
                toResource(image)
        );

        return chatClient.prompt()
                .user(userSpec -> userSpec
                        .text(buildPrompt(existingCategories))
                        .media(imageMedia))
                .call()
                .entity(ProductRecognitionResponseDTO.class);
    }

    private String buildPrompt(List<String> existingCategories) {
        return """
                Tu es un assistant qui identifie un produit à partir d'une photo pour un commerce camerounais.
                Catégories déjà existantes dans le système : %s

                Analyse l'image et propose un nom de produit, les catégories correspondantes,
                d'éventuelles nouvelles catégories, et un prix estimé en FCFA si possible.
                Ne remplis un champ que si tu es raisonnablement confiant.
                """.formatted(existingCategories);
    }

    private ByteArrayResource toResource(MultipartFile image) {
        try {
            return new ByteArrayResource(image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Impossible de lire l'image envoyée", e);
        }
    }
}
