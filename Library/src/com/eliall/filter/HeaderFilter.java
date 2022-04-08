package com.eliall.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eliall.common.Config;

public class HeaderFilter implements Filter {
	private final static Map<String, String> headers = new HashMap<String, String>();

	public void init(FilterConfig config) throws ServletException { }

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
		HttpServletRequest httpRequest = null;
		String requestMethod = null;
		
		if (request instanceof HttpServletRequest) requestMethod = (httpRequest = (HttpServletRequest)request).getMethod().toUpperCase();

		if (response instanceof HttpServletResponse) {
			HttpServletResponse httpResponse = (HttpServletResponse)response;

			if (httpRequest.getHeader("Origin") != null) {
				httpResponse.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"));
				httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
			}
			
			if (requestMethod != null && requestMethod.equals("OPTIONS")) {				
				httpResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE");
				httpResponse.setHeader("Access-Control-Allow-Headers", Config.XHR_HEADER_KEY);

				chain.doFilter(request, response); return;
			} else {
				if (headers != null && headers.size() > 0) for (String key : headers.keySet()) httpResponse.setHeader(key, headers.get(key));
			}
		}

		chain.doFilter(request, response);
	}

	public void destroy() { }
	
	public static void put(String key, String value) { if (key != null && value != null) headers.put(key, value); }
}