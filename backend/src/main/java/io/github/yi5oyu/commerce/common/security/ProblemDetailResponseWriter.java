package io.github.yi5oyu.commerce.common.security;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import io.github.yi5oyu.commerce.common.error.ErrorCode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Security 필터 단계(컨트롤러 밖)의 오류를 GlobalExceptionHandler와 같은 ProblemDetail 형식으로 쓴다.
 */
@Component
public class ProblemDetailResponseWriter {

    private final JsonMapper jsonMapper;

    public ProblemDetailResponseWriter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public void write(HttpServletRequest request, HttpServletResponse response, ErrorCode errorCode)
            throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", errorCode.status().getReasonPhrase());
        body.put("status", errorCode.status().value());
        body.put("detail", errorCode.detail());
        body.put("instance", request.getRequestURI());
        body.put("code", errorCode.name());

        response.setStatus(errorCode.status().value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        jsonMapper.writeValue(response.getWriter(), body);
    }
}
