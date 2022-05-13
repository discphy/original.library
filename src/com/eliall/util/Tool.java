package com.eliall.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.eliall.common.Config;

import okhttp3.Response;

@SuppressWarnings({"rawtypes","unchecked"})
public class Tool {
	private final static Pattern VALUE_PATTERN = Pattern.compile(".*@\\{([^@{}]+)\\}.*");

	private final static char[][] START_LETTERS = { { 'ㄱ' }, { 'ㄱ', 'ㄱ' }, { 'ㄴ' }, { 'ㄷ' }, { 'ㄷ', 'ㄷ' }, { 'ㄹ' }, { 'ㅁ' }, { 'ㅂ' }, { 'ㅂ', 'ㅂ' }, { 'ㅅ' }, { 'ㅅ', 'ㅅ' }, { 'ㅇ' }, { 'ㅈ' }, { 'ㅈ', 'ㅈ' }, { 'ㅊ' }, { 'ㅋ' }, { 'ㅌ' }, { 'ㅍ' }, { 'ㅎ' } };
	private final static char[][] MIDDLE_LETTERS = { { 'ㅏ' }, { 'ㅐ' }, { 'ㅑ' }, { 'ㅒ' }, { 'ㅓ' }, { 'ㅔ' }, { 'ㅕ' }, { 'ㅖ' }, { 'ㅗ' }, { 'ㅗ', 'ㅏ' }, { 'ㅗ', 'ㅐ' }, { 'ㅗ', 'ㅣ' }, { 'ㅛ' }, { 'ㅜ' }, { 'ㅜ', 'ㅓ' }, { 'ㅜ', 'ㅔ' }, { 'ㅜ', 'ㅣ' }, { 'ㅠ' }, { 'ㅡ' }, { 'ㅡ', 'ㅣ' }, { 'ㅣ' } };
	private final static char[][] END_LETTERS = { {}, { 'ㄱ' }, { 'ㄱ', 'ㄱ' }, { 'ㄱ', 'ㅅ' }, { 'ㄴ' }, { 'ㄴ', 'ㅈ' }, { 'ㄴ', 'ㅎ' }, { 'ㄷ' }, { 'ㄹ' }, { 'ㄹ', 'ㄱ' }, { 'ㄹ', 'ㅁ' }, { 'ㄹ', 'ㅂ' }, { 'ㄹ', 'ㅅ' }, { 'ㄹ', 'ㅌ' }, { 'ㄹ', 'ㅍ' }, { 'ㄹ', 'ㅎ' }, { 'ㅁ' }, { 'ㅂ' }, { 'ㅂ', 'ㅅ' }, { 'ㅅ' }, { 'ㅅ', 'ㅅ' }, { 'ㅇ' }, { 'ㅈ' }, { 'ㅊ' }, { 'ㅋ' }, { 'ㅌ' }, { 'ㅍ' }, { 'ㅎ' } };
	private final static char[][] DOUBLE_LETTERS = { { 'ㄱ' }, { 'ㄱ', 'ㄱ' }, { 'ㄱ', 'ㅅ' }, { 'ㄴ' }, { 'ㄴ', 'ㅈ' }, { 'ㄴ', 'ㅎ' }, { 'ㄷ' }, { 'ㄸ' }, { 'ㄹ' }, { 'ㄹ', 'ㄱ' }, { 'ㄹ', 'ㅁ' }, { 'ㄹ', 'ㅂ' }, { 'ㄹ', 'ㅅ' }, { 'ㄹ', 'ㄷ' }, { 'ㄹ', 'ㅍ' }, { 'ㄹ', 'ㅎ' }, { 'ㅁ' }, { 'ㅂ' }, { 'ㅂ', 'ㅂ' }, { 'ㅂ', 'ㅅ' }, { 'ㅅ' }, { 'ㅅ', 'ㅅ' }, { 'ㅇ' }, { 'ㅈ' }, { 'ㅈ', 'ㅈ' }, { 'ㅊ' }, { 'ㅋ' }, { 'ㅌ' }, { 'ㅍ' }, { 'ㅎ' }, { 'ㅏ' }, { 'ㅐ' }, { 'ㅑ' }, { 'ㅒ' }, { 'ㅓ' }, { 'ㅔ' }, { 'ㅕ' }, { 'ㅖ' }, { 'ㅗ' }, { 'ㅗ', 'ㅏ' }, { 'ㅗ', 'ㅐ' }, { 'ㅗ', 'ㅣ' }, { 'ㅛ' }, { 'ㅜ' }, { 'ㅜ', 'ㅓ' }, { 'ㅜ', 'ㅔ' }, { 'ㅜ', 'ㅣ' }, { 'ㅠ' }, { 'ㅡ' }, { 'ㅡ', 'ㅣ' }, { 'ㅣ' } };

