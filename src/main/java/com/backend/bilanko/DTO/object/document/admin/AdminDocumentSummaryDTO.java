package com.backend.bilanko.DTO.object.document.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDocumentSummaryDTO {
    private long totalCount;
    private long pretCount;
    private long fiscalCount;
    private long usersWithDocumentsCount;
}
