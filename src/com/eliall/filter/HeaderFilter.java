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

public class HeaderFilter implements Filter {
	private FilterConfig config; 

	public void init(FilterConfig config) throws ServletException { this.config = config; }

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
		HttpServletRequest httpRequest = null;
		String requestMethod = null, paramName = null;
		
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

				chain.doFilter(request, response);
			} else {
				Enumeration<String> paramNames = config.getInitParameterNames();
				
				while (paramNames.hasMoreElements()) {
					if ((paramName = paramNames.nextElement()) == null) continue;
					else httpResponse.setHeader(paramName, config.getInitParameter(paramName));
				}
			}
		}

		chain.doFilter(request, response);
	}

	public void destroy() { this.config = null; }
}