	public static String nvl(Object str, String def) {
		if (str == null) return def != null ? def : "";
		
		if (str instanceof String) {
			if (((String)str).trim().equals("")) return def != null ? def : "";
			else return ((String)str).trim();
		} else return str.toString();
	}

	public static String nvl(Object str) {
		return nvl(str, "");
	}

	public static String substring(String str, int length) {
		if (str != null && length >= 0) {
			byte[] strbyte = str.getBytes();

			if (strbyte.length <= length) return str;

			char[] charArray = str.toCharArray();
			int checkLimit = length;

			for (int i=0 ; i<charArray.length ; i++) {
				if (charArray[i] < 256) checkLimit -= 1;
				else checkLimit -= 2;

				if (checkLimit <= 0) break;
			}

			byte[] newByte = new byte[length + checkLimit];

			for (int i=0 ; i<newByte.length ; i++) newByte[i] = strbyte[i];

			return new String(newByte);
		}

		return str;
	}


	public static String rpad(String str, int length, String pad) {
		int templen = length - str.getBytes().length;

		for (int a=0; a<templen; a++) str = str + pad;

		return str;
	}

	public static String lpad(String str, int len, String pad) {
		int templen = len - str.getBytes().length;

		for (int a=0; a<templen; a++) str = pad + str;

		return str;
	}

	public static String join(String[] strSet, String needle) {
		String returnString = "";

		for (int a=0 ; a<strSet.length ; a++) {
			if (nvl(strSet[a]).equals("")) continue;
			else returnString += strSet[a] + "\t\t";
		}

		return returnString.trim().replaceAll("\t\t", needle);
	}

	public static String join(String[] strSet) {
		return join(strSet, ",");
	}
	
	public static String hostName() {
		String hostName = null;
		Process process = null;
		BufferedReader reader = null;

		try { hostName = InetAddress.getLocalHost().getHostName(); } catch (Throwable e) { }

		if (nvl(hostName).equals("")) {
			try {
				process = Runtime.getRuntime().exec("hostname");
				reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				hostName = reader.readLine();

				process.waitFor();
			} catch (Throwable e) {
				Tool.release(reader);
			}
		}

		if (nvl(hostName).equals("")) {
			try {
				for (NetworkInterface netInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
					if (netInterface == null) continue;
					if (netInterface.isLoopback()) continue;

					for (InetAddress inetAddress : Collections.list(netInterface.getInetAddresses())) {
						if (inetAddress == null) continue;
						if (hostName != null) break;

						if (!Tool.nvl(inetAddress.getHostName()).equals("")) hostName = inetAddress.getHostName();
					}
				}
			} catch (Throwable e) { }
		}

		return hostName;
	}

	public static String networkAddress() {
		try {
			for (NetworkInterface netInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				if (netInterface == null) continue;
				if (netInterface.isLoopback()) continue;

				for (InetAddress inetAddress : Collections.list(netInterface.getInetAddresses())) {
					if (inetAddress == null) continue;
					if (inetAddress.getHostAddress().matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}")) return inetAddress.getHostAddress();
				}
			}

			return InetAddress.getLocalHost().getHostAddress();
		} catch (Throwable e) { }

		return null;
	}

	public static String dateFormat(Date date, String dateForm) {
		if (date == null) return "";
		else return new SimpleDateFormat(dateForm.replaceAll("Y", "y").replaceAll("D", "d")).format(date);
	}

	public static String dateFormat(Calendar date, String dateForm) {
		if (date == null) return "";

		return dateFormat(date.getTime(), dateForm);
	}

	public static String numberFormat(long number, String format) {
		DecimalFormat decimalFormat = new DecimalFormat(format);

		return decimalFormat.format(number);
	}

	public static String numberFormat(long number) {
		return numberFormat(number, "#,###,###,###,###");
	}

	public static String numberFormat(String number) {
		number = nvl(number).replaceAll("[^0-9\\-\\.]", "");

		if (number.equals("")) return "";

		return numberFormat(Integer.parseInt(number));
	}

	public static String fileName(String fileFullPath) {
		int slash = Math.max(fileFullPath.lastIndexOf('/'), fileFullPath.lastIndexOf('\\'));

		if (slash > -1) fileFullPath = fileFullPath.substring(slash + 1);

		return fileFullPath;
	}

