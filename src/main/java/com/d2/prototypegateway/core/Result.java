package com.d2.prototypegateway.core;

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
}
