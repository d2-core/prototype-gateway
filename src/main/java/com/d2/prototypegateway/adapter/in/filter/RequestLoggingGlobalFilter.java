package com.d2.prototypegateway.adapter.in.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
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
public class RequestLoggingGlobalFilter implements GlobalFilter, Ordered {

	private final ModifyRequestBodyGatewayFilterFactory modifyRequestBodyGatewayFilterFactory;

	private final LoggingUseCase loggingUseCase;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		MediaType mediaType = exchange.getRequest().getHeaders().getContentType();

		if (mediaType != null && mediaType.includes(MediaType.MULTIPART_FORM_DATA)) {
			return logRequest(exchange, "")
				.then(chain.filter(exchange));
		}

		return modifyRequestBodyGatewayFilterFactory
			.apply(modifyRequestBodyGatewayFilterConfig())
			.filter(exchange, chain);
	}

	@Override
	public int getOrder() {
		return OrderConstant.REQUEST;

	}

	private ModifyRequestBodyGatewayFilterFactory.Config modifyRequestBodyGatewayFilterConfig() {
		return new ModifyRequestBodyGatewayFilterFactory.Config()
			.setRewriteFunction(String.class, String.class, (exchange, requestBody) ->
					logRequest(exchange, requestBody)
						.then(Mono.justOrEmpty(requestBody))
			);
	}

	private Mono<Void> logRequest(ServerWebExchange exchange, String requestBody) {
		ServerHttpRequest request = exchange.getRequest();
		HttpHeaders headers = request.getHeaders();
		String uuid = headers.getFirst(HeaderConstant.X_D2_REQUEST_UUID);

		return loggingUseCase.logRequest(
			uuid,
			request.getMethod().toString(),
			request.getHeaders(),
			requestBody != null ? requestBody : "{}",
			request.getQueryParams()
		);
	}
}
