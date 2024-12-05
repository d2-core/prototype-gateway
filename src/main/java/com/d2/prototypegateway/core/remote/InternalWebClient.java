package com.d2.prototypegateway.core.remote;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.d2.prototypegateway.constant.HeaderConstant;
import com.d2.prototypegateway.core.api.API;
import com.d2.prototypegateway.core.exception.ApiExceptionImpl;
import com.d2.prototypegateway.core.storage.ReactorContextStorage;
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
			.filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest ->
				Mono.deferContextual(contextView -> {
					ClientRequest.Builder builder = ClientRequest.from(clientRequest);
					Set<String> d2PrefixKeys = ReactorContextStorage.getStorage(contextView).keySet().stream()
						.filter(key -> key.startsWith(HeaderConstant.X_D2_PREFIX))
						.collect(Collectors.toSet());

					d2PrefixKeys.forEach(d2PrefixKey ->
						builder.header(d2PrefixKey, String.valueOf(ReactorContextStorage.get(contextView, d2PrefixKey)))
					);

					return Mono.just(builder.build());
				})
			))
			.build();
	}

	public <T> Mono<API<T>> get(String url, Object query, ParameterizedTypeReference<API<T>> typeReference) {
		return webClient
			.get()
			.uri(converUri(url, query))
			.retrieve()
			.onStatus(
				status -> !status.is2xxSuccessful(),
				this::doesNotSuccess
			)
			.bodyToMono(typeReference);
	}

	public <T> Mono<API<T>> post(String url, Object body, ParameterizedTypeReference<API<T>> typeReference) {
		return webClient
			.post()
			.uri(URI.create(url))
			.bodyValue(body)
			.retrieve()
			.onStatus(
				status -> !status.is2xxSuccessful(),
				this::doesNotSuccess
			)
			.bodyToMono(typeReference);
	}

	public <T> Mono<API<T>> patch(String url, Object body, ParameterizedTypeReference<API<T>> typeReference) {
		return webClient
			.patch()
			.uri(URI.create(url))
			.bodyValue(body)
			.retrieve()
			.onStatus(
				status -> !status.is2xxSuccessful(),
				this::doesNotSuccess
			)
			.bodyToMono(typeReference);
	}

	public <T> Mono<API<T>> delete(String url, Object query, ParameterizedTypeReference<API<T>> typeReference) {
		return webClient
			.delete()
			.uri(converUri(url, query))
			.retrieve()
			.onStatus(
				status -> !status.is2xxSuccessful(),
				this::doesNotSuccess
			)
			.bodyToMono(typeReference);
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
