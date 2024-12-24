package com.d2.prototypegateway.application.service;

import org.springframework.stereotype.Service;

import com.d2.prototypegateway.application.port.in.AuthUseCase;
import com.d2.prototypegateway.application.port.out.AuthPort;
import com.d2.prototypegateway.core.error.ErrorCodeImpl;
import com.d2.prototypegateway.core.exception.ApiExceptionImpl;
import com.d2.prototypegateway.model.domain.Auth;
import com.d2.prototypegateway.model.dto.TokenClaimsDto;
import com.d2.prototypegateway.model.enums.TokenRole;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService implements AuthUseCase {
	private final ObjectMapper objectMapper;
	private final AuthPort authPort;

	@Override
	public Mono<Auth> getAuth(String accessToken) {
		return authPort.validateToken(accessToken)
			.flatMap(tokenClaimsDto ->
				convertJsonWithThrow(tokenClaimsDto)
					.map(authDetailJson -> new Auth(tokenClaimsDto.getTokenRole(), tokenClaimsDto.getId(), authDetailJson))
			);
	}

	public Mono<String> convertJsonWithThrow(TokenClaimsDto tokenClaimsDto) {
		return Mono.defer(() -> {
				if (tokenClaimsDto.getTokenRole().equals(TokenRole.ADMIN)) {
					return authPort.getAdminUserAuth(tokenClaimsDto.getId());
				} else if (tokenClaimsDto.getTokenRole().equals(TokenRole.APP)) {
					return authPort.getUserAuth(tokenClaimsDto.getId());
				}
				return Mono.empty();
			})
			.switchIfEmpty(Mono.error(new ApiExceptionImpl(ErrorCodeImpl.INTERNAL_SERVER_ERROR,
				"token claims: %s".formatted(tokenClaimsDto.getTokenRole()))))
			.flatMap(detail -> Mono.fromCallable(() -> objectMapper.writeValueAsString(detail)));
	}
}
