package com.eliall.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eliall.common.Config;
import com.eliall.common.EliObject;
import com.eliall.daemon.Logger;
import com.eliall.definition.Admin;
import com.eliall.definition.Cache;
import com.eliall.definition.View;
import com.eliall.object.Mapping;
import com.eliall.util.HTTP;
import com.eliall.util.Tool;

@SuppressWarnings({"rawtypes","unchecked"})
@MultipartConfig(fileSizeThreshold=1024*1024*16, maxFileSize=1024*1024*32, maxRequestSize=1024*1024*64)
public class GateServlet extends HttpServlet {
	public static final ConcurrentHashMap<String, Object> classCache = new ConcurrentHashMap<String, Object>(), methodCache = new ConcurrentHashMap<String, Object>();
	
	private static final Class[] methodParams = { HttpServletRequest.class, HttpServletResponse.class, EliObject.class }, viewParams = { HttpServletRequest.class, HttpServletResponse.class };
	private static final String templateRegex = "/(.+)\\." + Config.EXTENSION + "$";

	@Override
	public void init() throws ServletException { super.init(); }

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		EliObject parameters = null;
		Object controller = null;
		boolean mobile = false;

		if (Tool.nvl(request.getCharacterEncoding()).equals("")) {
			request.setCharacterEncoding(Config.CHARSET);
			response.setCharacterEncoding(Config.CHARSET);
		} else if (Tool.nvl(response.getCharacterEncoding()).equals("")) response.setCharacterEncoding(Config.CHARSET);

		if (request.getMethod().toUpperCase().equals("OPTIONS")) { response.setStatus(HttpServletResponse.SC_OK); super.service(request, response); return; }

		controller = controller(request);
		mobile = mobile(request, response);

		request.setAttribute("parameters", parameters = parameters(request));
		request.setAttribute("page", URLEncoder.encode(request.getRequestURI() + (!Tool.nvl(request.getQueryString()).equals("") ? "?" + request.getQueryString() : ""), Config.CHARSET));

