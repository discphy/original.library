package com.eliall.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eliall.util.JSON;

@SuppressWarnings({"rawtypes","unchecked"})
public class EliList extends ArrayList<Object> {
	public EliList() { super(); }

	public EliList(Object object) {
		this();
		
		if (object == null) return;

		if (object instanceof Collection) super.addAll((Collection)object);
		else if (object instanceof String && ((String)object).trim().startsWith("[") && ((String)object).trim().endsWith("]")) super.addAll(JSON.stringToList((String)object));
		else add(object);
	}

	public void clean() {
		for (Object value : this.toArray()) if (value == null || (value instanceof String && value.equals("null"))) remove(value);
	}
	
	public EliList rebuild() {
		for (int index=0 ; index<this.size() ; index++) {
			Object object = get(index);
			
			if (object == null) continue;
			else if (object instanceof Map) set(index, new EliObject(object).rebuild());
			else if (object instanceof List) set(index, new EliList(object).rebuild());
			else if (object instanceof String) {
				if (((String)object).trim().startsWith("{") && ((String)object).trim().endsWith("}")) set(index, new EliObject(JSON.stringToMap((String)object)).rebuild());
				else if (((String)object).trim().startsWith("[") && ((String)object).trim().endsWith("]")) set(index, new EliList(JSON.stringToList((String)object)).rebuild());
			}
		}
		
		return this;
	}
	
	public EliList insert(Object val) {
		add(val); return this;
	}

	public EliList insert(int index, Object val) {
		add(index, val); return this;
	}

	public String toString() {
		return JSON.objectToString(this);
	}
	
	public String toString(boolean pretty) {
		return JSON.objectToString(this, pretty);
	}
	
	public EliObject getObject(int index) {
		Object object = get(index);
		
		if (object instanceof EliObject) return (EliObject)object;
		else if (object instanceof Map) return new EliObject((Map)object);
		
		return null;
	}
	
	public EliList getList(int index) {
		Object object = get(index);
		
		if (object instanceof EliList) return (EliList)object;
		else if (object instanceof Collection) return new EliList((Collection)object);
		
		return null;
	}
	
	public int getInt(int index) {
		return getInt(index, 0);
	}

	public int getInt(int index , int def) {
		return getNumber(index, Integer.valueOf(def)).intValue();
	}

	public long getLong(int index) {
		return getLong(index, 0);
	}

	public long getLong(int index, long def) {
		return getNumber(index, Long.valueOf(def)).longValue();
	}
	
	public float getFloat(int index) {
		return getFloat(index, 0);
	}

	public float getFloat(int index, float def) {
		return getNumber(index, Float.valueOf(def)).floatValue();
	}

	public double getDouble(int index) {
		return getDouble(index, 0);
	}

	public double getDouble(int index, double def) {
		return getNumber(index, Double.valueOf(def)).doubleValue();
	}

	public String getString(int index) {
		Object object = get(index);
		
		if (object == null) return null;
		
		return object.toString();
	}

	public String getString(int index, final String def) {
		Object object = get(index);
		
		if (object == null) return def;

		return object.toString();
	}
	
	public Date getDate(int index, final Date def) {
		final Object object = get(index);
		
		return (object != null) ? (Date)object : def;
	}

	public boolean getBoolean(int index) {
		return getBoolean(index, false);
	}

	public boolean getBoolean(int index , boolean def) {
		Object object = get(index);
		
		if (object == null) return def;
		if (object instanceof Number) return ((Number)object).intValue() > 0;
		if (object instanceof Boolean) return ((Boolean)object).booleanValue();
		
		throw new IllegalArgumentException("can't coerce to bool:" + object.getClass());
	}

	private Number getNumber(int index, Number def) {
		Object object = get(index);
		Number number = null;
		
		if (object != null) {
			if (object instanceof Boolean) return Integer.valueOf(((Boolean)object).booleanValue() ? 1 : 0);
			
			if (object instanceof Number) number = (Number)object;
			else if (object instanceof String) number = Double.valueOf((String)object);
		} else number = def;
		
		return number;
	}
}