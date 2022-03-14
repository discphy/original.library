package com.eliall.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.eliall.util.JSON;
import com.eliall.util.Tool;

@SuppressWarnings("rawtypes")
public class Config {
	public final static String DEVICE_KEY = "device&", PARAM_KEY = "param&", METHOD_KEY = "method&", TEMPLATE_KEY = "template&", FILES_KEY = "files&", TOKEN_KEY = "token&", TOKEN_SUFFIX = "tk";
	public final static String XHR_HEADER_KEY = "Origin-Headers", REFER_URI_KEY = "Referer-URI", APP_AGENT_KEY = "App-Agent", CORE_SERVER_KEY = "Core-Server", SERVER_NAME_KEY = "Server-Name";
	public final static String SITE_CODE_KEY = "Site-Code", USER_TOKEN_KEY = "User-Token", USER_IP_KEY = "User-IP";
	public final static int MAX_THREADS = Runtime.getRuntime().availableProcessors();

	protected static final String serverName = Tool.hostName() + "/" + Tool.networkAddress();
	protected static final Pattern varKeyPettern = Pattern.compile(".*\\$\\{([^${}]+)\\}.*");
	protected static final HashMap<String, String> config = new HashMap<String, String>();
	protected static final Properties properties = new Properties();
	
	public static String SEC_KEY = new String(new char[] { 101,108,102,105,110,49,48 }), SEC_PASS = new String(new char[] { 101,108,105,97,108,108 });
	public static String CHARSET = "UTF-8", EXTENSION = "do";
	
	protected static String tempPath = Tool.nvl(System.getProperty("java.io.tmpdir"), "/tmp"), storagePath = null;
	protected static Class configClass = new Object(){}.getClass().getEnclosingClass();
	
	static {
		String osName = System.getProperty("os.name").toLowerCase(), osType = "";
		
		if (osName.indexOf("win") >= 0) osType = "windows";
		else if (osName.indexOf("mac") >= 0) osType = "mac";
		else if (osName.indexOf("nux") >= 0) osType = "linux";
		
		System.setProperty("os.type", osType);
	}
	
	public static void initialize() {
		File file = new File(Config.get("service.config.file")), temp;
		FileReader reader = null;
		
		try {
			if (file != null && file.exists()) reader = new FileReader(file);
			if (reader != null) properties.load(reader);
		} catch (Throwable e) { e.printStackTrace(System.err); } finally { Tool.release(reader); }
		
		if (!get("service.path.temp").equals("")) { tempPath = get("service.path.temp"); remove("service.path.temp"); }
		if (!get("service.path.storage").equals("")) { storagePath = get("service.path.storage"); remove("service.path.storage"); }
		
		if (!Tool.nvl(tempPath).equals("")) if (!(temp = new File(tempPath)).exists()) temp.mkdirs();
	}

	public static String all() {
		Iterator<String> configKeys = config.keySet().iterator();
		Set<Object> systemKeys = System.getProperties().keySet();
		String configs = "", configKey;
		
		if (properties != null) for (Object key : properties.keySet()) if (key != null && key instanceof String) configs += key + " = " + get((String)key) + "\n";
		
		while (configKeys.hasNext()) {
			configKey = configKeys.next();
			configs += configKey + " = " + get(configKey) + "\n";
		}
		
		for (Object key : systemKeys) if (key instanceof String) configs += key + " = " + System.getProperty((String)key);
		
		configs += "serverName = " + serverName;
		
		return configs;
	}
	
	public static String serverName() {
		return serverName;
	}
	
	public static String datenPath() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyy" + File.separator + "MM" + File.separator + "dd");

