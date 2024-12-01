package com.d2.prototypegateway.application.service;

import org.springframework.stereotype.Service;

import com.d2.prototypegateway.application.port.in.AuthUseCase;
import com.d2.prototypegateway.application.port.out.AuthPort;
import com.d2.prototypegateway.model.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService implements AuthUseCase {
	private final ObjectMapper objectMapper;
	private final AuthPort authPort;
	@Override
	public Mono<Tuple3<Role, Long, String>> getAuthTuple(String accessToken) {
		return authPort.validateToken(accessToken)
			.flatMap(tokenClaimsDto -> {
				if (tokenClaimsDto.getRole().equals(Role.ADMIN)) {
					return authPort.getAdminUserAuth(tokenClaimsDto.getId())
						.map(detail -> Tuples.of(tokenClaimsDto.getRole(), tokenClaimsDto.getId(), detail));
				} else if (tokenClaimsDto.getRole().equals(Role.APP)) {
					return authPort.getUserAuth(tokenClaimsDto.getId())
						.map(detail -> Tuples.of(tokenClaimsDto.getRole(), tokenClaimsDto.getId(), detail));
				}
				return Mono.empty();
			})
			.switchIfEmpty(Mono.error(new Exception()))
			.onErrorResume(e -> Mono.error(new Exception(e)))
			.flatMap(tuple -> Mono.fromCallable(() -> Tuples.of(tuple.getT1(), tuple.getT2(),
				objectMapper.writeValueAsString(tuple.getT3()))))
			.onErrorResume(e -> {
				log.error("HELLO", e);
				return Mono.error(new Exception(e));
			});
	}
}
