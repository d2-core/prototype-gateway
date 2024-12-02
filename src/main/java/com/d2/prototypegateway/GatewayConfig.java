package com.d2.prototypegateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class GatewayConfig {

	@Bean
	public ObjectMapper objectMapper() {
		com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

		objectMapper.registerModule(new Jdk8Module());

		objectMapper.registerModule(new JavaTimeModule());

		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.LowerCamelCaseStrategy());

		return objectMapper;
	}
}
