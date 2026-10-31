package io.github.yi5oyu.commerce.common.error;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 모든 오류를 RFC 9457 ProblemDetail + 확장 필드 `code`(검증 실패 시 `errors`)로 응답한다.
 * Spring MVC 내장 예외(404 · 405 · 415 등)는 상위 클래스가 ProblemDetail로 만들고, 여기서 `code`만 붙인다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusiness(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return problem(errorCode, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception e) throws Exception {
        // 인증 · 인가 예외는 Spring Security가 401 · 403으로 처리하도록 넘긴다
        if (e instanceof AuthenticationException || e instanceof AccessDeniedException) {
            throw e;
        }
        log.error("Unhandled exception", e);
        return problem(CommonErrorCode.INTERNAL_ERROR, CommonErrorCode.INTERNAL_ERROR.detail());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new FieldErrorDetail(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, CommonErrorCode.INVALID_INPUT.detail());
        body.setProperty("code", CommonErrorCode.INVALID_INPUT.name());
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail problemDetail
                && (problemDetail.getProperties() == null || !problemDetail.getProperties().containsKey("code"))) {
            problemDetail.setProperty("code", codeFor(statusCode));
        }
        return response;
    }

    private static ResponseEntity<ProblemDetail> problem(ErrorCode errorCode, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(errorCode.status(), detail);
        body.setProperty("code", errorCode.name());
        return ResponseEntity.status(errorCode.status()).body(body);
    }

    // 내장 예외의 code: 같은 상태의 CommonErrorCode, 없으면 HTTP 상태 이름(예: METHOD_NOT_ALLOWED)
    private static String codeFor(HttpStatusCode statusCode) {
        return Arrays.stream(CommonErrorCode.values())
                .filter(code -> code.status().value() == statusCode.value())
                .map(Enum::name)
                .findFirst()
                .orElseGet(() -> {
                    HttpStatus status = HttpStatus.resolve(statusCode.value());
                    return status != null ? status.name() : "HTTP_" + statusCode.value();
                });
    }
}
