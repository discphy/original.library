package com.eliall.daemon;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import com.eliall.common.Config;
import com.eliall.common.Daemon;
import com.eliall.util.Tool;

public class Logger extends Daemon {
	public static String DEBUG = null, INFO = null, WARN = null, ERROR = null; 
	public static boolean EXECUTED = false, STDOUT = false;

	protected static final ConcurrentLinkedQueue<Log> logs = new ConcurrentLinkedQueue<Log>();
	protected static final HashMap<TimeUnit, SimpleDateFormat> formats = new HashMap<TimeUnit, SimpleDateFormat>();
	
	private static final SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static Logger logger =  null;
	
	static {
		DEBUG = path(Level.DEBUG); INFO = path(Level.INFO); WARN = path(Level.WARN); ERROR = path(Level.ERROR);

		formats.put(TimeUnit.DAYS, new SimpleDateFormat("yyyyMMdd"));
		formats.put(TimeUnit.HOURS, new SimpleDateFormat("yyyyMMddHH"));
		formats.put(TimeUnit.MINUTES, new SimpleDateFormat("yyyyMMddHHmm"));
	}
	
	public static Logger instance() {
		return logger != null ? logger : (logger = new Logger());
	}

	public Logger() {
		super(new Object(){}.getClass().getEnclosingClass().getSimpleName() + "-" + System.currentTimeMillis());
	}

	@Override
	public void process() {
		Log log = null; while ((log = logs.poll()) != null) write(log);
	}
	
	@Override
	protected void prepare() { EXECUTED = true; }

	private void write(Log log) {
		ByteBuffer fileBuffer = null;
		FileChannel fileChannel = null;
		RandomAccessFile fileRandom = null;

		try {
			log.path().getParent().toFile().mkdirs();
			
			fileRandom = new RandomAccessFile(log.path().toFile(), "rwd");
			fileBuffer = ByteBuffer.wrap((log.message + "\n").getBytes());
			
			(fileChannel = fileRandom.getChannel()).position(fileChannel.size());
			
			while (fileBuffer.hasRemaining()) fileChannel.write(fileBuffer);
		} catch (Throwable e) { } finally {
			Tool.release(fileBuffer);
			Tool.release(fileChannel);
			Tool.release(fileRandom);
		}
	}
	
	private static String path(Level level) {
		return Config.get("logger.path." + level.toString().toLowerCase().trim(), null);
	}

	public static void log(String message, String path) throws Exception { log(message, path, null); }
	public static void log(String message, String path, TimeUnit unit) throws Exception {
		if (!EXECUTED) return;

		if (path == null) throw new Exception("Path is not specified");
		else logs.offer(new Log(null, message, path, unit));
	}
	
	public static void debug(String message) { debug(message, null, null); }
	public static void debug(String message, String path) { debug(message, path, null); }
	public static void debug(String message, String path, TimeUnit unit) {
		if (!EXECUTED) return;

		if ((path = (path != null ? path : DEBUG)) == null) return;
		else logs.offer(new Log(Level.DEBUG, message, path, unit));
		
		if (STDOUT) System.out.println(message);
	}

	public static void info(String message) { info(message, null, null); }
	public static void info(String message, String path) { info(message, path, null); }
	public static void info(String message, String path, TimeUnit unit) {
		if (!EXECUTED) return;
		
		if ((path = (path != null ? path : INFO)) == null) return;
		else logs.offer(new Log(Level.INFO, message, path, unit));
		
		if (STDOUT) System.out.println(message);
	}

	public static void warn(String message) { warn(message, null, null, null); }
	public static void warn(String message, Throwable error) { warn(message, error, null, null); }
	public static void warn(String message, Throwable error, String path) { warn(message, error, path, null); }
	public static void warn(String message, Throwable error, String path, TimeUnit unit) {
		if (!EXECUTED) return;

		if ((path = (path != null ? path : WARN)) == null) return;
		else logs.offer(new Log(Level.WARN, message + (error != null ? "\t" + error.getMessage() : ""), path, unit));
		
		if (STDOUT) { System.out.println(message); if (error != null) System.out.println(error.toString()); }
	}

	public static void error(String message, Throwable error) { error(message, error, null, null); }
	public static void error(String message, Throwable error, String path) { error(message, error, path, null); }
	public static void error(String message, Throwable error, String path, TimeUnit unit) {
		if (!EXECUTED) return;

		if ((path = (path != null ? path : ERROR)) == null) return;
		else logs.offer(new Log(Level.ERROR, message + (error != null ? "\t" + error.getMessage() : ""), path, unit));
		
		if (STDOUT) { System.err.println(message); if (error != null) System.err.println(error.toString()); }
	}
	
	private static class Log {
		private Level level = Level.DEBUG;
		private String message = "", path = null;
		private TimeUnit unit = null;

		private Log(Level level, String message, String path, TimeUnit unit) {
			this.level = level;
			this.unit = TimeUnit.DAYS;
			
			if (message != null) this.message = time.format(new Date()) + (this.level != null ? "\t[" + level.toString() + "]" : "") + "\t" + message;
			if (path != null) this.path = path;
			if (unit != null) this.unit = unit;
		}

		private Path path() throws Exception {
			String suffix = formats.get(unit).format(new Date());

			if (path != null) return Paths.get(path + (path.endsWith(".") ? "" : ".") + suffix);
			else throw new Exception("No path specified");
		}
	}
	
	private enum Level {
		DEBUG, INFO, WARN, ERROR;

		public String toString() { return Tool.rpad(super.toString(), 5, " "); }
	}
}