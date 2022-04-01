package com.eliall.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.JsonNodeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class JSON {
	private static final ObjectMapper mapper = new ObjectMapper();
	
	static {
		final SimpleModule module = new SimpleModule();

        module.addSerializer(BigInteger.class, new ToStringSerializer());
        module.addSerializer(BigDecimal.class, new ToStringSerializer());
        
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		
		mapper.setVisibility(PropertyAccessor.SETTER, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

        mapper.registerModule(module);
	}
	
	public static Map<String, Object> stringToMap(String json) {
		JsonFactory jsonFactory = new JsonFactory();

		try {
			return mapper.readValue(jsonFactory.createParser(json), new TypeReference<HashMap<String, Object>>() {});
		} catch (Throwable e) { }
		
		return null;
	}
	
	public static Map<String, Object> streamToMap(InputStream json) {
		JsonFactory jsonFactory = new JsonFactory();

		try {
			return mapper.readValue(jsonFactory.createParser(json), new TypeReference<HashMap<String, Object>>() {});
		} catch (Throwable e) { }
		
		return null;
	}
	
	public static Map<String, Object> bytesToMap(byte[] bytes) {
		JsonFactory jsonFactory = new JsonFactory();

		try {
			return mapper.readValue(jsonFactory.createParser(bytes), new TypeReference<HashMap<String, Object>>() {});
		} catch (Exception e) { }
		
		return null;
	}
	
	public static List<Object> stringToList(String json) {
		JsonFactory jsonFactory = new JsonFactory();

		try {
			return mapper.readValue(jsonFactory.createParser(json), new TypeReference<ArrayList<Object>>() {});
		} catch (Throwable e) { }
		
		return null;
	}
	
	public static List<Object> streamToList(InputStream json) {
		JsonFactory jsonFactory = new JsonFactory();

		try {
			return mapper.readValue(jsonFactory.createParser(json), new TypeReference<ArrayList<Object>>() {});
		} catch (Throwable e) { }
		
		return null;
	}
	
	public static List<Object> bytesToList(byte[] bytes) {
		JsonFactory jsonFactory = new JsonFactory();

		try {
			return mapper.readValue(jsonFactory.createParser(bytes), new TypeReference<ArrayList<Object>>() {});
		} catch (Exception e) { }
		
		return null;
	}
	
	public static <T> T stringToObject(String value, Class<T> type, String charset) throws IOException  {
		return bytesToObject(value.getBytes(charset), type);
	}
	
	public static <T> T stringToObject(String value, Class<T> type) throws IOException  {
		return stringToObject(value, type, "UTF-8");
	}
	
	public static <T> T bytesToObject(byte[] bytes, Class<T> type) throws IOException {
		return mapper.readValue(bytes, type);
	}
	
	public static <T> T fileToObject(File file, Class<T> type) throws IOException  {
		return mapper.readValue(file, type);
	}
	
	public static <T> T streamToObject(InputStream is, Class<T> type) throws IOException  {
		return mapper.readValue(is, type);
	}
	
	public static String objectToString(Object object) {
		return objectToString(object, false);
	}
	
	public static String objectToString(Object object, boolean pretty) {
		try {
			if (!pretty) return mapper.writeValueAsString(object);
			else return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
		} catch (Throwable e) { }
		
		return null;
	}
	
	public static byte[] objectToBytes(Object object) throws IOException {
		return mapper.writeValueAsBytes(object);
	}
	
	public static void objectToFile(File file, Object object) throws IOException {
		mapper.writeValue(file, object);
	}
	
	public static void objectToStream(OutputStream os, Object object) throws IOException {
		mapper.writeValue(os, object);
	}
	
	public static String xmlToJson(String xml) {
		XmlMapper xmlMapper = null;

		try { 
			(xmlMapper = new XmlMapper()).registerModule(new SimpleModule().addDeserializer(JsonNode.class, new DuplicateToArrayJsonNodeDeserializer()));

			return mapper.writeValueAsString(xmlMapper.readTree(xml));
		} catch (Throwable e) { return null; }
	}
	
	public static class DuplicateToArrayJsonNodeDeserializer extends JsonNodeDeserializer {
		protected void _handleDuplicateField(JsonParser p, DeserializationContext ctxt, JsonNodeFactory nodeFactory,String fieldName, ObjectNode objectNode, JsonNode oldValue, JsonNode newValue) throws JsonProcessingException {
			ArrayNode node;
			
			if (oldValue instanceof ArrayNode) node = ((ArrayNode)oldValue).add(newValue);
			else node = nodeFactory.arrayNode().add(oldValue).add(newValue);
			
			objectNode.set(fieldName, node);
	    }
	}
}