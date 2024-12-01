package com.d2.prototypegateway.adapter.out.service;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.d2.prototypegateway.application.port.out.AuthPort;
import com.d2.prototypegateway.core.API;
import com.d2.prototypegateway.model.request.ValidateTokenRequest;
import com.d2.prototypegateway.model.dto.AdminUserAuthDto;
import com.d2.prototypegateway.model.dto.TokenClaimsDto;
import com.d2.prototypegateway.model.dto.UserAuthDto;

import reactor.core.publisher.Mono;

@Component
public class AuthServiceAdapter implements AuthPort {

	private final WebClient webClient;

	public AuthServiceAdapter() {
		this.webClient = WebClient.builder().build();

	}

	@Value("${url.auth}")
	private String baseUrl;

	@Override
	public Mono<TokenClaimsDto> validateToken(String accessToken) {
		ValidateTokenRequest request = new ValidateTokenRequest(accessToken);
		return webClient.post()
			.uri(URI.create(baseUrl + "/auth/v1/tokens/validate"))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.retrieve()
			.bodyToMono(new ParameterizedTypeReference<API<TokenClaimsDto>>() {})
			.map(API::getBody);
	}

	@Override
	public Mono<AdminUserAuthDto> getAdminUserAuth(Long adminUserId) {
		return webClient.get()
			.uri(URI.create(baseUrl + "/auth/v1/admin-users/%s/auth".formatted(adminUserId.toString())))
			.retrieve()
			.bodyToMono(new ParameterizedTypeReference<API<AdminUserAuthDto>>() {})
			.map(API::getBody);
	}

	@Override
	public Mono<UserAuthDto> getUserAuth(Long userId) {
		return webClient.get()
			.uri(URI.create(baseUrl + "/auth/v1/users/%s/auth".formatted(userId.toString())))
			.retrieve()
			.bodyToMono(new ParameterizedTypeReference<API<UserAuthDto>>() {})
			.map(API::getBody);
	}
}
