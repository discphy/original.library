package com.eliall.definition;

import java.util.regex.Pattern;

public interface Regexes {
	public final static Pattern BLANK_SCRIPTS = Pattern.compile("<script[ \t\r\n]*(type=['\"]?[^'\"]*['\"]?)?[ \t\r\n]*>[ \t\r\n]*</script>", Pattern.CASE_INSENSITIVE);

	public final static String PHONE_NO = "([0-9]{2,3})-?([0-9]{3,4})-?([0-9]{4})";
	public final static String DATE_YMD = "([0-9]{4})[,-_/]?([0-9]{2})[,-_/]?([0-9]{2})";
}