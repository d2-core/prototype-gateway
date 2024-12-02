package com.d2.prototypegateway.core.exception;

import com.d2.prototypegateway.core.api.Result;

public interface ApiException {
	Integer getHttpCode();

	Result getResult();

	String getLog();

	Object getBody();
}