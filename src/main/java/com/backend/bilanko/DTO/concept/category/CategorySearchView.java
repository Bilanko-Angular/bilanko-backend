package com.backend.bilanko.DTO.concept.category;

public interface CategorySearchView {
    Long getId();
    String getName();
    String getType();
    Long getUsageCount();
    Long getUserCount();
}