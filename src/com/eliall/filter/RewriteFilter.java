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
import javax.servlet.http.HttpServletRequestWrapper;

import com.eliall.common.Config;
import com.eliall.object.Mapping;
import com.eliall.util.Tool;

public class RewriteFilter implements Filter {
	public final static Map<String, String> MAPPING = new HashMap<String, String>();

	private final static String extensions = "do|jsp";
	
	public void init(FilterConfig config) throws ServletException { }

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
		HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper((HttpServletRequest)request);
		String requested = wrapper.getRequestURI(), filtered = filteredURI(Mapping.rewrite(requested));

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
