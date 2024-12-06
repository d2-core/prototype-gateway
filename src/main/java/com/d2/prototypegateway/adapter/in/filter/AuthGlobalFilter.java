package com.d2.prototypegateway.adapter.in.filter;

import java.util.Map;
import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ServerWebExchange;

import com.d2.prototypegateway.application.port.in.ExceptionHandlerUseCase;
import com.d2.prototypegateway.constant.HeaderConstant;
import com.d2.prototypegateway.application.port.in.AuthUseCase;
import com.d2.prototypegateway.constant.OrderConstant;
import com.d2.prototypegateway.constant.ServerExchangeAttributeConstant;
import com.d2.prototypegateway.core.exception.ApiExceptionImpl;
import com.d2.prototypegateway.core.storage.ReactorContextStorage;
import com.d2.prototypegateway.model.domain.Auth;
import com.d2.prototypegateway.model.domain.HandlerException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {
	private final AuthUseCase authUseCase;
	private final ExceptionHandlerUseCase exceptionHandlerUseCase;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String uuid = UUID.randomUUID().toString();
		Map<String, Object> map = exchange.getAttributes();
		map.put(ServerExchangeAttributeConstant.REQUEST_UUID, uuid);
		map.put(ServerExchangeAttributeConstant.AUTH_FAILED, false);

		ServerWebExchange uuidExchange = exchangeSetHeaderToUUID(exchange, uuid);
		String accessToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (accessToken == null) {
			return chain.filter(uuidExchange);
		}

		String refreshToken = exchange.getRequest().getHeaders().getFirst(HeaderConstant.X_D2_REFRESH);
		if (refreshToken != null) {
			return chain.filter(uuidExchange);
		}

		return authUseCase.getAuth(accessToken)
			.contextWrite(context -> ReactorContextStorage.add(context, HeaderConstant.X_D2_REQUEST_UUID, uuid))
			.flatMap(auth -> {
				ServerWebExchange mutatedExchange = exchangeSetHeaderToAuth(uuidExchange, auth);
				return chain.filter(mutatedExchange);
			})
			.onErrorResume(ApiExceptionImpl.class, ex ->
				throwException(chain, uuidExchange, exceptionHandlerUseCase.getHandlerApiHandleException(ex)))
			.onErrorResume(WebClientRequestException.class, ex ->
				throwException(chain, uuidExchange,
					exceptionHandlerUseCase.getHandlerWebClientRequestHandleException(ex)))
			.onErrorResume(Exception.class, ex ->
				throwException(chain, uuidExchange, exceptionHandlerUseCase.getHandlerHandleException(ex)));
	}

	@Override
	public int getOrder() {
		return OrderConstant.AUTH;
	}

	private ServerWebExchange exchangeSetHeaderToUUID(ServerWebExchange exchange, String uuid) {
		ServerWebExchange mutateExchange = exchange.mutate()
			.request(exchange.getRequest().mutate()
				.headers(headers -> headers.set(HeaderConstant.X_D2_REQUEST_UUID, uuid))
				.build()
			)
			.build();
		mutateExchange.getResponse().getHeaders().set(HeaderConstant.X_D2_REQUEST_UUID, uuid);
		return mutateExchange;
	}

	private ServerWebExchange exchangeSetHeaderToAuth(ServerWebExchange exchange, Auth auth) {
		ServerWebExchange mutateExchange = exchange.mutate()
				.request(exchange.getRequest().mutate()
				.headers(headers -> {
					headers.set(HeaderConstant.X_D2_AUTH_ROLE, auth.getRole().name());
					headers.set(HeaderConstant.X_D2_AUTH_ID, String.valueOf(auth.getId()));
					headers.set(HeaderConstant.X_D2_AUTH_DETAIL, auth.getAuthDetailJson());
				})
				.build()
			)
			.build();
		HttpHeaders responseHeaders = mutateExchange.getResponse().getHeaders();
		responseHeaders.set(HeaderConstant.X_D2_AUTH_ROLE, auth.getRole().name());
		responseHeaders.set(HeaderConstant.X_D2_AUTH_ID, String.valueOf(auth.getId()));
		responseHeaders.set(HeaderConstant.X_D2_AUTH_DETAIL, auth.getAuthDetailJson());
		return mutateExchange;
	}

	private Mono<Void> throwException(GatewayFilterChain chain, ServerWebExchange exchange,
		Mono<HandlerException> domain) {
		return domain
			.flatMap(handleException -> {
				Map<String, Object> map = exchange.getAttributes();
				map.put(ServerExchangeAttributeConstant.AUTH_FAILED, true);
				map.put(ServerExchangeAttributeConstant.HANDLE_EXCEPTION, handleException);
				return chain.filter(exchange);
			});
	}
}
