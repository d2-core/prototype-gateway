package com.d2.prototypegateway.application.service;

import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import com.d2.prototypegateway.application.port.in.LoggingUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class LoggingGlobalFilterService implements LoggingUseCase {
	private final ObjectMapper objectMapper;

	@Override
	public Mono<Void> logRequest(String originalUrl, String routeServiceId, String routeUrl, String method,
		MultiValueMap<String, String> headerMap, String requestBody, MultiValueMap<String, String> queryParamMap) {
		return Mono.fromRunnable(() -> {
			StringBuilder headerBuilder = new StringBuilder();
			headerMap.forEach((name, values) -> {
				headerBuilder.append("   - ")
					.append(name)
					.append(": ")
					.append(String.join(", ", values))
					.append("\n");
			});
			StringBuilder queryBuilder = new StringBuilder();
			queryParamMap.forEach((key, values) -> {
				queryBuilder.append(key)
					.append(": ")
					.append(String.join(", ", values))
					.append(", ");
			});
			String prettyQueryParams = queryBuilder.length() > 2
				? queryBuilder.substring(0, queryBuilder.length() - 2)
				: "not exist";

			String prettyRequestBody;
			try {
				Object jsonObject = objectMapper.readValue(requestBody, Object.class);
				ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
				prettyRequestBody = writer.writeValueAsString(jsonObject);
			} catch (JsonProcessingException ex) {
				prettyRequestBody = requestBody;
			}

			log.info("\n>> -- >> -- >> -- >> REQUEST  >> -- >> -- >> -- >>\n"
				+ "Method: " + method + "\n"
				+ "Original URL: " + originalUrl + "\n"
				+ "Route Service Id: " + routeServiceId + "\n"
				+ "Route URL: " + routeUrl + "\n"
				+ "Query Params: " + prettyQueryParams + "\n"
				+ "Request Body: " + prettyRequestBody + "\n"
				+ "Headers:\n" + headerBuilder + "\n"
				+ ">> -- >> -- >> -- >> REQUEST  >> -- >> -- >> -- >>\n");
		});
	}

	@Override
	public Mono<Void> logResponse(Integer statusCode, MultiValueMap<String, String> headerMap, String responseBody) {
		return Mono.fromRunnable(() -> {
			StringBuilder headerBuilder = new StringBuilder();
			headerMap.forEach((name, values) -> {
				headerBuilder.append("   - ")
					.append(name)
					.append(": ")
					.append(String.join(", ", values))
					.append("\n");
			});

			String prettyResponseBody;
			try {
				Object jsonObject = objectMapper.readValue(responseBody, Object.class);
				ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
				prettyResponseBody = writer.writeValueAsString(jsonObject);
			} catch (JsonProcessingException ex) {
				prettyResponseBody = responseBody;
			}

			log.info("\n<< -- << -- << -- << RESPONSE << -- << -- << -- <<\n"
				+ "Http Status Code: " + statusCode + "\n"
				+ "Response Body:\n" + prettyResponseBody + "\n"
				+ "Headers:\n" + headerBuilder + "\n"
				+ "<< -- << -- << -- << RESPONSE << -- << -- << -- <<\n");
		});
	}
}
