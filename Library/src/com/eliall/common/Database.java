package com.eliall.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.eliall.definition.DB;
import com.eliall.util.Tool;

public class Database implements DB {
	protected static final HashMap<String, SqlSessionFactory> factories = new HashMap<String, SqlSessionFactory>();
	protected static final String defaults = "default";

	public static void initialize() {
		StringReader reader = null;
		SqlSessionFactory factory = null;
		
		Pattern pattern = null;
		Matcher matcher = null;
		
		try {
			pattern = Pattern.compile("<[ \t\r\n]*environment[ \\t\\r\\n]+id=['\"]([^'\"]+)['\"][ \\t\\r\\n]*>", Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(Tool.nvl(Config.get("database.config")).trim());

			while (matcher.find()) {
				try {
					reader = new StringReader(Config.get("database.config"));
					factory = new SqlSessionFactoryBuilder().build(reader, matcher.group(1), Config.properties());
				} catch (Throwable e) { e.printStackTrace(System.err); } finally { Tool.release(reader); reader = null; }
				
				if (factory != null) factories.put(matcher.group(1), factory);
			}
		} catch (Throwable e) { e.printStackTrace(System.err); }
	}

	public static SqlSession session(String id) {
		return factories.get(id).openSession(false);
	}
	
	public static SqlSession session(boolean writable) {
		return factories.get(writable ? WRITER : READER).openSession(false);
	}

	public static void commit(Object object) {
		if (object == null) return;
		
		try {
			if (object instanceof Connection) ((Connection)object).commit();
			if (object instanceof SqlSession) ((SqlSession)object).commit();
		} catch (Throwable e) { }
	}
	
	public static void rollback(Object object) {
		if (object == null) return;
		
		try {
			if (object instanceof Connection) ((Connection)object).rollback();
			if (object instanceof SqlSession) ((SqlSession)object).rollback();
		} catch (Throwable e) { }
	}
	
	public static void release(Object ... objects) { Tool.release(objects); }
	
	public static void lock(String name) {
		lock(name, true);
	}
	
	public static void unlock(String name) {
		lock(name, false);
	}
	
	public static boolean locked(String name) {
		SqlSession session = session(true);
		int count = 0;
		
		try {
			count = session.selectOne("lock-select", Tool.substring(name, 50));
		} catch (Throwable e) {
			if (!(e instanceof NullPointerException)) e.printStackTrace(System.err);
		} finally {
			release(session);
		}

		return count > 0;
	}
	
	public static String clob(Clob clob) throws SQLException, IOException {
		BufferedReader reader = null;
		StringWriter writer = new StringWriter();

		try {
			char[] buffer = new char[1024];
			int readedBytes = 0;
			
			reader = new BufferedReader(clob.getCharacterStream());
			
			while ((readedBytes = reader.read(buffer, 0, buffer.length)) != -1) writer.write(buffer, 0, readedBytes);
			
			return writer.toString();
		} finally {
			Tool.release(writer);
			Tool.release(reader);
		}
	}
	
	private static void lock(String name, boolean lock) {
		SqlSession session = session(true);
		EliObject parameters = new EliObject();
		
		parameters.put("lock_name", Tool.substring(Tool.nvl(name), 50));
		parameters.put("agent_info", Tool.networkAddress());
			
		try {
			if (lock) session.insert("lock-insert", parameters);
			else session.delete("lock-delete", parameters);
		} catch (Throwable e) {
			rollback(session);
		} finally {
			commit(session);
			release(session);
		}
	}
}
