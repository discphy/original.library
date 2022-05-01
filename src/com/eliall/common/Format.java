package com.eliall.common;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.eliall.definition.Regexes;
import com.eliall.util.Tool;

public class Format {
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd"), GMT_FORMAT1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), GMT_FORMAT2 = new SimpleDateFormat("yyyy.MM.dd HH:mm");
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.########");

	public static String age(long time) {
		StringBuffer buffer = new StringBuffer();
		int age = (int)((System.currentTimeMillis() - time) / 1000), day = age / 86400, hour = age / 3600 % 24, minute = age % 3600 / 60, second = age % 60;
		
		if (day > 0) buffer.append(day).append("d");
		if (hour > 0) buffer.append(hour).append("h");
		if (minute > 0) buffer.append(minute).append("m");
		if (second > 0) buffer.append(second).append("s");

		return buffer.toString().replaceAll("([a-z]+)", "$1 ").trim();
	}
	
	public static String left(int time) {
		int hour = time / 3600, minute = (time - hour * 3600) / 60, second = time % 60;
		
		return hour + ":" + Tool.lpad(minute + "", 2, "0") + ":" + Tool.lpad(second + "", 2, "0");
	}

	public static String date(long time) {
		return DATE_FORMAT.format(new Date(time));
	}
	
	public static String time(long time, boolean seconds) {
		return (seconds ? GMT_FORMAT1 : GMT_FORMAT2).format(new Date(time));
	}
	
	public static String phone(String phone) {
		return phone(phone, false);
	}
	
	public static String phone(String phone, boolean numbers) {
		return phone != null ? phone(phone, numbers ? "" : "-") : null;
	}
	
	public static String phone(String phone, String seperator) {
		return phone.replaceFirst(Regexes.PHONE_NO, "$1" + seperator + "$2" + seperator + "$3");
	}

	public static String quantity(BigDecimal decimal) {
		return DECIMAL_FORMAT.format(decimal);
	}
	
	public static String quantity(long quantity) {
		return DECIMAL_FORMAT.format(quantity);
	}
	
	public static String quantity(int quantity) {
		return DECIMAL_FORMAT.format(quantity);
	}
}