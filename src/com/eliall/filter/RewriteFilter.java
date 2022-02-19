package com.eliall.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.eliall.common.Config;
import com.eliall.util.Tool;

public class RewriteFilter implements Filter {
	private String extensions = null;

	public void init(FilterConfig config) throws ServletException {
		if ((extensions = config.getInitParameter("Accept-Extension")) == null) extensions = "do";
		else extensions = extensions.replaceAll("[ ,\t]+", "|");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
		HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper((HttpServletRequest)request);
		String requested = wrapper.getRequestURI(), filtered = filteredURI(requested);

		if (requested.equals(filtered)) chain.doFilter(request, response);
		else wrapper.getRequestDispatcher(filtered).forward(request, response);
	}

	public void destroy() { }
	
	private String filteredURI(String uri) {
		if (uri.endsWith(Config.EXTENSION)) return uri;
		else if (Tool.nvl(extensions).trim().equals("")) return uri;
		
		if (uri.matches(".+(" + extensions + ")$")) for (String extension : extensions.split("[ ,\t|]+")) {
			if (uri.endsWith(extension)) return uri.substring(0, uri.length() - (extension.length() + 1)) + "." + Config.EXTENSION;
		}
		
		return uri;
	}
}
