package com.d2.prototypegateway.core.remote;

import java.awt.*;
import java.net.URI;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.d2.prototypegateway.constant.HeaderConstant;
import com.d2.prototypegateway.core.api.API;
import com.d2.prototypegateway.core.exception.ApiExceptionImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
public class InternalWebClient {
	private final WebClient webClient;
	private final ObjectMapper objectMapper;

	public InternalWebClient(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.webClient = WebClient.builder()
			.build();
	}

	public <T> Mono<API<T>> get(String url, Object query) {
		return Mono.deferContextual(contextView ->
			webClient
				.get()
				.uri(converUri(url, query))
				.header(contextView.hasKey(HeaderConstant.X_REQUEST_UUID) ? HeaderConstant.X_REQUEST_UUID : "X",
					contextView.getOrDefault(HeaderConstant.X_REQUEST_UUID, "X"))
				.retrieve()
				.onStatus(
					status -> !status.is2xxSuccessful(),
					this::doesNotSuccess
				)
				.bodyToMono(new ParameterizedTypeReference<API<T>>() {})
			);
	}

	public <T> Mono<API<T>> post(String url, Object body) {
		return Mono.deferContextual(contextView ->
			webClient
				.post()
				.uri(URI.create(url))
				.header(contextView.hasKey(HeaderConstant.X_REQUEST_UUID) ? HeaderConstant.X_REQUEST_UUID : "X",
					contextView.getOrDefault(HeaderConstant.X_REQUEST_UUID, "X"))
				.bodyValue(body)
				.retrieve()
				.onStatus(
					status -> !status.is2xxSuccessful(),
					this::doesNotSuccess
				)
				.bodyToMono(new ParameterizedTypeReference<API<T>>() {})
			);
	}

	public <T> Mono<API<T>> patch(String url, Object body) {
		return Mono.deferContextual(contextView ->
				webClient
					.patch()
					.uri(URI.create(url))
					.header(contextView.hasKey(HeaderConstant.X_REQUEST_UUID) ? HeaderConstant.X_REQUEST_UUID : "X",
						contextView.getOrDefault(HeaderConstant.X_REQUEST_UUID, "X"))
					.bodyValue(body)
					.retrieve()
					.onStatus(
						status -> !status.is2xxSuccessful(),
						this::doesNotSuccess
					)
					.bodyToMono(new ParameterizedTypeReference<API<T>>() {})
			);
	}

	public <T> Mono<API<T>> delete(String url, Object query) {
		return Mono.deferContextual(contextView ->
				webClient
					.delete()
					.uri(converUri(url, query))
					.header(contextView.hasKey(HeaderConstant.X_REQUEST_UUID) ? HeaderConstant.X_REQUEST_UUID : "X",
						contextView.getOrDefault(HeaderConstant.X_REQUEST_UUID, "X"))
					.retrieve()
					.onStatus(
						status -> !status.is2xxSuccessful(),
						this::doesNotSuccess
					)
					.bodyToMono(new ParameterizedTypeReference<API<T>>() {})
			);
	}

	private URI converUri(String url, Object query) {
		if (query == null) {
			return URI.create(url);
		}

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
		Map<String, Object> map = objectMapper.convertValue(query, new TypeReference<>() {
		});

		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() != null) {
				uriBuilder.queryParam(entry.getKey(), entry.getValue());
			}
		}

		return uriBuilder.build().encode().toUri();
	}

	private <T> Mono<T> doesNotSuccess(ClientResponse response) {
		return response.bodyToMono(new ParameterizedTypeReference<API<T>>() {})
			.flatMap(api -> Mono.error(new ApiExceptionImpl(response.statusCode().value(), api)));
	}
}
