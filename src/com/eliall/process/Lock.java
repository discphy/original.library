package com.eliall.process;

import org.apache.ibatis.session.SqlSession;

import com.eliall.common.Database;
import com.eliall.common.EliObject;

@SuppressWarnings("rawtypes")
public class Lock {
	public static String SELECT = "lock-info-select", INSERT = "lock-info-insert", DELETE = "lock-info-delete";
	
	public static void lock(String clazz, String method, int time) {
		SqlSession session = null;
		EliObject params = null;

		try {
			params = new EliObject();
			session = Database.session(true);
			
			params.put("class_key", clazz);
			params.put("method_key", method);

			if (time > 0) params.put("lock_time", time);
			
			if (session.insert(INSERT, params) > 0) Database.commit(session);
			else throw new Exception("Failed to lock for " + params.get("class_key") + "." + params.get("method_key") + "()");
		} catch (Throwable e) { Database.rollback(session); } finally { Database.release(session); }
	}

	public static void lock(Class clazz, int time) {
		lock(clazz.getEnclosingClass().getSimpleName(), clazz.getEnclosingMethod().getName(), time);
	}
	
	public static void unlock(String clazz, String method) {
		SqlSession session = null;
		EliObject params = null;
		
		try {
			params = new EliObject();
			session = Database.session(true);
			
			params.put("class_key", clazz);
			params.put("method_key", method);

			if (session.delete(DELETE, params) > 0) Database.commit(session);
			else throw new Exception("Failed to unlock for " + params.get("class_key") + "." + params.get("method_key") + "()");
		} catch (Throwable e) { Database.rollback(session); } finally { Database.release(session); }
	}
	
	public static void unlock(Class clazz) {
		unlock(clazz.getEnclosingClass().getSimpleName(), clazz.getEnclosingMethod().getName());
	}

	public static boolean locked(String clazz, String method) {
		SqlSession session = null;
		EliObject lock = null;

		try {
			session = Database.session(false);
			lock = new EliObject().set("class_key", clazz).set("method_key", method);

			if ((lock = new EliObject(session.selectOne(SELECT, lock))).size() > 0) return lock.getBoolean("locked");
		} catch (Throwable e) { } finally { Database.release(session); }

		return false;
	}
	
	public static boolean locked(Class clazz) {
		return locked(clazz.getEnclosingClass().getSimpleName(), clazz.getEnclosingMethod().getName());
	}
}