package com.d2.prototypegateway.application.port.in;

import org.springframework.util.MultiValueMap;

import reactor.core.publisher.Mono;

public interface LoggingUseCase {
	Mono<Void> logRequest(String originalUrl, String routeServiceId, String routeUrl, String method,
		MultiValueMap<String, String> headerMap, String requestBody, MultiValueMap<String, String> queryParamMap);

	public Mono<Void> logResponse(Integer statusCode, MultiValueMap<String, String> headerMap, String responseBody);
}
