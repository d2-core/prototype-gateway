package com.d2.prototypegateway.core.error;

public interface ErrorCode {
	Integer getHttpCode();

	String getCode();

	String getReason();

	String getMessage();
}
