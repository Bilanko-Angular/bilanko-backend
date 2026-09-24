package com.backend.bilanko.DTO.person;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppUpdateBroadcastRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 1000) String message
) {
}
