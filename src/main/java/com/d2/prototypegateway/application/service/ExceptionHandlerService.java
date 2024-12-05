package com.d2.prototypegateway.application.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.d2.prototypegateway.application.port.in.ExceptionHandlerUseCase;
import com.d2.prototypegateway.core.api.API;
import com.d2.prototypegateway.core.api.Result;
import com.d2.prototypegateway.core.error.ErrorCodeImpl;
import com.d2.prototypegateway.core.exception.ApiExceptionImpl;
import com.d2.prototypegateway.model.domain.HandlerException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExceptionHandlerService implements ExceptionHandlerUseCase {

	@Override
	public Mono<HandlerException> getHandlerApiHandleException(ApiExceptionImpl ex) {
		log.error(ex.getLog(), ex);

		API<Object> api = API.ERROR(ex.getResult(), ex.getBody());

		return Mono.just(new HandlerException(ex.getHttpCode(), api));
	}

	@Override
	public Mono<HandlerException> getHandlerWebClientRequestHandleException(WebClientRequestException ex) {
		log.error(ex.getLocalizedMessage(), ex);

		Result result = Result.ERROR(ErrorCodeImpl.INTERNAL_SERVER_ERROR, "web client request fail");
		API<Object> api = API.ERROR(result);

		return Mono.just(new HandlerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), api));
	}

	@Override
	public Mono<HandlerException> getHandlerHandleException(Exception ex) {
		log.error(ex.getLocalizedMessage(), ex);
		Result result = Result.ERROR(ErrorCodeImpl.INTERNAL_SERVER_ERROR, "");
		API<Object> api = API.ERROR(result);

		return Mono.just(new HandlerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), api));
	}
}
