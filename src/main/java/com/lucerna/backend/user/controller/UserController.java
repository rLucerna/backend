package com.lucerna.backend.user.controller;

import com.lucerna.backend.common.response.CommonResponse;
import com.lucerna.backend.user.dto.UserMeResponse;
import com.lucerna.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 정보 API.
 * GET /api/v1/users/me — 현재 인증된 사용자 조회 (없으면 JIT 생성)
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<CommonResponse<UserMeResponse>> me(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        UserMeResponse response = userService.getOrCreateUser(keycloakId, email);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
