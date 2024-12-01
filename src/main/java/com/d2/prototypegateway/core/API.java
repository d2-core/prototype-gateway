package com.d2.prototypegateway.core;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class API<T> {
	private Result result;
	private T body;
}
