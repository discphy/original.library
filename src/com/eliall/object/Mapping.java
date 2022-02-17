package com.eliall.object;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.eliall.util.Tool;

public class Mapping {
	public static String PACKAGE = null;

	private static final HashMap<String, String> URI_MAP = new HashMap<>();
	private static final HashMap<String, Mapping> URI_CACHE = new HashMap<>();
	
	private static AtomicBoolean changing = new AtomicBoolean();
	
	private String key;
	private Object value;
	private Method method;
	
	public Mapping(String key, Object value) {
		if (key == null || value == null) return;
		
		this.key = key;
		this.value = value;
	}

	public Mapping(String key, Object value, Method method) {
		if (key == null || value == null || method == null) return;

		this.key = key;
		this.value = value;
		this.method = method;
	}
	
	public String key() { return key; }
	public Object value() { return value; }
	public Method method() { return method; }

	public static void put(String uri, String clazz) {
		if (Tool.nvl(uri).equals("")) return;
		
		if (Tool.nvl(clazz).equals("")) URI_MAP.remove(uri);
		else URI_MAP.put(uri, clazz);
	}
	
	public static void put(String key, Mapping value) { 
		if (changing.get()) return;
		else changing.set(true);
		
		try { URI_CACHE.put(key, value); } finally { changing.set(false); }
	}

	public static String get(String uri, String def) { return Tool.nvl(URI_MAP.get(uri), def); }
	
	public static Mapping get(String key) { return URI_CACHE.get(key); }
}
