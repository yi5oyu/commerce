package io.github.yi5oyu.commerce.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest
@Import({
        SecurityConfig.class,
        ProblemDetailResponseWriter.class,
        ProblemDetailAuthenticationEntryPoint.class,
        ProblemDetailAccessDeniedHandler.class,
        SecurityConfigTest.SecuredTestController.class
})
class SecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProblemDetailAccessDeniedHandler accessDeniedHandler;

    @Autowired
    JsonMapper jsonMapper;

    @Test
    void protectedPathWithoutAuthenticationReturnsUnauthorizedProblem() throws Exception {
        assertProblem(mockMvc.perform(get("/test/secured")), 401, "UNAUTHORIZED", "/test/secured")
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Test
    void unknownPathWithoutAuthenticationIsAlsoUnauthorized() throws Exception {
        // 경로 존재 여부를 인증 전에 드러내지 않는다
        assertProblem(mockMvc.perform(get("/does-not-exist")), 401, "UNAUTHORIZED", "/does-not-exist");
    }

    @Test
    @WithMockUser
    void protectedPathWithAuthenticationPasses() throws Exception {
        mockMvc.perform(get("/test/secured")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void postWithoutCsrfTokenIsNotRejected() throws Exception {
        mockMvc.perform(post("/test/secured")).andExpect(status().isOk());
    }

    @Test
    void publicPathPassesWithoutAuthentication() throws Exception {
        // 슬라이스 테스트라 actuator가 없어 404 — 401이 아니면 Security를 통과한 것
        mockMvc.perform(get("/actuator/health")).andExpect(status().isNotFound());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
    }

    @Test
    void noSessionCookieIsIssued() throws Exception {
        mockMvc.perform(get("/test/secured")).andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    void accessDeniedReturnsForbiddenProblem() throws Exception {
        // 역할 기반 규칙은 Phase 2에서 생기므로 핸들러를 직접 호출해 형식만 확인
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/products");
        request.setRequestURI("/api/admin/products");
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(request, response, new AccessDeniedException("denied"));

        JsonNode body = jsonMapper.readTree(response.getContentAsString());
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType())
                .startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(body.get("code").asString()).isEqualTo("FORBIDDEN");
        assertThat(body.get("status").asInt()).isEqualTo(403);
        assertThat(body.get("instance").asString()).isEqualTo("/api/admin/products");
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

    @RestController
    static class SecuredTestController {

        @GetMapping("/test/secured")
        String secured() {
            return "ok";
        }

        @PostMapping("/test/secured")
        String securedPost() {
            return "ok";
        }
    }
}
