package com.lucerna.backend.common;

import com.lucerna.backend.common.exception.BusinessException;
import com.lucerna.backend.common.exception.ErrorCode;
import com.lucerna.backend.common.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("BusinessException 발생 시 ErrorCode에 맞는 HTTP 상태와 응답 반환")
    void handleBusinessException() throws Exception {
        mockMvc.perform(get("/test/business-error"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_002"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("Validation 실패 시 400 Bad Request와 COMMON_001 코드 반환")
    void handleValidationException() throws Exception {
        mockMvc.perform(post("/test/validation-error")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_001"));
    }

    @Test
    @DisplayName("처리되지 않은 예외 발생 시 500 Internal Server Error 반환")
    void handleUnhandledException() throws Exception {
        mockMvc.perform(get("/test/internal-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_500"));
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/business-error")
        public void businessError() {
            throw new BusinessException(ErrorCode.AUTH_TOKEN_EXCHANGE_FAILED);
        }

        @PostMapping("/validation-error")
        public void validationError(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/internal-error")
        public void internalError() {
            throw new RuntimeException("예상치 못한 오류");
        }

        record TestRequest(@NotBlank(message = "값은 필수입니다") String value) {
        }
    }
}