		try {
			String names[] = ((String)request.getAttribute(Config.METHOD_KEY)).split("[_]+"), template = null;
			StringBuilder methodName = new StringBuilder(); request.removeAttribute(Config.METHOD_KEY);
			
			Method method = null;
			List files = null;
			View view = null;
			
			for (int a=0 ; a<names.length ; a++) {
				if (a == 0) methodName.append(names[a]);
				else methodName.append(Character.toString(names[a].charAt(0)).toUpperCase() + names[a].substring(1));
			}

			try {
				if (controller == null) throw new NoSuchMethodException("Not found controller");
				else if (controller.getClass().getAnnotation(Admin.class) != null) request.setAttribute("admin", true);

				if ((method = controller.getClass().getMethod(methodName.toString(), methodParams)) != null) {
					if (method.getAnnotation(Admin.class) != null) request.setAttribute("admin", true);

					if ((view = method.getAnnotation(View.class)) != null) {
						if (mobile && !view.mobile().trim().equals("")) template = view.mobile();
						if (template == null) if (!view.uri().trim().equals("")) template = view.uri();

						if (!view.method().equals("")) {
							for (String item : view.method().split("[ ,\t]+")) {
								Class cls = controller.getClass();
								Method mthd = null;

								if ((mthd = (Method)methodCache.get(cls.getName() + "." + item)) == null) {
									while (cls != null) {
										if (mthd == null) try { mthd = cls.getDeclaredMethod(item, viewParams); } catch (Throwable e) { }
										if (mthd == null) try { mthd = cls.getMethod(item, viewParams); } catch (Throwable e) { }

										if (mthd != null) break;
										else cls = cls.getSuperclass();
									}

									if (mthd != null) if (mthd.getAnnotation(Cache.class) != null) methodCache.put(cls.getName() + "." + item, mthd);
								}

								if (mthd != null) {
									mthd.setAccessible(true); request.setAttribute(Config.METHOD_KEY, method);
									mthd.invoke(controller, new Object[] { request, response });
								}
							}
						}
					}
					
					method.setAccessible(true);
					method.invoke(controller, new Object[] { request, response, parameters });
				}
				
				if (request.getAttribute(Config.TEMPLATE_KEY) != null) template = Tool.nvl(request.getAttribute(Config.TEMPLATE_KEY));
				
				if (template != null && template.length() > 3) getServletContext().getRequestDispatcher(template).forward(request, response);
			} catch (Throwable e) {
				if (e instanceof NoSuchMethodException) {
					String uri = request.getRequestURI().replaceFirst(templateRegex, "/$1.jsp");
					File file = new File(getServletContext().getRealPath(uri));
					
					if (!file.exists() || !file.isFile()) throw new InvocationTargetException(e, e.getMessage() + " => " + uri);
					else getServletContext().getRequestDispatcher(uri).forward(request, response);
				} else if (!(e instanceof IllegalStateException)) throw e;
			} finally {
				if ((files = parameters.getList(Config.FILES_KEY)) != null) {
					for (Object object : files) {
						EliObject info = new EliObject(object);
						File file = new File(info.getString("path", ""));
						
						if (file.exists()) file.delete();
					}
				}
			}
		} catch (Throwable e) {
			try {
				if (e instanceof InvocationTargetException) response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, request.getRequestURI());
				else if (e instanceof NoSuchMethodException) response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
				else { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, request.getRequestURI()); throw e; }
			} catch (Throwable ee) { if (!(ee instanceof IllegalStateException)) ee.printStackTrace(System.err); } finally { Logger.error(e.getMessage(), e); }
		}
	}

	protected EliObject parameters(HttpServletRequest request) { return HTTP.parameters(request, request.getAttribute(Config.PARAM_KEY)); }
	
	protected Object controller(HttpServletRequest request) {
		String uri = request.getRequestURI().replaceFirst(templateRegex, "$1"), name = null, temp = null;
		String[] uris = (uri.startsWith("/") ? uri.substring(1) : uri).split("/"), packages = null;
		
		Class clazz = null;
		Object instance = null;
		
		if (uris == null || uris.length <= 0) return null;
		else request.setAttribute(Config.METHOD_KEY, uris[uris.length - 1]);
		
		if ((instance = classCache.get(uri)) != null) return instance;

		if (uris.length > 1) {
			try {
				if ((name = Mapping.get("/" + uri.substring(0, uri.length() - uris[uris.length - 1].length() - 1), "")).equals("")) {
					packages = (name = uri.substring(0, uri.length() - uris[uris.length - 1].length() - 1)).split("/");
					
					if (packages.length <= 1) name = Character.toString(name.charAt(0)).toUpperCase() + name.substring(1);
					else name = name.substring(0, name.length() - (temp = packages[packages.length - 1]).length() - 1) + "." + Character.toString(temp.charAt(0)).toUpperCase() + temp.substring(1);

					try { clazz = Class.forName(name = Mapping.PACKAGE + "." + name); } catch (ClassNotFoundException e) { clazz = Class.forName(name = name + "Controller"); }
				} else clazz = Class.forName(name);
				
				if (clazz == null || Modifier.isAbstract(clazz.getModifiers())) return null;

				if (clazz != null && (instance = clazz.getDeclaredConstructor().newInstance()) != null) if (clazz.getConstructor().getAnnotation(Cache.class) != null) classCache.put(uri, instance);
			} catch (Throwable e) { if (!(e instanceof ClassNotFoundException)) e.printStackTrace(System.err); } finally { Logger.debug("Controller: " + name); }
		}

		return instance;
	}
	
	protected boolean mobile(HttpServletRequest request, HttpServletResponse response) {
		String agent = Tool.nvl(request.getHeader("User-Agent")).toLowerCase(), cookie = HTTP.cookie(request, Config.DEVICE_KEY);
		boolean mobile = false;

		if (cookie != null) mobile = Boolean.parseBoolean(cookie);
		
		if (cookie == null) {
			if (!mobile) mobile = agent.indexOf("iphone") > 0 || agent.indexOf("ipad") > 0;
			if (!mobile) mobile = !(agent.indexOf("mac") > 0 || agent.indexOf("windows") > 0);
			
			HTTP.cookie(request, response, HTTP.domain(request), Config.DEVICE_KEY, String.valueOf(mobile), -1);
		}

		return mobile;
	}
}