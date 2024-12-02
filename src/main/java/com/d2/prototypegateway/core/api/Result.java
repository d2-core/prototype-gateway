package com.d2.prototypegateway.core.api;

import com.d2.prototypegateway.core.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Result {
	private String code;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String reason;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String message;

	public static Result OK() {
		Result result = new Result();
		result.code = "OK";
		result.reason = null;
		result.message = null;

		return result;
	}

	public static Result ERROR(String code, ErrorCode errorCode) {
		Result result = new Result();
		result.code = code;
		result.reason = "Reason: " + errorCode.getReason();
		result.message = errorCode.getMessage() + "[" + code + "]";

		return result;
	}

	public static Result ERROR(ErrorCode errorCode, String reasonArg) {
		Result result = new Result();
		result.code = errorCode.getCode();
		result.reason = "Reason: " + errorCode.getReason() + ": " + "[ " + reasonArg + " ]";
		result.message = errorCode.getMessage() + "[" + errorCode.getCode() + "]";

		return result;
	}
}
