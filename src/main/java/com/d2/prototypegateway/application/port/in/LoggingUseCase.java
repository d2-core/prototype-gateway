package com.d2.prototypegateway.application.port.in;

import org.springframework.util.MultiValueMap;

import com.d2.prototypegateway.model.domain.Auth;

import reactor.core.publisher.Mono;

public interface LoggingUseCase {
	Mono<Void> logRequest(String uuid, String method, MultiValueMap<String, String> headerMap,
		String requestBody, MultiValueMap<String, String> queryParamMap);

	Mono<Void> logResponse(String uuid, Integer statusCode, String originalUrl,
		String routeServiceId, String routeUrl, MultiValueMap<String, String> headerMap, String responseBody);

	Mono<Void> logAuth(String uuid, Auth auth);
}
