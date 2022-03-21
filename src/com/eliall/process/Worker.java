package com.eliall.process;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.eliall.common.Config;
import com.eliall.common.EliObject;
import com.eliall.util.JSON;
import com.eliall.util.Tool;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class Worker implements Callback {
	public final static OkHttpClient localClient = getHttpBuilder(3, 5, 10).build(), remoteClient = getHttpBuilder(5, 30, 10).build();
	public final static ExecutorService taskExecutor = Executors.newWorkStealingPool();

	private final static AtomicInteger threadCount = new AtomicInteger();

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try { taskExecutor.shutdown(); taskExecutor.awaitTermination(3, TimeUnit.SECONDS); }
			catch (Throwable e) { } finally { taskExecutor.shutdownNow(); }
		}));
	}

	public void onResponse(Call call, Response response) throws IOException {
		String processes = Tool.nvl(call.request().header("Next-Process")).trim(), process = null;
		EliObject result = null;
		int index = -1;

		try {
			if (response.code() == 200) {
				result = new EliObject(JSON.streamToMap(response.body().byteStream()));
				process = processes.indexOf(">") > 0 ? processes.substring(0, processes.indexOf(">")) : processes;

				if ((index = processes.indexOf(">")) > 0) {
					process = processes.substring(0, index);
					processes = index + 1 < processes.length() ? processes.substring(index + 1, processes.length()) : null;
				} else {
					process = processes;
					processes = null;
				}

				if (process.equals("")) return;
				if (result == null || result.size() <= 0) return;

				threadCount.incrementAndGet();
				localClient.newCall(new Request.Builder().addHeader("Next-Process", Tool.nvl(processes)).url(Config.url(process)).post(jsonBody(result.toString())).build()).enqueue(this);
			} else onFailure(call, new IOException("{ \"response_code\": " + response.code() + ", \"response_message\": \"" + response.message() + "\", \"thread_count\": " + threadCount.getAndIncrement() + " }"));
		} finally {
			Tool.release(response);
			Worker.decrease(process);
		}
	}

	public void onFailure(Call call, IOException exception) {
		String[] processes = Tool.nvl(call.request().header("Next-Process")).trim().split(">");
		String bodyString = Tool.nvl(requestBody(call.request())), errorString = exception != null ? Tool.nvl(exception.getMessage()).trim() : "";
		EliObject errorObject = errorString.startsWith("{") && errorString.endsWith("}") ? new EliObject(errorString) : new EliObject();

		try {
			if (bodyString.startsWith("{") && bodyString.endsWith("}")) errorObject.putAll(JSON.stringToMap(bodyString), false);

			if (!errorObject.containsKey("response_code")) errorObject.put("response_code", 999);
			if (!errorObject.containsKey("response_message")) errorObject.put("response_message", exception.toString());

			if (processes.length <= 0 || Tool.nvl(processes[0]).equals("")) return;

			threadCount.incrementAndGet();
			localClient.newCall(new Request.Builder().url(Config.url(processes[0])).post(jsonBody(errorObject.toString())).build()).enqueue(this);
		} finally {
			threadCount.decrementAndGet();
		}
	}

	public static void clean() {
		localClient.connectionPool().evictAll();
		remoteClient.connectionPool().evictAll();
	}

	public static void execute(Runnable task) {
		if (taskExecutor.isShutdown() || taskExecutor.isTerminated()) return;
		else if (task != null) taskExecutor.submit(task);
	}

	public static int threads() {
		return threadCount.get();
	}

	public static int connections() {
		return localClient.dispatcher().runningCallsCount() + remoteClient.dispatcher().runningCallsCount();
	}

	public static boolean enabled() {
		return threadCount.get() < Config.MAX_THREADS;
	}

	public static int increase(String name) {
		try { return threadCount.incrementAndGet(); } finally { }
	}

	public static int decrease(String name) {
		try { return threadCount.decrementAndGet(); } finally { }
	}

	public static String requestBody(Request request) {
		if (request == null || request.body() == null) return null;
		
		RequestBody body = request.body();
		Buffer buffer = new Buffer();
		String string = null;
		
		try { body.writeTo(buffer); string = buffer.readUtf8(); } catch (Throwable e) { } finally { buffer.close(); }

		return string;
	}

	public static RequestBody jsonBody(Object body) {
		return RequestBody.create(body instanceof String ? (String)body : body.toString(), MediaType.parse("application/json; charset=" + Config.CHARSET));
	}

	public static RequestBody formBody(EliObject object) {
		FormBody.Builder body = new FormBody.Builder();

		for (String key : object.keySet()) { if (key == null) continue; else body.add(key, object.getString(key, "")); }

		return body.build(); 
	}

	public static Dispatcher getDispatcher(int maxPerHost, int maxRequests) {
		Dispatcher dispatcher = new Dispatcher();

    	dispatcher.setMaxRequestsPerHost(maxPerHost);
    	dispatcher.setMaxRequests(maxRequests);

    	return dispatcher;
	}

	private static OkHttpClient.Builder getHttpBuilder(long connectTimeout, long readTimeout, long writeTimeout) {
		OkHttpClient.Builder builder = new OkHttpClient.Builder().dispatcher(getDispatcher(Config.MAX_THREADS * 2, Config.MAX_THREADS * 4));
		ConnectionPool pool = new ConnectionPool(Config.MAX_THREADS, (connectTimeout + readTimeout + writeTimeout) * 5 / 60, TimeUnit.MINUTES);

    	return builder.connectionPool(pool).connectTimeout(connectTimeout, TimeUnit.SECONDS).readTimeout(readTimeout, TimeUnit.SECONDS).writeTimeout(writeTimeout, TimeUnit.SECONDS);
	}
}