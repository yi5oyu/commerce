package io.github.yi5oyu.commerce.common.error;

/** 검증 실패 응답의 `errors` 항목 */
public record FieldErrorDetail(String field, String message) {
}
