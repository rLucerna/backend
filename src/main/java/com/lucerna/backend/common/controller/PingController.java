package com.lucerna.backend.common.controller;

import com.lucerna.backend.common.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * [DEV-ONLY] JWT 토큰 검증 테스트용 엔드포인트.
 * 실서비스 도메인 추가 후 삭제할 것.
 *
 * GET /api/v1/ping
 *   - Authorization: Bearer <token> 필수
 *   - 유효한 토큰: 200 + userId 반환
 *   - 만료된 토큰: 401 AUTH_005
 *   - 유효하지 않은 토큰: 401 AUTH_006
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class PingController {

    @GetMapping("/ping")
    public ResponseEntity<CommonResponse<Map<String, Object>>> ping(Authentication authentication) {
        log.debug("토큰 검증 성공 → userId={}", authentication.getName());
        return ResponseEntity.ok(CommonResponse.success(Map.of(
                "userId", authentication.getName(),
                "message", "토큰 검증 성공"
        )));
    }
}