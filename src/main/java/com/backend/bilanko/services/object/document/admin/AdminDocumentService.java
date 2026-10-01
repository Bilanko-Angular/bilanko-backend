package com.backend.bilanko.services.object.document.admin;

import com.backend.bilanko.DTO.object.document.admin.AdminDocumentCreateRequest;
import com.backend.bilanko.DTO.object.document.admin.AdminDocumentResponseDTO;
import com.backend.bilanko.DTO.object.document.admin.AdminDocumentSummaryDTO;
import com.backend.bilanko.DTO.object.document.admin.AdminDocumentUpdateRequest;
import org.springframework.data.domain.Page;

public interface AdminDocumentService {

    AdminDocumentSummaryDTO getSummary();

    Page<AdminDocumentResponseDTO> getPagedDocuments(int page, int size);

    Page<AdminDocumentResponseDTO> searchDocuments(
             String keyword, String type, int page, int size);

    AdminDocumentResponseDTO getById(Long documentId);

    AdminDocumentResponseDTO createDocument(AdminDocumentCreateRequest request);

    AdminDocumentResponseDTO updateDocument(Long documentId, AdminDocumentUpdateRequest request);

    void deleteDocument(Long documentId);
}
