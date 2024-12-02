package com.d2.prototypegateway.constant;

import org.springframework.core.Ordered;

public class OrderConstant {
	public static Integer AUTH = Ordered.HIGHEST_PRECEDENCE;
	public static Integer REQUEST = -100;
	public static Integer RESPONSE = Ordered.LOWEST_PRECEDENCE;
}
