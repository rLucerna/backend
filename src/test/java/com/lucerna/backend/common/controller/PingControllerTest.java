package com.lucerna.backend.common.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PingController 컨트롤러 로직 단위 테스트.
 * - Authentication 파라미터는 MockHttpServletRequest.setUserPrincipal()로 주입
 *   (standaloneSetup에서 springSecurityFilterChain 없이 Authentication 주입하는 표준 방식)
 * - 보안 필터(401/403) 동작은 JwtAuthenticationEntryPointTest / JwtAccessDeniedHandlerTest에서 검증
 */
class PingControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PingController())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/ping - 인증된 사용자 요청 시 200과 userId 반환")
    void ping_인증된_사용자_200() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "test-user-sub", null, Collections.emptyList());

        mockMvc.perform(get("/api/v1/ping")
                        .with(req -> {
                            req.setUserPrincipal(auth);
                            return req;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("test-user-sub"))
                .andExpect(jsonPath("$.data.message").value("토큰 검증 성공"));
    }
}
