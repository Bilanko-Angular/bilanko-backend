package com.backend.bilanko.DTO.auth;

public record ForgotPasswordResponse(
        String message,
        boolean success,
        String token
) {
    /** Réponse simple sans token (étapes 1 et 2 de validation) */
    public ForgotPasswordResponse(String message, boolean success) {
        this(message, success, null);
    }
}