	public static String queryString(Map map) {
		String string = "";
		
		for (Object key : map.keySet()) {
			if (key == null) continue;
			
			try { string += URLEncoder.encode(key.toString(), Config.CHARSET); } catch (UnsupportedEncodingException e) { string += key; } finally { string += "="; }
			try { string += URLEncoder.encode(map.get(key).toString(), Config.CHARSET); } catch (UnsupportedEncodingException e) { string += map.get(key); } finally { string += "\t\t\t"; }
		}

		return string.trim().replaceAll("\t\t\t", "&");
	}

	public static String splitLetters(String text) {
		if (text == null) return "";
		if (text.length() == 0) return text;

		StringBuilder letters = new StringBuilder(text.length() * 6);

		for (char ch : text.toCharArray()) {
			if (ch >= '가' && ch <= '힣') {
				int ce = ch - '가';

				letters.append(START_LETTERS[ce / (588)]);
				letters.append(MIDDLE_LETTERS[(ce = ce % (588)) / 28]); // 21 * 28

				if ((ce = ce % 28) != 0) letters.append(END_LETTERS[ce]);
			} else if (ch >= 'ㄱ' && ch <= 'ㅣ') letters.append(DOUBLE_LETTERS[ch - 'ㄱ']);
			else letters.append(ch);
		}

		return letters.toString();
	}
	
	public static String matchValues(Map values, String value) {
		String key;
		Matcher matcher;

		if (values != null && value != null) {
			while ((matcher = VALUE_PATTERN.matcher(value)).find()) {
				try {
					key = matcher.group(1);
					value = value.replaceAll("@\\{" + key + "\\}", Tool.nvl(values.get(key)));
				} catch (IllegalArgumentException e) { }
			}
		}
		
		return value;
	}

	public static double percent(double percent) {
		return percent(percent, 2);
	}

	public static double percent(double percent, int count) {
		for (int a=0 ; a<count+2 ; a++) percent = percent * 10;

		String temp = String.valueOf(Math.round(percent));

		for (int a=0 ; a<2 ; a++) temp = String.valueOf(Math.round(Double.parseDouble(temp) / 10));

		return (count > 0 ? Double.parseDouble(temp)*(10*count) : Double.parseDouble(temp));
	}

	public static Calendar offsetMonth(String date, int month) {
		Calendar calendar = Calendar.getInstance(Locale.KOREA);
		String day = "1";

		date = date.replaceAll("[^0-9]", "");

		if (date.length() < 6) return null;
		if (date.length() > 6) day = date.substring(6);

		calendar.set(Integer.parseInt(date.substring(0, 4)), Integer.parseInt(date.substring(4, 6)) - 1, Integer.parseInt(day));
		calendar.add(Calendar.MONTH, month);

		return calendar;
	}

	public static Calendar offsetMonth(Date date, int month) {
		return offsetMonth(dateFormat(date, "yyyyMMdd"), month);
	}

	public static Calendar offsetDay(String date, int day) {
		Calendar calendar = Calendar.getInstance(Locale.KOREA);

		date = date.replaceAll("[^0-9]", "");

		if (date.length() < 8) return null;

		calendar.set(Integer.parseInt(date.substring(0, 4)), Integer.parseInt(date.substring(4, 6)) - 1, Integer.parseInt(date.substring(6)));
		calendar.add(Calendar.DATE, day);

		return calendar;
	}

	public static Calendar offsetDay(Date date, int day) {
		return offsetDay(dateFormat(date, "yyyyMMdd"), day);
	}
	
	public static Map<String, String> split(String parameter, String del1, String del2) {
		HashMap<String, String> result = new HashMap<String, String>();
		StringTokenizer tokenizer = !nvl(parameter).equals("") ? new StringTokenizer(parameter, del1) : null;

		if (tokenizer == null) return result;

		String str, key;
		int index;

		while (tokenizer.hasMoreTokens()) {
			str = tokenizer.nextToken();
			index = str.indexOf(del2);

			if (index == -1) continue;
			else key = str.substring(0, index);

			if (key != null) if (!result.containsKey(key = key.trim())) result.put(key, str.substring(index + 1, str.length()).replaceAll("%20", " ").replaceAll("%3D", "=").replaceAll("%25", "%").replaceAll("%26", "&"));
		}

		return result;
	}

	public static Map copyMap(Map src, Map dst, boolean overwrite, boolean collection) {
		if (src == null) return dst;

		if (dst != null) {
			for (Object key : src.keySet()) {
				if (!overwrite) if (dst.containsKey(key)) continue;

				if (!collection && src.get(key) != null) {
					if (src.get(key) instanceof Map) continue;
					else if (src.get(key) instanceof List) continue;
				}

				dst.put(key, src.get(key));
			}
		}

		return dst;
	}

