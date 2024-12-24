package com.d2.prototypegateway.model.domain;

import com.d2.prototypegateway.model.enums.TokenRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Auth {
	private TokenRole tokenRole;

	private Long id;

	private String authDetailJson;
}
