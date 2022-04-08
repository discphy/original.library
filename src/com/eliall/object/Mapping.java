package com.eliall.object;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.eliall.util.Tool;

public class Mapping {
	public static String PACKAGE = null;

	private final static HashMap<String, String> uris = new HashMap<String, String>(), rewrites = new HashMap<String, String>();
	private final static ConcurrentHashMap<String, Object> classes = new ConcurrentHashMap<String, Object>(), methods = new ConcurrentHashMap<String, Object>();

	public static void name(String uri, String clazz) {
		if (Tool.nvl(uri).equals("")) return;
		else if (!Tool.nvl(clazz).equals("")) uris.put(uri, clazz);
	}	
	public static String name(String uri) { return Tool.nvl(uris.get(uri), ""); }
	
	public static void clazz(String uri, Object object) {
		if (Tool.nvl(uri).equals("")) return;
		
		if (object == null) classes.remove(uri);
		else classes.put(uri, object);
	}
	public static Object clazz(String uri) { return classes.get(uri); }
	
	public static void method(String key, Method method) {
		if (Tool.nvl(key).equals("")) return;
		
		if (method == null) methods.remove(key);
		else methods.put(key, method);
	}
	public static Method method(String key) { return (Method)methods.get(key); }

	public static void rewrite(String uri, String rewrite) {
		if (Tool.nvl(uri).equals("")) return;
		else if (!Tool.nvl(rewrite).equals("")) rewrites.put(uri, rewrite);
	}	
	public static String rewrite(String uri) { return Tool.nvl(rewrites.get(uri), uri); }
}
