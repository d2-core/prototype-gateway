package com.d2.prototypegateway.core.error;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCodeImpl implements ErrorCode {
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "GW-001", "internal server error",
		"현재 시스템에 일시적인 문제가 발생했습니다. 조금 후 다시 시도해주세요.");

	private final Integer httpCode;
	private final String code;
	private final String reason;
	private final String message;
}