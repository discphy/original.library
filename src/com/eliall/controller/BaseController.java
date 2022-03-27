package com.eliall.controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;

import com.eliall.common.Config;
import com.eliall.common.Database;
import com.eliall.common.EliObject;
import com.eliall.daemon.Logger;
import com.eliall.definition.Cache;
import com.eliall.definition.View;
import com.eliall.util.HTTP;
import com.eliall.util.Security;
import com.eliall.util.Tool;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

@SuppressWarnings("rawtypes")
public class BaseController {
	protected static final Callback HTTP_CALLBACK = new HttpCallback();
	protected static final Pattern REDIRECT_PATTERN = Pattern.compile("\\[([a-z]+)\\]((https?://[^/]+)?/.*)", Pattern.CASE_INSENSITIVE);
	protected static final String HTML_TYPE = "text/html", PLAIN_TYPE = "text/plain", JSON_TYPE = "application/json";

	@View(uri = "/pc.jsp", mobile = "/mobile.jsp", method = "method")
	public void test(HttpServletRequest request, HttpServletResponse response, EliObject parameters) {
		json(request, response, parameters);
	}

	@Cache
	protected void json(HttpServletRequest request, HttpServletResponse response, Object object) {
		response.setContentType(JSON_TYPE);
		response.setCharacterEncoding(Config.CHARSET.toLowerCase());
		
		try { response.getWriter().write(object.toString()); } catch (Throwable e) { }
	}

	protected void location(HttpServletResponse response, String location) {
		response.setContentType(HTML_TYPE);
		response.setCharacterEncoding(Config.CHARSET.toLowerCase());
		
		try { response.getWriter().print("<html><body><script>location.replace(\"" + location + "\");</script></body></html>"); } catch (Throwable e) { }
	}

	protected void noncacheable(HttpServletRequest request, HttpServletResponse response) {
		response.setHeader("Pragma", "no-cache"); response.setDateHeader("Expires", 0);
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	}
	
	protected void error(HttpServletResponse response, int code, Object object) {
		error(response, code, object, null);
	}

	protected void error(HttpServletResponse response, int code, Object object, SqlSession session) {
		if (code >= 300) try { 
			if (object != null) {
				String message = object.toString();
				Throwable error = null;
				
				if (object instanceof Throwable) message = (error = (Throwable)object).getMessage(); 
				
				response.setHeader("Status-Text", message);
				response.sendError(code, message);
				
				Logger.error(message, error);
			} else response.sendError(code);
		} catch (Throwable e) { } finally { if (session != null) Database.rollback(session); }
	}

	protected void error(HttpServletRequest request, HttpServletResponse response, int code, String message, Object object) {
		if (object != null) {
			try {
				Method method = null;
				
				try { method = object.getClass().getMethod("put", Object.class, Object.class); }
				catch (Throwable e) { method = object.getClass().getMethod("put", String.class, Object.class); }
				
				if (method != null) {
					method.invoke(object, "code", code);
					method.invoke(object, "error", message);
					
					json(request, response, object); return;
				}
			} catch (Throwable e) { }
		}
		
		error(response, code, message);
	}

	protected void error(Class clazz, Throwable error) {
		error(clazz.getEnclosingClass().getSimpleName(), clazz.getEnclosingMethod().getName(), null, error, null);
	}

	protected void error(String clazz, String method, Throwable error) {
		error(clazz, method, null, error, null);
	}

	protected void error(String clazz, String method, String message, Throwable error) {
		error(clazz, method, message, error, null);
	}

	protected void error(String clazz, String method, String message, Throwable error, SqlSession session) {
		Logger.error("[" + clazz + (method != null ? "." + method + "()" : "") + "] " + message, error);
		Database.rollback(session);
	}

	protected HttpServletRequest request(HttpServletRequest request, String key, Object value) {
		if (key != null && value != null) request.setAttribute(key, value); return request;
	}

	protected HttpServletResponse response(HttpServletRequest request, HttpServletResponse response, Object object) {
		EliObject parameters = object instanceof EliObject ? (EliObject)object : new EliObject(object);

		if (request != null) if (request.getMethod().toUpperCase().equals("OPTIONS")) if (response != null) response.setStatus(HttpServletResponse.SC_OK);
		if (response != null) { if (response.getContentType() == null) response.setContentType(HTML_TYPE); response.setCharacterEncoding(Config.CHARSET.toLowerCase()); }

		if (object != null) {
			String method = null, url = null, key = null, value = null;
			Matcher matcher = null;
			
			if (parameters.containsKey("@__redirect")) url = (String)parameters.remove("@__redirect");
			else if (object instanceof String) url = (String)object;

			if (url != null && (matcher = REDIRECT_PATTERN.matcher(url)).find()) {
				method = matcher.group(1);
				url = matcher.group(2);
			} else url = null;

			if (url != null) {
				if (Tool.nvl(method, "GET").toUpperCase().equals("POST")) {
					try {
						response.setContentType(HTML_TYPE); response.getWriter().write("<html><head></head><body><form name=\"redirect\" method='POST' action=\"" + url + "\">");
						
						for (String name : parameters.keySet()) response.getWriter().write("<input type='hidden' name='" + name + "' value=\"" + parameters.get(name) + "\">");
						for (String param : Tool.nvl(parameters.remove("@__query", "")).split("[&]+")) {
							if (param.indexOf("=") < 0) continue;
							else { key = param.substring(param.indexOf("=")); value = param.substring(param.indexOf("=") + 1, param.length()); }
							
							response.getWriter().write("<input type='hidden' name='" + key + "' value=\"" + value + "\">");
						}
						
						response.getWriter().write("</form><script>document.forms[\"redirect\"].submit();</script></body></html>");
					} catch (Throwable e) { }
				} else {
					url += (url.indexOf("?") > 0 ? "&" : "?") + parameters.remove("@__query", "");
					url += "&" + Tool.nvl(request.getQueryString()); url = url.replaceAll("[&]+", "&");

					try { response.sendRedirect(url); } catch (Throwable e) { }
				}
			} else try { response.getWriter().write(object.toString()); } catch (Throwable e) { }
		}
		
		return response;
	}

	protected String address(HttpServletRequest request) {
		return Tool.nvl(request.getHeader(Config.USER_IP_KEY), HTTP.address(request));
	}
	
	protected String encrypt(String source) {
		try { return Security.encrypt(source); } catch (Throwable e) { return source; }
	}
	
	protected String decrypt(String source) {
		try { return Security.decrypt(source); } catch (Throwable e) { return source; }
	}

	protected EliObject agent(HttpServletRequest request) {
		EliObject info = new EliObject();
		String agent = Tool.nvl(request.getHeader(Config.APP_AGENT_KEY), " /" + HTTP.cookie(request, Config.APP_AGENT_KEY.toLowerCase().replaceAll("-", "_"))), strings[] = null;
		
		if ((strings = agent.split("[/]+")).length >= 3) {
			info.put("platform", strings[1]);
			info.put("version", strings[2]);
		}
		
		return info;
	}

	protected static class HttpCallback implements Callback {
		public void onResponse(Call call, Response respoonse) throws IOException { Tool.release(respoonse); }
		public void onFailure(Call call, IOException exception) { }
	}
}