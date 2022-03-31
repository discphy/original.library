package com.eliall.common;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eliall.util.JSON;

@SuppressWarnings({"rawtypes","unchecked"})
public class EliObject extends HashMap<String, Object> {
	public static String toString(Object object) { return JSON.objectToString(object, false); }
	public static String toString(Object object, boolean pretty) { return JSON.objectToString(object, pretty); }
	
	public static EliObject object(Object object) { return object != null ? instance(object) : null; }
	public static EliObject instance(Object object) { return object instanceof EliObject ? (EliObject)object : new EliObject(object); }
	
	public EliObject() { super(); }
	public EliObject(String key, Object value) { put(key, value); }

	public EliObject(Object object) {
		this();
		
		if (object == null) return;
		else if (object instanceof String) object = JSON.stringToMap((String)object);
		
		if (object == null) return;
		else if (object instanceof Map) super.putAll((Map)object);
		else if (object instanceof EliObject) super.putAll((EliObject)object);
	}

	public void putAll(Map object) {
		Map newResult = new HashMap();
		Set keys = object.keySet();
		
		for (Object key : keys) if (object.get(key) != null) newResult.put(key, object.get(key));
		
		super.putAll(newResult);
	}
	
	public Object put(String key , Object value) {
		if (value == null) return null;
		else return super.put(key, value);
	}
	
	public Object search(String key) {
		if (key != null) {
			String keys[] = ((String)key).split("[>]+");
			Object value = get(keys[0]);
			
			if (value != null) for (int a=1 ; a<keys.length ; a++) {
				if (value == null) return null;
				else if (value instanceof Map) value = ((Map)value).get(keys[a]);
			}
			
			return value;
		} else return null;
	}
	
	public EliObject rebuild() {
		for (String key : keySet()) {
			Object object = get(key);
			
			if (object == null) continue;
			else if (object instanceof Map) put(key, new EliObject((Map)object).rebuild());
			else if (object instanceof List) put(key, new EliList((List)object).rebuild());
		}
		
		return this;
	}
	
	public EliObject clone() { return new EliObject(this); }
	
	public EliObject clean() {
		for (String key : keySet()) {
			Object value = get(key);

			if (value == null || (value instanceof String && value.equals("null"))) remove(key);
		}

		return this;
	}

	public EliObject putAll(Map object, boolean overwrite) {
		if (object != null)  {
			if (overwrite) {
				putAll(object);
			} else {
				for (Object keyObject : object.keySet()) {
					String key = keyObject instanceof String ? (String)keyObject : null;
					Object value = key != null ? object.get(key) : null;
					
					if (containsKey(key)) continue;
					if (value != null) put(key, value);
				}
			}
		}
		
		return this;
	}

	public EliObject set(String key, Object val) {
		put(key, val); return this;
	}
	
	public EliObject delete(String ... keys) {
		for (String part : keys) for (String key : part.split("[ ,\t\r\n/]+")) remove(key); return this;
	}

	public Map toMap() {
		return new HashMap<String, Object>(this);
	}
	
	public String toString() {
		return JSON.objectToString(this);
	}
	
	public String toString(boolean pretty) {
		return JSON.objectToString(this, pretty);
	}
	
	public EliObject getObject(String key) {
		Object object = get(key);
		
		if (object != null) {
			if (object instanceof EliObject) return (EliObject)object;
			if (object instanceof String || object instanceof Map) return new EliObject(object);
		}
		
		return null;
	}
	
	public List getList(String key) {
		Object object = get(key);
		
		if (object != null) {
			if (object instanceof List) return (List)object;
			else if (object instanceof String) return JSON.stringToList((String)object);
			else if (object instanceof Collection) return new ArrayList((Collection)object);
		}
		
		return null;
	}
	
	public EliList getList(String key, boolean nullable) {
		Object object = null;
		
		if ((object = get(key)) != null) return object instanceof EliList ? (EliList)object : new EliList(object);

		return nullable ? null : new EliList();
	}

	public EliList getItems(String ... keys) {
		if (keys != null) {
			if (keys.length == 1) return getList(keys[0], true);

			if (keys.length > 1) {
				EliList list = new EliList();
				Object object = null;

				for (String key : keys) {
					if (key == null) continue;
					else if ((object = get(key)) != null) list.add(object);
				}

				return list;
			}
		}

		return null;
	}
	
	public int getInt(String key) {
		return getInt(key, 0);
	}

	public int getInt(String key, int def) {
		return getNumber(key, Integer.valueOf(def)).intValue();
	}

	public long getLong(String key) {
		return getLong(key, 0);
	}

	public long getLong(String key, long def) {
		return getNumber(key, Long.valueOf(def)).longValue();
	}
	
	public float getFloat(String key) {
		return getFloat(key, 0);
	}

	public float getFloat(String key, float def) {
		return getNumber(key, Float.valueOf(def)).floatValue();
	}

	public double getDouble(String key) {
		return getDouble(key, 0);
	}

	public double getDouble(String key, double def) {
		return getNumber(key, Double.valueOf(def)).doubleValue();
	}
	
	public BigDecimal getDecimal(String key) {
		return getDecimal(key, "0");
	}
	
	public BigDecimal getDecimal(String key, String def) {
		Object object = get(key);
		
		return new BigDecimal(object != null ? String.valueOf(object).replaceAll("[^0-9\\-.]+", "") : def);
	}

	public String getString(String key) {
		Object object = get(key);
		
		if (object == null) return null;
		
		return object.toString();
	}

	public String getString(String key, final String def) {
		Object object = get(key);
		
		if (object == null) return def;

		return object.toString();
	}

	public String getStrings(String needle, String ... keys) {
		StringBuffer strings = new StringBuffer();
		
		for (String key : keys) strings.append(getString(key, "")).append(needle);

		return strings.toString().substring(0, strings.length() - needle.length());
	}
	
	public Date getDate(String field, final Date def) {
		final Object object = get(field);
		
		if (object != null) {
			if (object instanceof Date) return (Date)object;
			else if (object instanceof Calendar) return ((Calendar)object).getTime();
		}
		
		return def;
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public boolean getBoolean(String key, boolean def) {
		Object object = get(key);
		String string = null;
		
		if (object == null) return def;
		else if (object instanceof String) string = ((String)object).trim().toLowerCase();

		if (string != null) return string.equals("y") || string.equals("1") || string.equals("t") || string.equals("true");

		if (object instanceof Number) return ((Number)object).intValue() > 0;
		if (object instanceof Boolean) return ((Boolean)object).booleanValue();
		
		throw new IllegalArgumentException("can't coerce to bool:" + object.getClass());
	}

	private Number getNumber(String key, Number def) {
		Object object = get(key);
		Number number = null;
		
		try {
			if (object == null) throw new Exception("object is null");
			
			if (object instanceof String) {
				if (((String)object).trim().equals("")) throw new Exception("object is blank");

				object = ((String)object).replaceAll("[^0-9\\-.]+", "");
				number = new BigDecimal((String)object);
			}
			
			if (object instanceof Boolean) return Integer.valueOf(((Boolean)object).booleanValue() ? 1 : 0);
			else if (object instanceof Calendar) object = ((Calendar)object).getTime();

			if (object instanceof Number) number = (Number)object;
			else if (object instanceof Date) number = ((Date)object).getTime();
			else if (object instanceof Timestamp) number = ((Timestamp)object).getTime();
		} catch (Throwable e) { number = def; }
		
		return number;
	}
}