package com.lucerna.backend.auth.controller;

import com.lucerna.backend.auth.dto.RefreshRequest;
import com.lucerna.backend.auth.dto.TokenRequest;
import com.lucerna.backend.auth.dto.TokenResponse;
import com.lucerna.backend.auth.service.PkceTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Postman OAuth2 내장 클라이언트 테스트 전용 엔드포인트.
 * dev 프로필에서만 활성화 — 프로덕션 배포 시 자동 비활성화.
 *
 * OAuth2 표준(RFC 6749): 토큰 엔드포인트는 access_token을 루트에 반환해야 함.
 * CommonResponse로 감싸면 Postman이 access_token을 추출하지 못하므로 직접 반환.
 */
@Slf4j
@Profile("dev")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthDevController {

    private final PkceTokenService pkceTokenService;

    @PostMapping("/token-dev")
    public ResponseEntity<TokenResponse> tokenDev(HttpServletRequest request) {
        String code        = request.getParameter("code");
        String verifier    = request.getParameter("code_verifier");
        String redirectUri = request.getParameter("redirect_uri");
        log.debug("[DEV] token-dev 수신 → code={}, verifier={}, redirect_uri={}", code, verifier, redirectUri);

        TokenRequest tokenRequest = new TokenRequest(code, verifier, redirectUri);
        TokenResponse tokenResponse = pkceTokenService.exchangeAuthCode(tokenRequest);
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * Postman OAuth2 자동 갱신은 RFC 6749에 따라 form-encoded를 전송하므로
     * /auth/refresh(@RequestBody JSON)로는 파싱 불가 → HttpServletRequest로 직접 수신.
     */
    @PostMapping("/refresh-dev")
    public ResponseEntity<TokenResponse> refreshDev(HttpServletRequest request) {
        String refreshToken = request.getParameter("refresh_token");
        log.debug("[DEV] refresh-dev 수신 → refresh_token={}", refreshToken != null ? "present" : "null");

        TokenResponse tokenResponse = pkceTokenService.refreshToken(new RefreshRequest(refreshToken));
        return ResponseEntity.ok(tokenResponse);
    }
}
