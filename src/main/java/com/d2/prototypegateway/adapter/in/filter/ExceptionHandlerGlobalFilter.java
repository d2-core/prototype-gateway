package com.d2.prototypegateway.adapter.in.filter;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.asm.TypeReference;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.d2.prototypegateway.constant.OrderConstant;
import com.d2.prototypegateway.constant.ServerExchangeAttributeConstant;
import com.d2.prototypegateway.core.api.API;
import com.d2.prototypegateway.model.domain.HandlerException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
@Component
public class ExceptionHandlerGlobalFilter implements GlobalFilter, Ordered {
	private final ObjectMapper objectMapper;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		Map<String, Object> map = exchange.getAttributes();
		String authFailed = String.valueOf(map.get(ServerExchangeAttributeConstant.AUTH_FAILED));
		if (!Boolean.parseBoolean(authFailed)) {
			return chain.filter(exchange);
		} else {
			return handleException(exchange);
		}
	}

	@Override
	public int getOrder() {
		return OrderConstant.EXCEPTION;
	}

	private Mono<Void> handleException(ServerWebExchange exchange) {
		Map<String, Object> map = exchange.getAttributes();

		HandlerException handleException
			= (HandlerException) map.get(ServerExchangeAttributeConstant.HANDLE_EXCEPTION);
		HttpStatus statusCode = HttpStatus.valueOf(handleException.getHttpStatusCode());

		String responseBody;
		try {
			responseBody = objectMapper.writeValueAsString(handleException.getResultBody());
		} catch (JsonProcessingException ex) {
			responseBody = "{}";
		}

		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(statusCode);
		response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
		DataBuffer buffer = response
			.bufferFactory()
			.wrap(responseBody.getBytes(StandardCharsets.UTF_8));

		return response.writeWith(Mono.just(buffer));
	}
}
