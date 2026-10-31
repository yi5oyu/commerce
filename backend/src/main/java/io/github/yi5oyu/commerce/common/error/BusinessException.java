package io.github.yi5oyu.commerce.common.error;

/**
 * 도메인 규칙 위반. {@link GlobalExceptionHandler}가 ErrorCode의 상태 · code · detail로 응답한다.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.detail());
    }

    /** detail을 상황에 맞게 바꿀 때. 화면에 표시되므로 내부 정보를 넣지 않는다. */
    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
