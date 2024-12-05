package com.d2.prototypegateway.core.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import reactor.util.context.Context;
import reactor.util.context.ContextView;

public class ReactorContextStorage {
	private final static String CONTEXT_MAP = "D2_REACTOR_CONTEXT_MAP";

	public static Map<String, Object> getStorage(ContextView contextView) {
		if (contextView.hasKey(CONTEXT_MAP)) {
			return contextView.get(CONTEXT_MAP);
		}
		return Collections.emptyMap();
	}

	public static Context add(Context context, String key, String value) {
		Map<String, Object> map;
		if (context.hasKey(CONTEXT_MAP)) {
			map = context.get(CONTEXT_MAP);
		} else {
			map = new HashMap<>();
		}
		map.put(key, value);

		return context.put(CONTEXT_MAP, map);
	}

	public static Object get(ContextView contextView, String key) {
		if (contextView.hasKey(CONTEXT_MAP)) {
			Map<String, Object> map = contextView.get(CONTEXT_MAP);
			return map.get(key);
		}
		return null;
	}

	public static Context remove(Context context, String key) {
		if (context.hasKey(CONTEXT_MAP)) {
			Map<String, Object> map = context.get(CONTEXT_MAP);
			if (map.containsKey(key)) {
				map.remove(key);
				return context.put(CONTEXT_MAP, map);
			}
		}
		return context;
	}
}
