package com.d2.prototypegateway.application.service;

import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import com.d2.prototypegateway.application.port.in.LoggingUseCase;
import com.d2.prototypegateway.model.domain.Auth;
import com.d2.prototypegateway.model.enums.Role;
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
	public Mono<Void> logRequest(String uuid, String method, MultiValueMap<String, String> headerMap,
		String requestBody, MultiValueMap<String, String> queryParamMap) {
		return Mono.fromRunnable(() -> {
			String prettyHeader = convertPrettyHeader(headerMap);

			String prettyQueryParams = convertQueryParams(queryParamMap);

			String prettyRequestBody = convertPrettyBody(requestBody);

			log.info("\n>> -- >> -- >> -- >> REQUEST  >> -- >> -- >> -- >>\n"
				+ "* UUID: " + uuid + "\n"
				+ "* Method: " + method + "\n"
				+ "* Query Params: " + prettyQueryParams + "\n"
				+ "* Request Body: " + prettyRequestBody + "\n"
				+ "* Headers:\n" + prettyHeader + "\n"
				+ ">> -- >> -- >> -- >> REQUEST  >> -- >> -- >> -- >>\n");
		});
	}

	@Override
	public Mono<Void> logResponse(String uuid, Integer statusCode, String originalUrl, String routeServiceId,
		String routeUrl, MultiValueMap<String, String> headerMap, String responseBody) {
		return Mono.fromRunnable(() -> {
			String prettyHeader = convertPrettyHeader(headerMap);

			String prettyResponseBody = convertPrettyBody(responseBody);

			log.info("\n<< -- << -- << -- << RESPONSE << -- << -- << -- <<\n"
				+ "* UUID: " + uuid + "\n"
				+ "* Http Status Code: " + statusCode + "\n"
				+ "* Original URL: " + originalUrl + "\n"
				+ "* Route Service Id: " + routeServiceId + "\n"
				+ "* Route URL: " + routeUrl + "\n"
				+ "* Response Body:\n" + prettyResponseBody + "\n"
				+ "* Headers:\n" + prettyHeader + "\n"
				+ "<< -- << -- << -- << RESPONSE << -- << -- << -- <<\n");
		});
	}

	@Override
	public Mono<Void> logAuth(String uuid, Auth auth) {
		return Mono.fromRunnable(() -> {
			log.info("\n--  --  -- --  --  --  AUTH  --  --  -- --  --  --\n"
				+ "* UUID: " + uuid + "\n"
				+ "* Role: " + auth.getRole().name() + "\n"
				+ "* Id: " + auth.getId() + "\n"
				+ "--  --  -- --  --  --  AUTH  --  --  -- --  --  --\n");
		});
	}

	private String convertPrettyHeader(MultiValueMap<String, String> headerMap) {
		StringBuilder headerBuilder = new StringBuilder();
		headerMap.forEach((name, values) -> {
			headerBuilder.append("   - ")
				.append(name)
				.append(": ")
				.append(String.join(", ", values))
				.append("\n");
		});

		return headerBuilder.toString();
	}

	private String convertPrettyBody(String body) {
		String prettyBody;
		try {
			Object jsonObject = objectMapper.readValue(body, Object.class);
			ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
			prettyBody = writer.writeValueAsString(jsonObject);
		} catch (JsonProcessingException ex) {
			prettyBody = body;
		}

		return prettyBody;
	}

	private String convertQueryParams(MultiValueMap<String, String> queryParamMap) {
		StringBuilder queryBuilder = new StringBuilder();
		queryParamMap.forEach((key, values) -> {
			queryBuilder.append(key)
				.append(": ")
				.append(String.join(", ", values))
				.append(", ");
		});

		return queryBuilder.length() > 2
			? queryBuilder.substring(0, queryBuilder.length() - 2)
			: "";
	}
}
