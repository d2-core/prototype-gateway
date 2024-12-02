package com.d2.prototypegateway.core.api;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class API<T> {
	private Result result;
	private T body;

	public static <T> API<T> OK(T body) {
		API<T> api = new API<>();
		api.result = Result.OK();
		api.body = body;

		return api;
	}

	public static API<Object> NO_CONTENT() {
		API<Object> api = new API<>();
		api.result = Result.OK();
		api.body = new Object();

		return api;
	}

	public static API<Object> ERROR(Result result) {
		API<Object> api = new API<>();
		api.result = result;
		api.body = new Object();

		return api;
	}

	public static API<Object> ERROR(Result result, Object body) {
		API<Object> api = new API<>();
		api.result = result;
		api.body = body;

		return api;
	}
}
