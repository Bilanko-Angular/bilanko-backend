package com.backend.bilanko.controller.ai;

import com.backend.bilanko.DTO.ai.ProductDescriptionClean;
import com.backend.bilanko.DTO.ai.ProductRecognitionResponseDTO;
import com.backend.bilanko.mapper.AIProductMapper;
import com.backend.bilanko.services.ai.AiProductRecognitionService;
import com.backend.bilanko.utils.routes.AiApiRoutes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(AiApiRoutes.BASE)
@RequiredArgsConstructor
public class AiProductController {

    private final AiProductRecognitionService aiProductRecognitionService;
    private final AIProductMapper aiProductMapper;

    @PostMapping(value = AiApiRoutes.RECOGNIZE_PRODUCT, consumes = "multipart/form-data")
    public ResponseEntity<ProductDescriptionClean> recognizeProduct(
            @RequestParam("image") MultipartFile image) {
        ProductRecognitionResponseDTO aiReconise = aiProductRecognitionService.recognize(image);
        return ResponseEntity.ok(aiProductMapper.cleanAiImageJsonReponse(aiReconise));
    }
}
