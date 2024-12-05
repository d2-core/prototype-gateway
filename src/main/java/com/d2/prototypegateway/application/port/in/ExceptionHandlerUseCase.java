package com.d2.prototypegateway.application.port.in;

import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.d2.prototypegateway.core.exception.ApiExceptionImpl;
import com.d2.prototypegateway.model.domain.HandlerException;

import reactor.core.publisher.Mono;

public interface ExceptionHandlerUseCase {

	Mono<HandlerException> getHandlerApiHandleException(ApiExceptionImpl ex);

	Mono<HandlerException> getHandlerWebClientRequestHandleException(WebClientRequestException ex);

	Mono<HandlerException> getHandlerHandleException(Exception ex);
}
