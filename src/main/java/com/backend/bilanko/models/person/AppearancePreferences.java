package com.backend.bilanko.models.person;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class AppearancePreferences {
    @Column(name = "appearance_theme")
    private String theme = "light"; // "light" | "dark"

    @Column(name = "appearance_language")
    private String language = "en";

    @Column(name = "appearance_date_format")
    private String dateFormat = "DD/MM/YYYY";

    @Column(name = "appearance_currency")
    private String currency = "XAF";

    @Column(name = "appearance_compact_mode")
    private boolean compactMode = true;
}