	public static Map parseUrl(String url) {
		Map parsedInfo = new HashMap();
		String tempString = "";

		parsedInfo.put("url", url);
		parsedInfo.put("protocol", url.substring(0, url.indexOf("://")));

		tempString = url.substring(url.indexOf("://")+3, url.indexOf("/", url.indexOf("://")+3));

		if (tempString.indexOf("@") > 0) tempString = tempString.substring(tempString.indexOf("@")+1);

		if (tempString.indexOf(":") > 0) {
			parsedInfo.put("host", tempString.substring(0, tempString.indexOf(":")));
			parsedInfo.put("port", tempString.substring(tempString.indexOf(":")));
		} else {
			parsedInfo.put("host", tempString);
			parsedInfo.put("port", "80");
		}

		if (url.indexOf("?") > 0) parsedInfo.put("query", url.substring(url.indexOf("?")+1, url.length()));

		parsedInfo.put("path", url.substring(url.indexOf("/", url.indexOf("://")+3)).replaceAll(Tool.nvl((String)parsedInfo.get("query")), "").replaceAll("\\?", ""));

		return parsedInfo;
	}

	public static String[] stringArray(Object object) throws IllegalArgumentException {
		Object[] objects = null;
		String[] values = null;

		if (object instanceof String[]) return (String[])object;
		if (object instanceof Map) objects = ((Map)object).values().toArray();
		if (object instanceof Set) objects = ((Set)object).toArray();
		if (object instanceof List) objects = ((List)object).toArray();

		if (objects != null) {
			values = new String[objects.length];

			for (int a=0 ; a<objects.length ; a++) values[a] = (String)objects[a];
		} else {
			throw new IllegalArgumentException("Not supported object type");
		}

		return values;
	}

	public static List[] parseMap(Map map) {
		List[] returnValue = { new ArrayList(), new ArrayList() };
		Iterator keys = map.keySet().iterator();

		while (keys.hasNext()) {
			Object key = keys.next();

			returnValue[0].add(key);
			returnValue[1].add(map.get(key));
		}

		return returnValue;
	}
	
	public static boolean usable(Object object) {
		if (object == null) return false;
		
		if (object instanceof String) return !((String)object).equals("");
		if (object instanceof String[]) return ((String[])object).length > 0;
		if (object instanceof Response) return ((Response)object).code() < 400;
		if (object instanceof Collection) return ((Collection)object).size() > 0;

		return true;
	}

	public static void release(Object ... objects) {
		if (objects == null) return;

		for (Object object : objects) {
			if (object == null) continue;

			try {
				if (object instanceof Closeable) { ((Closeable)object).close(); return; }
				if (object instanceof AutoCloseable) { ((AutoCloseable)object).close(); return; }
	
				if (object instanceof Map) { ((Map)object).clear(); return; }
				if (object instanceof ByteBuffer) { ((ByteBuffer)object).clear(); return; }
	
				if (object instanceof Process) { ((Process)object).destroy(); return; }
				if (object instanceof HttpURLConnection) { ((HttpURLConnection)object).disconnect(); return; }
				
				if (object.getClass().getMethod("close") != null) object.getClass().getMethod("close").invoke(object);
			} catch (Exception e) {}
		}
	}

	public static void touch(File file) throws FileNotFoundException, IOException {
		new FileOutputStream(file).close();
		file.setLastModified(System.currentTimeMillis());
	}
	
	public static void readStream(InputStream input, OutputStream output) throws IOException {
		readStream(input, output, null, null);
	}

	public static void readStream(InputStream input, OutputStream output, byte[] prefix, byte[] suffix) throws IOException {
		byte[] buffer = new byte[2048];
		int readed = -1;
		
		if (prefix != null && prefix.length > 0) output.write(prefix);

		try { while ((readed = input.read(buffer)) != -1) output.write(buffer, 0, readed); } finally { buffer = null; }
		
		if (suffix != null && suffix.length > 0) output.write(suffix);
	}

	public static void writeStream(InputStream input, String path) throws IOException {
		writeStream(input, path, null, null);
	}

	public static void writeStream(InputStream input, String path, byte[] prefix, byte[] suffix) throws IOException {
		File file = null;
		FileOutputStream fos = null;
		
		try {
			if ((file = new File(path)).exists()) file.delete();
			else if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
			
			readStream(input, fos = new FileOutputStream(file), prefix, suffix);
		} finally { release(fos); }
	}
}