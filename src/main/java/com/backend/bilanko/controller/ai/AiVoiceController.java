package com.backend.bilanko.controller.ai;


import com.backend.bilanko.DTO.ai.ProductRecognitionResponseDTO;
import com.backend.bilanko.services.ai.GroqTranscriptionService;
import com.backend.bilanko.utils.constant.AiApiRoutes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(AiApiRoutes.BASE)
@RequiredArgsConstructor
public class AiVoiceController {

    private final GroqTranscriptionService groqTranscriptionService;

    @PostMapping(value = AiApiRoutes.RECOGNIZE_PRODUCT_VOICE, consumes = "multipart/form-data")
    public ResponseEntity<ProductRecognitionResponseDTO> recognizeProductFromVoice(
            @RequestParam("audio") MultipartFile audio) {
        return ResponseEntity.ok(groqTranscriptionService.recognize(audio));
    }
}