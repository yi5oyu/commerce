package io.github.yi5oyu.commerce.common.error;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// Security 필터는 끈다 — 401 · 403 형식은 Security 설정에서 검증
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandlerTest.ErrorTestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void unknownPathReturnsNotFoundProblem() throws Exception {
        assertProblem(mockMvc.perform(get("/does-not-exist")), 404, "NOT_FOUND", "/does-not-exist");
    }

    @Test
    void validationFailureReturnsFieldErrors() throws Exception {
        ResultActions result = mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "", "email": "not-an-email"}
                        """));

        assertProblem(result, 400, "INVALID_INPUT", "/test/validation")
                .andExpect(jsonPath("$.detail").value(CommonErrorCode.INVALID_INPUT.detail()))
                .andExpect(jsonPath("$.errors", hasSize(2)))
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("name", "email")))
                .andExpect(jsonPath("$.errors[0].message").isNotEmpty());
    }

    @Test
    void businessExceptionUsesErrorCode() throws Exception {
        assertProblem(mockMvc.perform(get("/test/business")), 409, "TEST_CONFLICT", "/test/business")
                .andExpect(jsonPath("$.detail").value(TestErrorCode.TEST_CONFLICT.detail()));
    }

    @Test
    void unexpectedExceptionHidesInternalMessage() throws Exception {
        assertProblem(mockMvc.perform(get("/test/unexpected")), 500, "INTERNAL_ERROR", "/test/unexpected")
                .andExpect(jsonPath("$.detail").value(CommonErrorCode.INTERNAL_ERROR.detail()))
                .andExpect(content().string(not(containsString("secret internal message"))));
    }

    @Test
    void builtInExceptionGetsStatusBasedCode() throws Exception {
        assertProblem(mockMvc.perform(post("/test/business")), 405, "METHOD_NOT_ALLOWED", "/test/business");
    }

    private static ResultActions assertProblem(ResultActions result, int status, String code, String instance)
            throws Exception {
        return result
                .andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.instance").value(instance))
                .andExpect(jsonPath("$.code").value(code));
    }

    enum TestErrorCode implements ErrorCode {
        TEST_CONFLICT;

        @Override
        public HttpStatus status() {
            return HttpStatus.CONFLICT;
        }

        @Override
        public String detail() {
            return "테스트 충돌입니다.";
        }
    }

    record ValidationRequest(@NotBlank String name, @Email String email) {
    }

    @RestController
    static class ErrorTestController {

        @PostMapping("/test/validation")
        void validation(@Valid @RequestBody ValidationRequest request) {
        }

        @GetMapping("/test/business")
        void business() {
            throw new BusinessException(TestErrorCode.TEST_CONFLICT);
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new IllegalStateException("secret internal message");
        }
    }
}
