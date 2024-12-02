package com.d2.prototypegateway.adapter.in.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.d2.prototypegateway.application.port.in.LoggingUseCase;
import com.d2.prototypegateway.constant.OrderConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class ResponseLoggingGlobalFilter implements GlobalFilter, Ordered {

	private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory;

	private final LoggingUseCase loggingUseCase;

	private ModifyResponseBodyGatewayFilterFactory.Config modifyResponseGatewayFilterConfig() {
		return new ModifyResponseBodyGatewayFilterFactory.Config()
			.setRewriteFunction(String.class, String.class, (exchange, responseBody) ->
					logResponse(exchange.getResponse(), responseBody)
						.then(Mono.justOrEmpty(responseBody))
			);
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		return modifyResponseBodyGatewayFilterFactory
			.apply(modifyResponseGatewayFilterConfig())
			.filter(exchange, chain);
	}

	@Override
	public int getOrder() {
		return OrderConstant.RESPONSE;

	}

	private Mono<Void> logResponse(ServerHttpResponse response, String responseBody) {
		Integer statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 0;

		return loggingUseCase.logResponse(statusCode, response.getHeaders(), responseBody);
	}
}
