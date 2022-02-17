package com.eliall.plugin;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.DataSourceFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.JournalMode;
import org.sqlite.SQLiteConfig.SynchronousMode;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteOpenMode;

public class SQLite implements DataSourceFactory {
	private String url = null;

	private SQLiteConfig config = null;
	private SQLiteDataSource source = null;
	
	public void setProperties(Properties properties) {
		config = new SQLiteConfig(properties);
		url = properties.getProperty("url");

		config.setOpenMode(SQLiteOpenMode.FULLMUTEX);
		config.setSynchronous(SynchronousMode.NORMAL);
		config.setJournalMode(JournalMode.WAL);
//		config.setBusyTimeout(busyTimeout);
		config.setCacheSize(4096);
		config.setPageSize(2048);
	}

	@Override
	public DataSource getDataSource() {
		(source != null ? source : (source = new SQLiteDataSource(config))).setUrl(url); return source;
	}
}
