package com.backend.bilanko.controller.ai;


import com.backend.bilanko.DTO.ai.ProductDescriptionClean;
import com.backend.bilanko.DTO.ai.ProductRecognitionResponseDTO;
import com.backend.bilanko.mapper.AIProductMapper;
import com.backend.bilanko.services.ai.GroqTranscriptionService;
import com.backend.bilanko.utils.routes.AiApiRoutes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(AiApiRoutes.BASE)
@RequiredArgsConstructor
public class AiVoiceController {
    private final AIProductMapper aiProductMapper;
    private final GroqTranscriptionService groqTranscriptionService;

    @PostMapping(value = AiApiRoutes.RECOGNIZE_PRODUCT_VOICE, consumes = "multipart/form-data")
    public ResponseEntity<ProductDescriptionClean> recognizeProductFromVoice(
            @RequestParam("audio") MultipartFile audio) {
        return ResponseEntity.ok(aiProductMapper.cleanAiVoiceJsonReponse(
                groqTranscriptionService.recognizeFromAudio(audio))
        );
    }
}