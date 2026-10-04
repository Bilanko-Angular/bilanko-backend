package com.backend.bilanko.config.app;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        boolean hasBearer = request.getHeader("Authorization") != null
                && request.getHeader("Authorization").startsWith("Bearer ");

        log.warn("401 Unauthorized on {} {} | bearerPresent={} | reason={}",
                request.getMethod(),
                request.getRequestURI(),
                hasBearer,
                authException.getMessage());

        writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                hasBearer
                        ? "Token invalide ou expiré"
                        : "Authentification requise",
                request.getRequestURI());
    }

    private void writeJson(HttpServletResponse response, int status, String error, String message, String path)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
