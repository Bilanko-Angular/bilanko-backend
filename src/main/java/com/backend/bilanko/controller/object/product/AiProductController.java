package com.backend.bilanko.controller.object.product;

import com.backend.bilanko.DTO.object.product.ProductRecognitionResponseDTO;
import com.backend.bilanko.services.object.product.AiProductRecognitionService;
import com.backend.bilanko.utils.constant.AiApiRoutes;
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

    @PostMapping(value = AiApiRoutes.RECOGNIZE_PRODUCT, consumes = "multipart/form-data")
    public ResponseEntity<ProductRecognitionResponseDTO> recognizeProduct(
            @RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(aiProductRecognitionService.recognize(image));
    }
}
