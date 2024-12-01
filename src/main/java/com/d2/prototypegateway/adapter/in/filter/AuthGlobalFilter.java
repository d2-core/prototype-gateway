package com.d2.prototypegateway.adapter.in.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.d2.prototypegateway.constant.HeaderConstant;
import com.d2.prototypegateway.application.port.in.AuthUseCase;

import lombok.AllArgsConstructor;
import lombok.Data;

@Order(-10)
@Component
public class AuthGlobalFilter extends AbstractGatewayFilterFactory<AuthGlobalFilter.Config> {
	private final AuthUseCase authUseCase;

	public AuthGlobalFilter(AuthUseCase authUseCase) {
		super(Config.class);
		this.authUseCase = authUseCase;
	}

	@Override
	public GatewayFilter apply(Config config) {
		return ((exchange, chain) -> {
			String accessToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
			if (accessToken == null) {
				return chain.filter(exchange);
			}
			return authUseCase.getAuthTuple(accessToken)
				.flatMap(tuple -> chain.filter(exchange.mutate()
					.request(exchange.getRequest().mutate()
						.headers(headers -> {
							headers.set(HeaderConstant.X_D2_AUTH_ROLE, tuple.getT1().name());
							headers.set(HeaderConstant.X_D2_AUTH_ID, String.valueOf(tuple.getT2()));
							headers.set(HeaderConstant.X_D2_AUTH_DETAIL, tuple.getT3());
						}).build()
					).build()
				));
		});
	}

	@Data
	@AllArgsConstructor
	public static class Config {
		private final String message;
	}
}
