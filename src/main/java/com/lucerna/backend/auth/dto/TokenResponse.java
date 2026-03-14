package com.lucerna.backend.auth.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String idToken,
        String tokenType,
        Long expiresIn
) {
}
