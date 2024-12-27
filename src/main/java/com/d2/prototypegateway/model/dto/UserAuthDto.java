package com.d2.prototypegateway.model.dto;

import com.d2.prototypegateway.model.enums.TokenRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthDto {
	private Long userId;
	private TokenRole tokenRole;
}
