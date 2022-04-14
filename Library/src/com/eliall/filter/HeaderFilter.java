package com.eliall.filter;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eliall.common.Config;
import com.eliall.common.EliObject;

public class HeaderFilter implements Filter {
	private final static EliObject configs = new EliObject(), headers = new EliObject();

	public void init(FilterConfig config) throws ServletException {
		Enumeration<String> paramNames = null;
		String paramName = null;

		if (config != null && (paramNames = config.getInitParameterNames()) != null) {
			while (paramNames.hasMoreElements()) {
				if ((paramName = paramNames.nextElement()) == null || paramName.trim().equals("")) continue;
				else configs.put(paramName, config.getInitParameter(paramName));
			}
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
		HttpServletRequest httpRequest = null;
		String requestMethod = null, headerValue = null;
		
		if (request instanceof HttpServletRequest) requestMethod = (httpRequest = (HttpServletRequest)request).getMethod().toUpperCase();

		if (response instanceof HttpServletResponse) {
			HttpServletResponse httpResponse = (HttpServletResponse)response;
			boolean cors = configs.getBoolean("Cross-Domain");

			if (cors && httpRequest.getHeader("Origin") != null) {
				httpResponse.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"));
				httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
			}
			
			if (cors && requestMethod != null && requestMethod.equals("OPTIONS")) {				
				httpResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE");
				httpResponse.setHeader("Access-Control-Allow-Headers", Config.XHR_HEADER_KEY);

				chain.doFilter(request, response); return;
			} else {
				if (headers != null && headers.size() > 0) for (String key : headers.keySet()) {
					if ((headerValue = headers.getString(key, "")).equals("")) continue;
					else httpResponse.setHeader(key, headerValue);
				}
			}
		}

		chain.doFilter(request, response);
	}

	public void destroy() { }
	
	public static void put(String key, String value) { if (key != null && value != null) headers.put(key, value); }
}