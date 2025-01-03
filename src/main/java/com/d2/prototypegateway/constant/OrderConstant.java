package com.d2.prototypegateway.constant;

import org.springframework.core.Ordered;

public class OrderConstant {
	public static Integer MULTI = Ordered.HIGHEST_PRECEDENCE;
	public static Integer AUTH = -200;
	public static Integer REQUEST = -100;
	public static Integer RESPONSE = -50;
	public static Integer EXCEPTION = Ordered.LOWEST_PRECEDENCE;
}
