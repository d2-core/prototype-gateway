package com.d2.prototypegateway.application.port.in;

import com.d2.prototypegateway.model.enums.Role;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

public interface AuthUseCase {

	Mono<Tuple3<Role, Long, String>> getAuthTuple(String accessToken);
}