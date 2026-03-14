package com.lucerna.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
        @NotBlank(message = "code는 필수입니다") String code,
        @NotBlank(message = "codeVerifier는 필수입니다") String codeVerifier,
        @NotBlank(message = "redirectUri는 필수입니다") String redirectUri
) {
}
