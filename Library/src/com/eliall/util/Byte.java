package com.eliall.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Byte {
	public static void writeBytes(byte[] src, String path) throws IOException {
		ByteArrayInputStream bis = null;
		
		try { Tool.writeStream(bis = new ByteArrayInputStream(src), path, null, null); } finally { Tool.release(bis); }
	}

	public static byte[] copy(byte[] src) {
		return copy(src, 0, src.length);
	}
	
	public static byte[] copy(byte[] src, int offset) {
		return copy(src, offset, src.length - offset);
	}
	
	public static byte[] copy(byte[] src, int offset, int length) {
		byte[] newBytes = new byte[length];

		System.arraycopy(src, offset, newBytes, 0, length);

		return newBytes;
	}
	
	public static byte[] merge(byte[] ... bytes) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] value = null;
		
		try {
			for (int a=0 ; a<bytes.length ; a++) output.write(bytes[a]);

			if (output != null) value = output.toByteArray();
		} finally { Tool.release(output); }
		
		return value;
	}
	
	public static byte[] merge(byte[] base, byte[] paste, int offset) throws IOException {
		return merge(base, paste, offset, paste.length - offset);
	}

	public static byte[] merge(byte[] base, byte[] paste, int offset, int length) throws IOException {
		ByteArrayInputStream input = new ByteArrayInputStream(paste);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = null, value = null;
		
		try {
			if (offset > 0) input.skip(offset);
			if (length > 0) input.read(buffer = new byte[length]);
	
			if (base != null) output.write(base);
			if (buffer != null) output.write(buffer);
			
			value = output.toByteArray();
		} finally { Tool.release(output, input); buffer = null; }

		return value;
	}
	
	public static byte[] fromInt(int value) {
		byte[] bytes = new byte[4];

		ByteBuffer.wrap(bytes).putInt(value);

		return bytes;
	}
	
	public static byte[] fromLong(long value) {
		byte[] bytes = new byte[8];

		ByteBuffer.wrap(bytes).putLong(value);

		return bytes;
	}

	public static byte[] fromDouble(double value) {
		byte[] bytes = new byte[8];

		ByteBuffer.wrap(bytes).putDouble(value);

		return bytes;
	}

	public static int toInt(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getInt();
	}
	
	public static long toLong(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getLong();
	}

	public static double toDouble(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getDouble();
	}
}
