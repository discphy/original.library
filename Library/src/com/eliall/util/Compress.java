package com.eliall.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipOutputStream;

public class Compress {
	public static void compress(String path, Type type) throws IOException {
		OutputStream output = null;
		FileInputStream fin = null;
		FileOutputStream fout = null;

		byte[] buffer = new byte[2048];
		int readed = 0;
		
		try {
			fin = new FileInputStream(path);
			fout = new FileOutputStream(path + "." + type.extension());
			
			if (type.compareTo(Type.ZIP) == 0) output = new ZipOutputStream(fout);
			else if (type.compareTo(Type.GZIP) == 0) output = new GZIPOutputStream(fout);
			else output = fout;
			
			while ((readed = fin.read(buffer, 0, buffer.length)) != -1) output.write(buffer, 0, readed);
		} finally {
			Tool.release(output);
			Tool.release(fout);
			Tool.release(fin);
		}
	}

	public static byte[] compress(String value, String charset) throws IOException {
		ByteArrayOutputStream baos = null;
		DeflaterOutputStream out = null;
        
		try {
			baos = new ByteArrayOutputStream();
			out = new DeflaterOutputStream(baos, new Deflater(Deflater.BEST_COMPRESSION));

			out.write(value.getBytes(charset));
			out.finish();
			
			return baos.toByteArray();
        } finally {
        	Tool.release(baos);
        	Tool.release(out);
        }
	}
	
	public static String decompress(byte[] bytes, String charset) throws IOException {
		InputStream in = null;
		ByteArrayOutputStream baos = null;
		
		byte[] buffer = new byte[2048];
		int readed = -1;

        try {
        	in = new InflaterInputStream(new ByteArrayInputStream(bytes));
        	baos = new ByteArrayOutputStream();

            while ((readed = in.read(buffer)) > 0) baos.write(buffer, 0, readed); baos.flush();
            
            return new String(baos.toByteArray(), charset);
        } finally {
        	Tool.release(baos);
        	Tool.release(in);
        }
	}
	
	public static String encode(String value, String charset) throws IOException {
        StringBuffer sb = new StringBuffer();
        byte[]bytes = compress(value, charset);
        
        for (byte b : bytes) sb.append(String.format("%02X", b));
 
        return sb != null ? sb.toString() : null;
	}
	
	public static String decode(String value, String charset) throws IOException {
		byte[] bytes = null;
		 
        if (value == null || value.length() % 2 != 0) bytes = new byte[] { };
        else bytes = new byte[value.length() / 2];
        
        for (int a=0 ; a<value.length() ; a+=2) bytes[(int)Math.floor(a / 2)] = (byte)Integer.parseInt(value.substring(a, a + 2), 16);
        
        return decompress(bytes, charset);
	}
	
	public enum Type {
		ZIP, GZIP;
		
		public String extension() {
			if (this.compareTo(GZIP) == 0) return "gz";
			if (this.compareTo(ZIP) == 0) return "zip";
			
			return "tmp";
		}
	}
}
