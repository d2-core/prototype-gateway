package com.d2.prototypegateway.adapter.in.filter;

import static java.util.Collections.*;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.*;

import java.net.URI;
import java.util.Set;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
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
public class RequestLoggingGlobalFilter implements GlobalFilter, Ordered {

	private final ModifyRequestBodyGatewayFilterFactory modifyRequestBodyGatewayFilterFactory;

	private final LoggingUseCase loggingUseCase;

	private ModifyRequestBodyGatewayFilterFactory.Config modifyRequestBodyGatewayFilterConfig() {

		return new ModifyRequestBodyGatewayFilterFactory.Config()
			.setRewriteFunction(String.class, String.class, (exchange, requestBody) ->
				logRequest(exchange, requestBody)
					.then(Mono.justOrEmpty(requestBody))
			);
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		return modifyRequestBodyGatewayFilterFactory
			.apply(modifyRequestBodyGatewayFilterConfig())
			.filter(exchange, chain);
	}

	@Override
	public int getOrder() {
		return OrderConstant.REQUEST;

	}

	private Mono<Void> logRequest(ServerWebExchange exchange, String requestBody) {
		ServerHttpRequest request = exchange.getRequest();
		Set<URI> uris = exchange.getAttributeOrDefault(GATEWAY_ORIGINAL_REQUEST_URL_ATTR, emptySet());
		Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
		URI routeUri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);

		String originalUrl = uris.isEmpty() ? "Unknown" : uris.iterator().next().toString();
		String routeId = route != null ? route.getId() : "Unknown";
		String routeUrl = "Unknown";
		if (route != null && routeUri != null) {
			routeUrl = route.getUri().toString() + routeUri.getPath();
		}

		return loggingUseCase.logRequest(
			originalUrl,
			routeId,
			routeUrl,
			request.getMethod().toString(),
			request.getHeaders(),
			requestBody,
			request.getQueryParams()
		);
	}
}
