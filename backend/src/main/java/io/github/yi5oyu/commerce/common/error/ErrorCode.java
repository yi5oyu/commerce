package io.github.yi5oyu.commerce.common.error;

import org.springframework.http.HttpStatus;

/**
 * 오류 종류. 응답의 `code`는 {@link #name()} — enum 상수명이 그대로 쓰인다.
 * 공통 오류는 {@link CommonErrorCode}, 도메인 오류는 도메인별 enum(예: MemberErrorCode)으로 구현한다.
 */
public interface ErrorCode {

    String name();

    HttpStatus status();

    /** 화면에 그대로 표시해도 되는 기본 설명 */
    String detail();
}
