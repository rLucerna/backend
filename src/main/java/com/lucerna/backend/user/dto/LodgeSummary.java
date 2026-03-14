package com.lucerna.backend.user.dto;

import com.lucerna.backend.user.entity.Lodge;

/**
 * Lodge 요약 정보 DTO.
 */
public record LodgeSummary(
        Long lodgeId,
        String nickname,
        String profileImageUrl,
        String introduction
) {
    public static LodgeSummary from(Lodge lodge) {
        return new LodgeSummary(
                lodge.getId(),
                lodge.getNickname(),
                lodge.getProfileImageUrl(),
                lodge.getIntroduction()
        );
    }
}
