package com.d2.prototypegateway.adapter.in.filter;

import static java.util.Collections.*;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.*;

import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.d2.prototypegateway.application.port.in.LoggingUseCase;
import com.d2.prototypegateway.constant.HeaderConstant;
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
					logResponse(exchange, responseBody)
						.then(exchangeDeleteHeaderToPrefixD2(exchange))
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

	private Mono<Void> exchangeDeleteHeaderToPrefixD2(ServerWebExchange exchange) {
		return Mono.fromRunnable(() -> {
			HttpHeaders headers = exchange.getResponse().getHeaders();
			Set<String> d2PrefixHeaderKeys = headers.keySet().stream()
				.filter(key -> key.startsWith(HeaderConstant.X_D2_PREFIX))
				.collect(Collectors.toSet());

			d2PrefixHeaderKeys.forEach(headers::remove);
		});
	}

	private Mono<Void> logResponse(ServerWebExchange exchange, String responseBody) {
		ServerHttpResponse response = exchange.getResponse();

		HttpHeaders headers = exchange.getResponse().getHeaders();

		String uuid = headers.getFirst(HeaderConstant.X_D2_REQUEST_UUID);

		Integer statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 0;

		Set<URI> uris = exchange.getAttributeOrDefault(GATEWAY_ORIGINAL_REQUEST_URL_ATTR, emptySet());
		Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
		URI routeUri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);

		String originalUrl = uris.isEmpty() ? "Unknown" : uris.iterator().next().toString();
		String routeId = route != null ? route.getId() : "Unknown";
		String routeUrl = "Unknown";
		if (route != null && routeUri != null) {
			routeUrl = route.getUri().toString() + routeUri.getPath();
		}

		return loggingUseCase.logResponse(
			uuid,
			statusCode,
			originalUrl,
			routeId,
			routeUrl,
			response.getHeaders(),
			responseBody
		);
	}
}
