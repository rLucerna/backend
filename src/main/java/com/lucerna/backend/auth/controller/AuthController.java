package com.lucerna.backend.auth.controller;

import com.lucerna.backend.auth.dto.LogoutRequest;
import com.lucerna.backend.auth.dto.RefreshRequest;
import com.lucerna.backend.auth.dto.TokenRequest;
import com.lucerna.backend.auth.dto.TokenResponse;
import com.lucerna.backend.auth.service.PkceTokenService;
import com.lucerna.backend.common.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final PkceTokenService pkceTokenService;

    /**
     * PKCE Authorization Code → Keycloak Token 교환
     * POST /auth/token
     */
    @PostMapping("/token")
    public ResponseEntity<CommonResponse<TokenResponse>> token(@Valid @RequestBody TokenRequest request) {
        TokenResponse tokenResponse = pkceTokenService.exchangeAuthCode(request);
        return ResponseEntity.ok(CommonResponse.success(tokenResponse));
    }

    /**
     * Refresh Token → 신규 Token 발급
     * POST /auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenResponse tokenResponse = pkceTokenService.refreshToken(request);
        return ResponseEntity.ok(CommonResponse.success(tokenResponse));
    }

    /**
     * 로그아웃 — Keycloak 세션 종료 + Refresh Token 무효화
     * POST /auth/logout
     *
     * TODO: 추후 Spring Security OAuth2 Client로 전환 시
     *   OidcClientInitiatedLogoutSuccessHandler를 이용해 end_session_endpoint 자동 처리 가능.
     *   http.logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler()))
     */
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        pkceTokenService.logout(request.idToken(), request.refreshToken());
        return ResponseEntity.ok(CommonResponse.success());
    }

}

