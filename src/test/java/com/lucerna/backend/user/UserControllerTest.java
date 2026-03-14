package com.lucerna.backend.user;

import com.lucerna.backend.user.controller.UserController;
import com.lucerna.backend.user.dto.LodgeSummary;
import com.lucerna.backend.user.dto.UserMeResponse;
import com.lucerna.backend.user.entity.UserStatus;
import com.lucerna.backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private static final String KEYCLOAK_ID = "kc-sub-123";
    private static final String EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("기존 유저 조회 → 200, isNewUser=false")
    void existingUser_returns200() throws Exception {
        Jwt jwt = createJwt();
        setSecurityContext(jwt);

        UserMeResponse response = new UserMeResponse(
                1L, EMAIL, UserStatus.ACTIVE, false,
                new LodgeSummary(10L, "산책하는 고양이", null, "안녕하세요"),
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(userService.getOrCreateUser(KEYCLOAK_ID, EMAIL)).willReturn(response);

        mockMvc.perform(get("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isNewUser").value(false))
                .andExpect(jsonPath("$.data.lodge.nickname").value("산책하는 고양이"));
    }

    @Test
    @DisplayName("신규 유저 생성 → 200, isNewUser=true, lodge=null")
    void newUser_returns200() throws Exception {
        Jwt jwt = createJwt();
        setSecurityContext(jwt);

        UserMeResponse response = new UserMeResponse(
                1L, EMAIL, UserStatus.ACTIVE, true,
                null,
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(userService.getOrCreateUser(KEYCLOAK_ID, EMAIL)).willReturn(response);

        mockMvc.perform(get("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isNewUser").value(true))
                .andExpect(jsonPath("$.data.lodge").doesNotExist());
    }

    // === 헬퍼 메서드 ===

    private Jwt createJwt() {
        return Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .subject(KEYCLOAK_ID)
                .claim("email", EMAIL)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private void setSecurityContext(Jwt jwt) {
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt, List.of(new SimpleGrantedAuthority("ROLE_user"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
