package com.backend.bilanko.DTO.search;

public record GlobalSearchResultDTO(
        String type,
        long id,
        Object data
) {
}
