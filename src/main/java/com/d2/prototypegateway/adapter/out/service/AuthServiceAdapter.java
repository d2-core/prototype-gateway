package com.d2.prototypegateway.adapter.out.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import com.d2.prototypegateway.application.port.out.AuthPort;
import com.d2.prototypegateway.core.api.API;
import com.d2.prototypegateway.core.remote.InternalWebClient;
import com.d2.prototypegateway.model.dto.AdminUserAuthDto;
import com.d2.prototypegateway.model.dto.TokenClaimsDto;
import com.d2.prototypegateway.model.dto.UserAuthDto;
import com.d2.prototypegateway.model.request.ValidateTokenRequest;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class AuthServiceAdapter implements AuthPort {

	private final InternalWebClient internalWebClient;

	@Value("${url.auth}")
	private String baseUrl;

	@Override
	public Mono<TokenClaimsDto> validateToken(String accessToken) {
		ValidateTokenRequest request = new ValidateTokenRequest(accessToken);
		return internalWebClient.post(baseUrl + "/auth/v1/tokens/validate", request,
				new ParameterizedTypeReference<API<TokenClaimsDto>>() {})
			.map(API::getBody);
	}

	@Override
	public Mono<AdminUserAuthDto> getAdminUserAuth(Long adminUserId) {
		return internalWebClient.get(baseUrl + "/auth/v1/admin-users/%s/auth".formatted(adminUserId), null,
				new ParameterizedTypeReference<API<AdminUserAuthDto>>() {})
			.map(API::getBody);
	}

	@Override
	public Mono<UserAuthDto> getUserAuth(Long userId) {
		return internalWebClient.get(baseUrl + "/auth/v1/users/%s/auth".formatted(userId), null,
				new ParameterizedTypeReference<API<UserAuthDto>>() {})
			.map(API::getBody);
	}
}
