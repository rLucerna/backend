package com.lucerna.backend.user.dto;

import com.lucerna.backend.user.entity.User;
import com.lucerna.backend.user.entity.UserStatus;

import java.time.LocalDateTime;

/**
 * GET /api/v1/users/me 응답 DTO.
 */
public record UserMeResponse(
        Long userId,
        String email,
        UserStatus status,
        boolean isNewUser,
        LodgeSummary lodge,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
    public static UserMeResponse of(User user, boolean isNewUser, LodgeSummary lodge) {
        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getStatus(),
                isNewUser,
                lodge,
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}
