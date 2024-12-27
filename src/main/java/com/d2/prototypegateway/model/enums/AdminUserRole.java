package com.d2.prototypegateway.model.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AdminUserRole {
	MANAGER("관리자"),
	TEACHER("선생님"),
	TEAM_MEMBER("선생님과 함께하는 멤버");
	private final String description;
}
