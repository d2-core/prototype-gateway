package com.d2.prototypegateway.application.port.in;

import com.d2.prototypegateway.model.domain.Auth;

import reactor.core.publisher.Mono;

public interface AuthUseCase {

	Mono<Auth> getAuth(String accessToken);
}
