package com.eliall;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;

import javax.servlet.ServletConfig;

import com.eliall.common.Config;
import com.eliall.common.Database;
import com.eliall.common.EliObject;
import com.eliall.filter.HeaderFilter;
import com.eliall.filter.RewriteFilter;
import com.eliall.object.Mapping;
import com.eliall.util.JSON;
import com.eliall.util.Tool;

@SuppressWarnings("rawtypes")
public class Start {
	public static void initialize(Class clazz) {
		String classesPath = clazz.getResource("").getPath().replaceFirst(clazz.getPackage().getName().replaceAll("\\.", "/"), ""), configPath = null;
		File classesRoot = new File(classesPath), serviceRoot = null, documentRoot = null, configFile = null;

		try {
			serviceRoot = classesRoot.getParentFile().getParentFile();
			documentRoot = serviceRoot.getCanonicalFile().getParentFile();
			configFile = new File(documentRoot.getCanonicalPath() + File.separator + "config");

			if (!configFile.exists()) configFile = new File((documentRoot = documentRoot.getParentFile()).getCanonicalPath() + File.separator + "config");
			if (!configFile.exists()) configFile = new File((documentRoot = serviceRoot).getCanonicalPath() + File.separator + "config");

			if (configFile.exists()) configPath = configFile.getCanonicalPath();

			if (serviceRoot.exists()) Config.set("service.root", serviceRoot.getCanonicalPath());
			if (documentRoot.exists()) Config.set("document.root", documentRoot.getCanonicalPath());

			Config.set("server.type", Config.get("server.type", "dev"));
			Config.set("database.mode", Config.get("database.mode", ""));

			setupConfig(configPath);
			setupHeader(configPath);
			setupMapping(configPath);
			setupDatabase(configPath);

			Config.initialize();
			Database.initialize();
		} catch (Throwable e) { System.err.println(e.getMessage()); }
	}
	
	public static void setup(ServletConfig config) {
		String charset = config.getInitParameter("Character-Set"), extension = config.getInitParameter("URI-Extension"), pack = config.getInitParameter("Controller-Package");
		String[] names = { config.getInitParameter("Default-Config"), config.getInitParameter("Database-Config") };
		
		if (!Tool.nvl(pack).trim().equals("")) Mapping.PACKAGE = pack.trim();
		if (!Tool.nvl(charset).trim().equals("")) Config.CHARSET = charset.trim();
		if (!Tool.nvl(extension).trim().equals("")) Config.EXTENSION = extension.trim();
		
		for (String name : names) {
			Method method = null;

			try {
				if (Tool.nvl(name).trim().equals("")) continue;
				else method = Class.forName(name).getMethod("initialize");
				
				try { method.invoke(null); } catch (Throwable e) {
					try { method.invoke(Class.forName(name).getConstructor().newInstance()); } catch (Throwable ee) {
						method.invoke(Class.forName(name).getDeclaredConstructor().newInstance());
					}
				}
			} catch (Throwable e) { System.err.println(e.toString()); }
		}
		
		config.getServletContext().addFilter(HeaderFilter.class.getSimpleName(), HeaderFilter.class).addMappingForUrlPatterns(null, false, "/*");
		config.getServletContext().addFilter(RewriteFilter.class.getSimpleName(), RewriteFilter.class).addMappingForUrlPatterns(null, false, "/*");
	}

	private static void setupConfig(String configPath) {
		File configFile = new File(configPath + File.separator + Config.get("server.type") + ".properties");

		try {
			if (configFile.exists()) Config.set("service.config.file", configFile.getCanonicalPath());
			else throw new Exception("Setting up Service configuration failed");
		} catch (Throwable e) { System.err.println(e.getMessage() + ": " + configFile.getAbsolutePath() + " (No such file)"); }
	}
	
	private static void setupHeader(String configPath) {
		String filePath = (configPath + File.separator + "header.json").replaceAll("[/]+", "/");

		FileInputStream inputStream = null;
		EliObject headerMap = null;
		
		if (!new File(filePath).exists()) return;
		
		try {
			inputStream = new FileInputStream(filePath);
			headerMap = new EliObject(JSON.streamToMap(inputStream));
			
			for (String key : headerMap.keySet()) HeaderFilter.put(key, headerMap.getString(key));
		} catch (Throwable e) { System.err.println("Setting up Header configuration failed: " + e.getMessage()); } finally { Tool.release(inputStream); }
	}

	private static void setupMapping(String configPath) {
		String filePath = (configPath + File.separator + "uris.json").replaceAll("[/]+", "/");

		FileInputStream inputStream = null;
		EliObject uriConfig = null;
		
		if (!new File(filePath).exists()) return;

		try {
			inputStream = new FileInputStream(filePath);
			uriConfig = new EliObject(JSON.streamToMap(inputStream));

			if (uriConfig.containsKey("mapping_list")) {
				for (Object object : uriConfig.getList("mapping_list")) {
					EliObject mapping = new EliObject(object);
					String clazz = mapping.getString("class", "");
	
					Mapping.name(mapping.getString("uri"), clazz.startsWith(".") ? (uriConfig.getString("root_package", "") + clazz).replaceAll("[\\.]+", ".") : clazz);
				}
			}
			
			if (uriConfig.containsKey("rewrite_list")) {
				for (Object object : uriConfig.getList("rewrite_list")) {
					EliObject mapping = new EliObject(object);
					String rewrite = mapping.getString("rewrite", "");
					
					if (rewrite.equals("")) continue;
					else Mapping.rewrite(mapping.getString("uri"), rewrite);
				}
			}
		} catch (Throwable e) { System.err.println("Setting up URI configuration failed: " + e.getMessage()); } finally { Tool.release(inputStream); }
	}

	private static void setupDatabase(String configPath) {
		String databaseMode = Config.get("database.mode", ""), databasePrefix = (!databaseMode.equals("") ? databaseMode + "." : "");
		File configFile = new File((configPath + File.separator + databasePrefix + "database.xml").replaceAll("[/]+", "/"));
		
		FileInputStream inputStream = null;
		ByteArrayOutputStream outputStream = null;

		try {
			if (configFile.exists()) {
				String path = configFile.getParentFile().getCanonicalPath().replaceAll("\\\\", "/");
				byte[] buffer = new byte[1024];
				int readed = -1;
	
				try {
					inputStream = new FileInputStream(configFile);
					outputStream = new ByteArrayOutputStream();
					
					while ((readed = inputStream.read(buffer, 0, buffer.length)) != -1) outputStream.write(buffer, 0, readed);
	
					Config.set("database.path", path.startsWith("/") ? path : "/" + path);
					Config.set("database.config", outputStream.toString());
				} finally {
					Tool.release(outputStream);
					Tool.release(inputStream);
				}
			} else throw new Exception("");
		} catch (Throwable e) { System.err.println("Setting up Database configuration failed: " + configFile.getAbsolutePath() + " (No such file)"); }
	}
}
