package com.lucerna.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "idToken은 필수입니다") String idToken,
        @NotBlank(message = "refreshToken은 필수입니다") String refreshToken
) {
}
