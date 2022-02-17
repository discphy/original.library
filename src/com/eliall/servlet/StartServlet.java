package com.eliall.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.eliall.Start;

public class StartServlet extends HttpServlet {
	static { Start.initialize(StartServlet.class); }

	public void init() throws ServletException {
		super.init();
		Start.setup(getServletConfig());
	}
}
