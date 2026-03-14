package com.lucerna.backend.auth.controller;

import com.lucerna.backend.auth.dto.LogoutRequest;
import com.lucerna.backend.auth.dto.RefreshRequest;
import com.lucerna.backend.auth.dto.TokenRequest;
import com.lucerna.backend.auth.dto.TokenResponse;
import com.lucerna.backend.auth.service.PkceTokenService;
import com.lucerna.backend.common.response.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
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

    // [DEV-ONLY] Postman OAuth2 내장 클라이언트 테스트용
    // OAuth2 표준(RFC 6749): 토큰 엔드포인트는 access_token을 루트에 반환해야 함
    // CommonResponse로 감싸면 Postman이 access_token을 추출하지 못하므로 직접 반환
    // 실서비스 전환 시 이 메서드 삭제할 것
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

    // [DEV-ONLY] Postman OAuth2 내장 클라이언트 토큰 갱신 테스트용
    // Postman의 OAuth2 자동 갱신은 RFC 6749에 따라 form-encoded를 전송하므로
    // /auth/refresh(@RequestBody JSON)로는 파싱 불가 → HttpServletRequest로 직접 수신
    // 실서비스 전환 시 이 메서드 삭제할 것
    @PostMapping("/refresh-dev")
    public ResponseEntity<TokenResponse> refreshDev(HttpServletRequest request) {
        String refreshToken = request.getParameter("refresh_token");
        log.debug("[DEV] refresh-dev 수신 → refresh_token={}", refreshToken != null ? "present" : "null");

        TokenResponse tokenResponse = pkceTokenService.refreshToken(new RefreshRequest(refreshToken));
        return ResponseEntity.ok(tokenResponse);
    }

}

