package com.backend.bilanko.controller.object.document.admin;

import com.backend.bilanko.DTO.object.document.admin.AdminDocumentCreateRequest;
import com.backend.bilanko.DTO.object.document.admin.AdminDocumentResponseDTO;
import com.backend.bilanko.DTO.object.document.admin.AdminDocumentSummaryDTO;
import com.backend.bilanko.DTO.object.document.admin.AdminDocumentUpdateRequest;
import com.backend.bilanko.services.object.document.admin.AdminDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
public class AdminDocumentController {

    private final AdminDocumentService adminDocumentService;

    @GetMapping("/summary")
    public ResponseEntity<AdminDocumentSummaryDTO> getSummary() {
        return ResponseEntity.ok(adminDocumentService.getSummary());
    }

    @GetMapping
    public ResponseEntity<Page<AdminDocumentResponseDTO>> getPagedDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminDocumentService.getPagedDocuments(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AdminDocumentResponseDTO>> searchDocuments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminDocumentService.searchDocuments(
                keyword, type, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminDocumentResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(adminDocumentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AdminDocumentResponseDTO> createDocument(
            @Valid @RequestBody AdminDocumentCreateRequest request) {
        return ResponseEntity.ok(adminDocumentService.createDocument(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminDocumentResponseDTO> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody AdminDocumentUpdateRequest request) {
        return ResponseEntity.ok(adminDocumentService.updateDocument(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        adminDocumentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

}