		return format.format(calendar.getTime());
	}
	
	public static String tempPath() { return tempPath(""); }
	public static String tempPath(String ... prefix) {
		return Tool.cleanPath(tempPath + (prefix != null ? File.separator + Tool.join(prefix, File.separator) : "") + File.separator + datenPath());
	}
	
	public static String storagePath() { return storagePath(true, ""); }
	public static String storagePath(boolean daten) { return storagePath(daten, ""); }
	public static String storagePath(boolean daten, String ... prefix) {
		return Tool.cleanPath(storagePath + (prefix != null ? File.separator + Tool.join(prefix, File.separator) : "") + (daten ? File.separator + datenPath() : ""));
	}
	
	public static String storagePrefix() {
		return storagePath.replaceFirst(get("service.path.replace"), "");
	}

	public static Properties properties() {
		Properties props = new Properties();
		Object value = null;
		
		Set<Object> systemKeys = System.getProperties().keySet();
		Set<String> configKeys = config.keySet();

		for (Object key : systemKeys) if (key instanceof String) props.setProperty((String)key, System.getProperty((String)key));
		for (String key : configKeys) if ((value = config.get(key)) != null) props.setProperty(key, String.valueOf(value));
		
		return props;
	}

	public static Map configObject(String fileName, AtomicLong lastModified, long lifeTime) {
		String config = configString(fileName, lastModified, lifeTime);
		Map object = null;
		
		if (config != null) object = JSON.stringToMap(config);
		
		return object;
	}
	
	public static String configString(String fileName, AtomicLong lastModified, long lifeTime) {
		String configPath = configFilePath(fileName);
		File configFile = configPath != null ? new File(configPath) : null;
		long modified = 0;

		if (configFile != null && configFile.exists()) {
			if (lastModified != null) {
				if ((modified = configFile.lastModified()) - lastModified.get() < lifeTime * 1000) return null;
				else lastModified.set(modified);
			}
		} else return null;
		
		FileInputStream fis = null;
		ByteArrayOutputStream body = null;
		
		try {
			byte[] buffer = new byte[1024];
			int readedBytes = -1;
			
			fis = new FileInputStream(configFile);
			body = new ByteArrayOutputStream();

			while ((readedBytes = fis.read(buffer, 0, buffer.length)) != -1) body.write(buffer, 0, readedBytes);
			
			return Config.value(body.toString());
		} catch (Throwable e) {
			e.printStackTrace(System.err);
		} finally {
			Tool.release(body);
			Tool.release(fis);
		}
		
		return null;
	}
	
	public static String classFilePath(Object object) {
		String classLocation = object.getClass().getProtectionDomain().getCodeSource().getLocation().getPath(), packageName = object.getClass().getPackage().getName();
		File classFile = new File(classLocation + File.separator + packageName.replaceAll("\\.", File.separator));

		return classFile.getAbsolutePath();
	}
	
	public static String configFilePath(String fileName) {
		String serviceConfigFile = get("service.config.file"), serviceConfigPath = serviceConfigFile != null ? new File(serviceConfigFile).getParent() : null;
		File configFile = serviceConfigPath != null ? new File(serviceConfigPath + File.separator + fileName) : null;
		
		return configFile != null ? configFile.getAbsolutePath() : null;
	}
	
	public static String url(String key) {
		return "http://127.0.0.1" + System.getProperty("server.port") + ("/" + Config.get(key)).replaceAll("//", "/");
	}
	
	public static String value(String value) {
		if (value != null) {
			String varKey, varValue;
			Matcher matcher = null;
			
			while ((matcher = varKeyPettern.matcher(value)).find()) {
				try {
					varKey = matcher.group(1);
					varValue = get(varKey);
					value = value.replaceAll("\\$\\{" + varKey + "\\}", Tool.nvl(varValue));
				} catch (IllegalArgumentException e) { }
			}
		}
		
		return value;
	}

	public static String get(String key) {
		return get(key, "");
	}

	public static String get(String key, String nvl) {
		String value = config != null ? config.get(key) : null;

		if (value == null) value = properties.containsKey(key) ? properties.getProperty(key) : null;
		if (value == null) value = System.getProperty(key);
		
		if (value == null) if (key != null) if (!key.toLowerCase().startsWith("sec")) {
			Object found = null;

			for (Field field2 : configClass.getFields()) {
				try { if (field2.getName().equals(key)) if ((found = field2.get(null)) != null) { value = found.toString(); break; } } catch (Throwable e) { }
				try { if (field2.getName().toUpperCase().equals(key.toUpperCase())) if ((found = field2.get(null)) != null) { value = found.toString(); break; } } catch (Throwable e) { }
				try { if (field2.getName().toLowerCase().equals(key.toLowerCase())) if ((found = field2.get(null)) != null) { value = found.toString(); break; } } catch (Throwable e) { }
			}
		}

		return value(value != null ? value : nvl);
	}
	
	public static void set(String key, String value) {
		config.put(key, value);
	}
	
	public static void remove(String key) {
		config.remove(key); properties.remove(key);
	}
}