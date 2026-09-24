package com.backend.bilanko.DTO.person.preference;

public record AppearancePreferencesDTO(
        String theme,
        String language,
        String dateFormat,
        String currency,
        boolean compactMode
) {}