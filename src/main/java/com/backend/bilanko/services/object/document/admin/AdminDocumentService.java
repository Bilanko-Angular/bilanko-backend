package com.backend.bilanko.services.object.document.admin;

import com.backend.bilanko.DTO.object.document.admin.AdminDocumentCreateRequest;
import com.backend.bilanko.DTO.object.document.admin.AdminDocumentResponseDTO;
import com.backend.bilanko.DTO.object.document.admin.AdminDocumentSummaryDTO;
import com.backend.bilanko.DTO.object.document.admin.AdminDocumentUpdateRequest;
import org.springframework.data.domain.Page;

public interface AdminDocumentService {

    AdminDocumentSummaryDTO getSummary(String adminEmail);

    Page<AdminDocumentResponseDTO> getPagedDocuments(String adminEmail, int page, int size);

    Page<AdminDocumentResponseDTO> searchDocuments(
            String adminEmail, String keyword, String type, int page, int size);

    AdminDocumentResponseDTO getById(String adminEmail, Long documentId);

    AdminDocumentResponseDTO createDocument(String adminEmail, AdminDocumentCreateRequest request);

    AdminDocumentResponseDTO updateDocument(String adminEmail, Long documentId, AdminDocumentUpdateRequest request);

    void deleteDocument(String adminEmail, Long documentId);
}
