package com.backend.bilanko.config.app;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, @NonNull HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = auth != null ? auth.getName() : "anonymous";
        String authorities = auth != null
                ? auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", "))
                : "none";

        log.warn("403 Forbidden on {} {} | user={} | authorities={} | reason={}",
                request.getMethod(),
                request.getRequestURI(),
                user,
                authorities,
                accessDeniedException.getMessage());

        writeJson(response,
                request.getRequestURI());
    }

    private void writeJson(HttpServletResponse response, String path)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpServletResponse.SC_FORBIDDEN);
        body.put("error", "Forbidden");
        body.put("message", "Accès refusé : vous n'avez pas les droits nécessaires");
        body.put("path", path);

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
