package com.d2.prototypegateway.adapter.in.filter;

import java.nio.charset.StandardCharsets;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

import com.d2.prototypegateway.constant.HeaderConstant;
import com.d2.prototypegateway.application.port.in.AuthUseCase;
import com.d2.prototypegateway.constant.OrderConstant;
import com.d2.prototypegateway.core.api.API;
import com.d2.prototypegateway.core.api.Result;
import com.d2.prototypegateway.core.error.ErrorCodeImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {
	private final AuthUseCase authUseCase;
	private final ObjectMapper objectMapper;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String accessToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (accessToken == null) {
			return chain.filter(exchange);
		}
		return authUseCase.getAuth(accessToken)
			.flatMap(auth -> {
				ServerWebExchange mutatedExchange = exchange.mutate()
					.request(exchange.getRequest().mutate()
						.headers(headers -> {
							headers.set(HeaderConstant.X_D2_AUTH_ROLE, auth.getRole().name());
							headers.set(HeaderConstant.X_D2_AUTH_ID, String.valueOf(auth.getId()));
							headers.set(HeaderConstant.X_D2_AUTH_DETAIL, auth.getAuthDetailJson());
						})
						.build()
					)
					.build();
				return chain.filter(mutatedExchange);
			});
	}

	@Override
	public int getOrder() {
		return OrderConstant.AUTH;
	}

	private Mono<Void> exceptionHandler(ServerWebExchange exchange, WebClientRequestException ex) {
		Result errorResult = Result.ERROR("GT-400", ErrorCodeImpl.INTERNAL_SERVER_ERROR);
		return getExceptionApiResult(exchange, errorResult, ex);
	}

	private Mono<Void> exceptionHandler(ServerWebExchange exchange, Throwable tx) {
		Result errorResult = Result.ERROR("GT-500", ErrorCodeImpl.INTERNAL_SERVER_ERROR);
		return getExceptionApiResult(exchange, errorResult, tx);
	}

	private Mono<Void> getExceptionApiResult(ServerWebExchange exchange, Result errorResult, Throwable tx) {
		log.error(tx.getLocalizedMessage(), tx);
		API<Object> api = API.ERROR(errorResult);
		String result;
		try {
			result = objectMapper.writeValueAsString(api);
		} catch (JsonProcessingException e) {
			result = "{}";
		}

		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
		DataBuffer buffer = response
			.bufferFactory()
			.wrap(result.getBytes(StandardCharsets.UTF_8));

		return response.writeWith(Mono.just(buffer));
	}
}
