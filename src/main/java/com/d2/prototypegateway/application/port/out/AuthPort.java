package com.d2.prototypegateway.application.port.out;

import com.d2.prototypegateway.model.dto.AdminUserAuthDto;
import com.d2.prototypegateway.model.dto.TokenClaimsDto;
import com.d2.prototypegateway.model.dto.UserAuthDto;

import reactor.core.publisher.Mono;

public interface AuthPort {

	Mono<TokenClaimsDto> validateToken(String accessToken);

	Mono<AdminUserAuthDto> getAdminUserAuth(Long adminUserId);

	Mono<UserAuthDto> getUserAuth(Long userId);
}
