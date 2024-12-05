package com.d2.prototypegateway.model.domain;

import com.d2.prototypegateway.core.api.API;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HandlerException {
	private final Integer httpStatusCode;

	private final API<Object> resultBody;
}
