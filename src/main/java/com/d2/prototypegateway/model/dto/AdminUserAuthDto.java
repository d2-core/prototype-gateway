package com.d2.prototypegateway.model.dto;

import com.d2.prototypegateway.model.enums.AdminUserRole;
import com.d2.prototypegateway.model.enums.TokenRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserAuthDto {
	private Long adminUserId;
	private TokenRole tokenRole;
	private AdminUserRole adminUserRole;
}
