package com.d2.prototypegateway.model.dto;

import com.d2.prototypegateway.model.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenClaimsDto {
	private Role role;
	private Long id;
}
