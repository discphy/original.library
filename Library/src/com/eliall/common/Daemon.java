package com.eliall.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.eliall.daemon.Logger;
import com.eliall.process.Worker;

public class Daemon extends Thread {
	protected static final String uuid = UUID.nameUUIDFromBytes((Config.serverName() + System.currentTimeMillis()).getBytes()).toString();

	protected AtomicBoolean runnable = new AtomicBoolean(true), paused = new AtomicBoolean(false), stopped = new AtomicBoolean(false);
	protected ConcurrentHashMap<String, Long> runnings = new ConcurrentHashMap<String, Long>();
	protected List<Method> processes = new ArrayList<Method>();

	protected volatile long sleepTime = 1000, maxSleepTime = 3000, minSleepTime = 10, runInterval = sleepTime;
	
	public Daemon(String name) {
		super(name); setDaemon(true);
		
		try {
			Method[] methods = getClass().getDeclaredMethods();
	
			for (Method method : methods) {
				if (method.getAnnotation(Process.class) == null) continue;
				
				method.setAccessible(true);
				processes.add(method);
			}
		} catch (Throwable e) { exception(e); } finally {
			Runtime.getRuntime().addShutdownHook(new Shutdown(this));
		}
	}
	
	@Override
	public void run() {
		prepare();

		while (runnable.get()) {
			try { Thread.sleep(sleepTime); } catch (Throwable e) { }
			
			try {
				if (paused()) continue;
				else process();
				
				if (stopped()) break;
			} catch (Throwable e) { exception(e); } finally { executed(); }
		}
		
		finalize();
	}

	public void shutdown() { runnable(false); }
	public void runnable(boolean flag) { runnable.set(flag); }
	public void exception(Throwable e) { Logger.error(e.getMessage(), e); }
	
	public void stop(long wait) { if (wait > 0) try { Thread.sleep(wait); } catch (Throwable e) { }; stopped.set(true); }
	public void pause(long wait) { if (wait > 0) try { Thread.sleep(wait); } catch (Throwable e) { }; paused.set(true); }
	public void resume(long wait) { if (wait > 0) try { Thread.sleep(wait); } catch (Throwable e) { }; paused.set(false); }

	public boolean paused() { return paused.get(); }
	public boolean stopped() { return stopped.get(); }
	
	protected void prepare() { }
	protected void process() {
		Object invoker = this;

		for (Method method : processes) {
			if (runnings.get(method.getName()) != null) {
				if (System.currentTimeMillis() - runnings.get(method.getName()).longValue() < runInterval) continue;
				else runnings.remove(method.getName());
			} else runnings.put(method.getName(), Long.valueOf(System.currentTimeMillis()));

			Worker.execute(new Runnable() { public void run() {
				try { method.invoke(invoker); }
				catch (Throwable e) { Logger.error(e.getMessage(), e); }
				finally { runnings.remove(method.getName()); }
			} });
		}
	}
	protected void executed() { }
	protected void finalize() { }

	private class Shutdown extends Thread {
		private Daemon target = null;
		
		public Shutdown(Daemon daemon) { super(new Object(){}.getClass().getEnclosingClass().getSimpleName() + "-" + daemon.getName()); this.target = daemon; }
		
		public void run() {
			try { target.interrupt(); } catch (Throwable e) { } finally { target.shutdown(); }
			try { target.join(); } catch (Throwable e) { }
		}
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface Process { }
}