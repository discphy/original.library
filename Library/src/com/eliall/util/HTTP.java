package com.eliall.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.eliall.common.Config;
import com.eliall.common.EliObject;

@SuppressWarnings({"rawtypes","unchecked"})
public class HTTP {
	private final static String domainRegex1 = "(.*)(\\.[^.]+)(\\.[^.]{2,3})(\\.[^.]{2})$", domainRegex2 = "(.*)(\\.[^.]+)(\\.[^.]{2,3})$", filenameRegex = ".*;[ \t\r\n]*filename=\"?([^'\"]+)\"";
	
	public static Map headerKeys = null;
	public static String addressKey = null;

	public static void attribute(HttpServletRequest request, String key, Object value) {
		if (request == null) return;
		else request.setAttribute(key, value);
	}

	public static void cookie(HttpServletRequest request, HttpServletResponse response, String domain, String key, String value, int ttl) {
		Cookie[] cookies = request.getCookies();
		Cookie cookie = new Cookie(key, value = Tool.nvl(value));

		if (cookies != null) {
			for (Cookie previous : cookies) {
				if (previous == null) continue;
				
				if (Tool.nvl(previous.getName()).equals(key)) {
					previous.setValue(value);
					previous.setMaxAge(1);

					response.addCookie(previous);
				}
			}
		}

		cookie.setDomain(domain != null ? domain : domain(request));
		cookie.setMaxAge(ttl);
		cookie.setPath("/");

		response.addCookie(cookie);
	}

	public static String cookie(HttpServletRequest request, String key) {
		Cookie[] cookies = request.getCookies();

		if (cookies == null) return null;
		if (Tool.nvl(key).equals("")) return null;

		for (Cookie cookie : cookies) {
			if (cookie == null) continue;

			if (Tool.nvl(cookie.getName()).equals(key)) {
				if (Tool.nvl(cookie.getValue()).equals("")) continue;
				else return cookie.getValue();
			}
		}

		return null;
	}

	public static String address(HttpServletRequest request) {
		String addr = null;

		if (Tool.nvl(addr).equals("")) addr = request.getHeader("X-User-Address");
		if (Tool.nvl(addr).equals("")) addr = request.getHeader("X-Forwarded-For");
		if (Tool.nvl(addr).equals("")) addr = request.getHeader("X-Cluster-Client-Ip");

		if (Tool.nvl(addr).equals("")) addr = request.getHeader("Proxy-Client-IP");
		if (Tool.nvl(addr).equals("")) addr = request.getHeader("WL-Proxy-Client-IP");

		if (Tool.nvl(addr).equals("")) addr = request.getRemoteAddr();

		return addr;
	}

	public static String domain(HttpServletRequest request) {
		return domain(request.getServerName());
	}

	public static String domain(String domain) {
		String filtered = domain;

		if (!domain.startsWith(".") && domain.split("[.]").length > 2) {
			if (domain.matches(domainRegex1)) filtered = domain.replaceFirst(domainRegex1, "$2$3$4");
			else if (domain.matches(domainRegex2)) filtered = domain.replaceFirst(domainRegex2, "$2$3");

			if (filtered.startsWith(".") && filtered.length() <= 6) filtered = domain;
		}

		return filtered.startsWith(".") ? filtered : "." + filtered;
	}

	public static EliObject parameters(HttpServletRequest request, Object references) {
		String type = Tool.nvl(request.getContentType()).trim().toLowerCase();
		EliObject parameters = new EliObject(references), origins = null;

		Map<String, String[]> params = request.getParameterMap();
		Object xhrs = request.getHeader(Config.XHR_HEADER_KEY);
		
		if (xhrs != null && (origins = new EliObject(xhrs)).size() > 0) for (String key : origins.keySet()) request.setAttribute(key, origins.get(key));
		
		if (headerKeys != null) for (Object key : headerKeys.keySet()) {
			if (parameters.get(key) == null) if (request.getHeader(key.toString()) != null) parameters.put(headerKeys.get(key).toString(), request.getHeader(key.toString()));
			if (parameters.get(key) == null) if (request.getAttribute(key.toString()) != null) parameters.put(headerKeys.get(key).toString(), request.getAttribute(key.toString()));
		}

		if (addressKey != null) if (parameters.get(addressKey) == null) parameters.put(addressKey, address(request));

		if (params != null) {
			for (String key : params.keySet()) {
				String[] value = params.get(key);
				List<String> values = value != null && value.length > 1 ? new ArrayList<String>() : null;

				if (values != null) for (String string : value) values.add(string);
				if (values != null) parameters.put(key, values);
				else if (value.length > 0) parameters.put(key, value[0]);
			}
		}

		if (request.getMethod().toUpperCase().equals("POST")) {
			if (type.startsWith("multipart")) {
				File temp = new File(Config.tempPath());
				List files = new ArrayList();

				byte[] buffer = new byte[1024];
				int count = 1, readed = -1;

				try {
					for (Part part : request.getParts()) {
						String path = null;
						EliObject object = null;

						InputStream input = null;
						FileOutputStream output = null;

						if (part.getSize() < 1) continue;
						else if (Tool.nvl(part.getContentType()).length() < 3) continue;
						else if (!temp.exists()) temp.mkdirs();

						try {
							object = new EliObject();
							path = temp.getAbsolutePath() + File.separator + request.hashCode() + "_" + (count++) + ".tmp";

							input = part.getInputStream();
							output = new FileOutputStream(path);

							while ((readed = input.read(buffer, 0, buffer.length)) != -1) output.write(buffer, 0, readed);

							object.put("path", path);
							object.put("name", part.getName());
							object.put("type", part.getContentType());
							object.put("file", part.getHeader("content-disposition").replaceFirst(filenameRegex, "$1"));

							files.add(object);
						} catch (Throwable e) { } finally {
							Tool.release(output);
							Tool.release(input);
						}
					}
				} catch (Throwable e) { e.printStackTrace(System.err); } finally { if (files.size() > 0) parameters.put(Config.FILES_KEY, files); }
			} else if (!type.startsWith("application/json")) {
				ByteArrayOutputStream body = null;
				InputStream stream = null;

				byte[] buffer = new byte[1024];
				int readed = -1;

				try {
					stream = request.getInputStream();
					body = new ByteArrayOutputStream();

					while ((readed = stream.read(buffer, 0, buffer.length)) != -1) body.write(buffer, 0, readed);

					parameters.putAll(JSON.stringToMap((String)request.getAttribute("body")), true);
				} catch (Throwable e) { e.printStackTrace(System.err); } finally { Tool.release(body); }
			} else try { parameters.putAll(JSON.streamToMap(request.getInputStream()), true); } catch (Throwable e) { }
		}

		return parameters;
	}
}