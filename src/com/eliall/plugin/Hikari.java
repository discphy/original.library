package com.eliall.plugin;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.DataSourceFactory;

import com.eliall.util.Tool;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Hikari implements DataSourceFactory {
	private HikariConfig config = null;
	private HikariDataSource source = null;

	@Override
	public void setProperties(Properties properties) {
		InputStream stream = null;
		Properties props = null;
		
		try {
			config = new HikariConfig();
			props = new Properties();
			
			if (properties.containsKey("file")) {
				try {
					stream = new FileInputStream(properties.getProperty("file"));
				} catch (Throwable e) {
					stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(properties.getProperty("file"));
				}
				
				if (stream != null) props.load(stream);
			}
		} catch (Throwable e) { e.printStackTrace(System.err); } finally { Tool.release(stream); }
		
		for (Object key : properties.keySet()) props.setProperty(key.toString(), properties.getProperty(key.toString()));
		for (Object key : props.keySet()) config.addDataSourceProperty("dataSource." + key.toString(), props.getProperty(key.toString()));
		
		if (!Tool.nvl(props.getProperty("driver")).equals("")) config.setDriverClassName(props.getProperty("driver"));

		config.setJdbcUrl(props.getProperty("url"));
		config.setUsername(props.getProperty("username"));
		config.setPassword(props.getProperty("password"));
	}

	@Override
	public DataSource getDataSource() {
		return source != null ? source : (source = new HikariDataSource(config));
	}
}
