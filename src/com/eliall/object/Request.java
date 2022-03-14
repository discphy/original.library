package com.eliall.object;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import com.eliall.common.Config;
import com.eliall.common.EliObject;
import com.eliall.util.Tool;

public class Request extends EliObject implements HttpServletRequest {
	public final static String ENCODING = "enconding&", AUTHENTICATED = "authenticate&", TIMESTAMP = "timestamp&";
	public final static String SESSION = "session&", USER = "user&", URI = "uri&", URL = "url&";

	private HttpServletRequest request = null;

	public Request() { }
	public Request(HttpServletRequest request) { this.request = request; }
	
	@Override
	public Map<String, String[]> getParameterMap() {
		if (request != null) return request.getParameterMap();

		Map<String, String[]> map = new HashMap<String, String[]>();

		for (String key : keySet()) {
			Object value = get(key);

			if (value == null) continue;

			if (value instanceof String[]) map.put(key, (String[])value) ;
			else map.put(key, new String[] { getString(key) });
		}

		return map;
	}

	@Override
	public void removeAttribute(String name) {
		if (request == null) remove(name);
		else request.removeAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object o) {
		if (request == null) put(name, o);
		else request.setAttribute(name, o);
	}

	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		if (request == null) put(ENCODING, env);
		else request.setCharacterEncoding(env);
	}

	@Override public Enumeration<String> getAttributeNames() { return request != null ? request.getAttributeNames() : getParameterNames(); }
	@Override public String getCharacterEncoding() { return request != null ? request.getCharacterEncoding() : Config.CHARSET; }
	@Override public int getContentLength() { return request != null ? request.getContentLength() : toString().getBytes().length; }
	@Override public long getContentLengthLong() { return request != null ? request.getContentLengthLong() : toString().getBytes().length; }
	@Override public String getContentType() { return request != null ? request.getContentType() : getClass().getName(); }
	@Override public DispatcherType getDispatcherType() { return request != null ? request.getDispatcherType() : DispatcherType.REQUEST; }
	@Override public ServletInputStream getInputStream() throws IOException { return request != null ? request.getInputStream() : null; }
	@Override public String getLocalAddr() { return request != null ? request.getLocalAddr() : Tool.networkAddress(); }
	@Override public String getLocalName() { return request != null ? request.getLocalName() : Tool.hostName(); }
	@Override public int getLocalPort() { return request != null ? request.getLocalPort() : -1; }
	@Override public Locale getLocale() { return request != null ? request.getLocale() : Locale.getDefault(); }
	@Override public Enumeration<Locale> getLocales() { return request != null ? request.getLocales() : Collections.enumeration(Arrays.asList(getLocale())); }
	@Override public String getParameter(String key) { return request != null ? request.getParameter(key) : getString(key); }
	@Override public Enumeration<String> getParameterNames() { return request != null ? request.getParameterNames() : Collections.enumeration(keySet()); }
	@Override public String[] getParameterValues(String name) { return request != null ? request.getParameterValues(name) : getParameterMap().get(name); }
	@Override public String getProtocol() { return request != null ? request.getProtocol() : "data"; }
	@Override public BufferedReader getReader() throws IOException { return request != null ? request.getReader() : null; }
	@Deprecated public String getRealPath(String path) { return request != null ? request.getRealPath(path) : null; }
	@Override public String getRemoteAddr() { return request != null ? request.getRemoteAddr() : "127.0.0.1"; }
	@Override public String getRemoteHost() { return request != null ? request.getRemoteHost() : "localhost"; }
	@Override public int getRemotePort() { return request != null ? request.getLocalPort() : -1; }
	@Override public RequestDispatcher getRequestDispatcher(String path) { return request != null ? request.getRequestDispatcher(path) : null; }
	@Override public String getScheme() { return request != null ? request.getScheme() : "json"; }
	@Override public String getServerName() { return request != null ? request.getServerName() : Tool.hostName(); }
	@Override public int getServerPort() { return request != null ? request.getServerPort() : -1; }
	@Override public ServletContext getServletContext() { return request != null ? request.getServletContext() : null; }
	@Override public boolean isAsyncStarted() { return request != null ? request.isAsyncStarted() : false; }
	@Override public boolean isAsyncSupported() { return request != null ? request.isAsyncSupported() : false; }
	@Override public boolean isSecure() { return request != null ? request.isSecure() : false; }
	@Override public AsyncContext startAsync() throws IllegalStateException { return request != null ? request.startAsync() : null; }
	@Override public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException { return request != null ? request.startAsync(servletRequest, servletResponse) : null; }
	@Override public boolean authenticate(HttpServletResponse response) throws IOException, ServletException { return request != null ? request.authenticate(response) : getBoolean(AUTHENTICATED); }
	@Override public String changeSessionId() { return request != null ? request.changeSessionId() : getString(SESSION); }
	@Override public String getAuthType() { return request != null ? request.getAuthType() : null; }
	@Override public String getContextPath() { return request != null ? request.getContextPath() : null; }
	@Override public Cookie[] getCookies() { return request != null ? request.getCookies() : null; }
	@Override public long getDateHeader(String name) { return request != null ? request.getDateHeader(name) : getLong(TIMESTAMP, System.currentTimeMillis()); }
	@Override public String getHeader(String name) { return request != null ? request.getHeader(name) : getString(name); }
	@Override public Enumeration<String> getHeaderNames() { return request != null ? request.getHeaderNames() : getParameterNames(); }
	@Override public Enumeration<String> getHeaders(String name) { return request != null ? request.getHeaders(name) : Collections.enumeration(Arrays.asList(getParameterMap().get(name))); }
	@Override public int getIntHeader(String name) { return request != null ? request.getIntHeader(name) : getInt(name); }
	@Override public String getMethod() { return request != null ? request.getMethod() : "POST"; }
	@Override public Part getPart(String name) throws IOException, ServletException { return request != null ? request.getPart(name) : null; }
	@Override public Collection<Part> getParts() throws IOException, ServletException { return request != null ? request.getParts() : null; }
	@Override public String getPathInfo() { return request != null ? request.getPathInfo() : null; }
	@Override public String getPathTranslated() { return request != null ? request.getPathTranslated() : null; }
	@Override public String getQueryString() { return request != null ? request.getQueryString() : Tool.queryString(this); }
	@Override public String getRemoteUser() { return request != null ? request.getRemoteUser() : getString(USER); }
	@Override public String getRequestURI() { return request != null ? request.getRequestURI() : getString(URI); }
	@Override public StringBuffer getRequestURL() { return request != null ? request.getRequestURL() : new StringBuffer(getString(URL)); }
	@Override public String getRequestedSessionId() { return request != null ? request.getRequestedSessionId() : getString(SESSION); }
	@Override public String getServletPath() { return request != null ? request.getServletPath() : null; }
	@Override public HttpSession getSession() { return request != null ? request.getSession() : null; }
	@Override public HttpSession getSession(boolean create) { return request != null ? request.getSession(create) : null; }
	@Override public Principal getUserPrincipal() { return request != null ? request.getUserPrincipal() : null; }
	@Override public boolean isRequestedSessionIdFromCookie() { return request != null ? request.isRequestedSessionIdFromCookie() : false; }
	@Override public boolean isRequestedSessionIdFromURL() { return request != null ? request.isRequestedSessionIdFromURL() : false; }
	@Deprecated public boolean isRequestedSessionIdFromUrl() { return request != null ? request.isRequestedSessionIdFromUrl() : false; }
	@Override public boolean isRequestedSessionIdValid() { return request != null ? request.isRequestedSessionIdValid() : true; }
	@Override public boolean isUserInRole(String role) { return request != null ? request.isUserInRole(role) : getBoolean(role); }
	@Override public void login(String username, String password) throws ServletException { if (request != null) request.login(username, password); }
	@Override public void logout() throws ServletException { if (request != null) request.logout(); }
	@Override public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException { return request != null ? request.upgrade(handlerClass) : null; }
	@Override public AsyncContext getAsyncContext() { return request != null ? request.getAsyncContext() : null; }
	@Override public Object getAttribute(String arg0) { return request != null ? request.getAttribute(arg0) : get(arg0); }
}