package com.backend.bilanko.DTO.person;

public record AppearancePreferencesDTO(
        String theme,
        String language,
        String dateFormat,
        String currency,
        boolean compactMode
) {}