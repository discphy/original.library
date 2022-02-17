package com.eliall.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.eliall.common.Config;

public class Security {
	private static final SecureRandom random = new SecureRandom();
	private static final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final String number = "0123456789";
	private static final int iterations = 16384, length = 256;
	
	public static String md5(byte[] data) throws NoSuchAlgorithmException {
		return hash(data, "MD5");
	}
	
	public static String hash(byte[] data) throws NoSuchAlgorithmException {
		return hash(data, "SHA-256");
	}
	
	public static String sha512(byte[] data) throws NoSuchAlgorithmException {
		return hash(data, "SHA-512");
	}
	
	public static String salt(int size) {
		return salt(size, false);
	}
	
	public static String salt(int size, boolean numeric) {
		StringBuilder salt = new StringBuilder(size);
    	
		for (int a=0 ; a<size ; a++) salt.append((numeric ? number : alphabet).charAt(random.nextInt((numeric ? number : alphabet).length())));

		return salt.toString();
	}
	
	public static String password(String user, String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
		char[] chars = password.toCharArray();
		PBEKeySpec spec = new PBEKeySpec(chars, user.getBytes(), iterations, length);

		Arrays.fill(chars, Character.MIN_VALUE);

		try { return Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded()); } finally { spec.clearPassword(); }
	}
    
	public static boolean equals(String user, String password, String secured) {
		try { return password(user, password).equalsIgnoreCase(secured); } catch (Throwable e) { return false; }
    }
	
	public static void encrypt(InputStream data, OutputStream result, String key, String password) throws IOException, GeneralSecurityException {
		aes128(Cipher.ENCRYPT_MODE, data, result, key, password);
	}

	public static void decrypt(InputStream data, OutputStream result, String key, String password) throws IOException, GeneralSecurityException {
		aes128(Cipher.DECRYPT_MODE, data, result, key, password);
	}
	
	public static String encrypt(String source) throws IOException, GeneralSecurityException {
		return encrypt(source.getBytes());
	}
	
	public static String encrypt(byte[] bytes) throws IOException, GeneralSecurityException {
		return Base64.getEncoder().encodeToString(Security.encrypt(bytes, Config.SEC_KEY, Config.SEC_PASS));
	}
	
	public static byte[] encrypt(byte[] bytes, boolean base64) throws IOException, GeneralSecurityException {
		byte[] result = Security.encrypt(bytes, Config.SEC_KEY, Config.SEC_PASS);
		
		if (base64) result = Base64.getEncoder().encode(result);
		
		return result;
	}

	public static byte[] encrypt(byte[] data, String key, String password) throws IOException, GeneralSecurityException {
		return aes128(Cipher.ENCRYPT_MODE, data, key, password);
	}

	public static byte[] decrypt(byte[] data, String key, String password) throws IOException, GeneralSecurityException {
		return aes128(Cipher.DECRYPT_MODE, data, key, password);
	}
	
	public static String decrypt(byte[] bytes) throws UnsupportedEncodingException, IOException, GeneralSecurityException {
		return new String(decrypt(bytes, Config.SEC_KEY, Config.SEC_PASS), Config.CHARSET);
	}
	
	public static String decrypt(String source) throws UnsupportedEncodingException, IOException, GeneralSecurityException {
		return decrypt(Base64.getDecoder().decode(source));
	}
	
	private static String hash(byte[] data, String type) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance(type.toUpperCase()); // digest.update(data);
		StringBuffer result = new StringBuffer();
		byte[] bytes = digest.digest(data);

		for (int a=0 ; a<bytes.length ; a++) {
			String hex = Integer.toHexString(0xff & bytes[a]);

			result.append(hex.length() == 1 ? '0' + hex : hex);
		}

		return result.toString();
	}
	
	private static byte[] aes128(int mode, byte[] data, String key, String password) throws IOException, GeneralSecurityException {
		ByteArrayInputStream input = null;
		ByteArrayOutputStream output = null;
		
		try {
			input = new ByteArrayInputStream(data);
			output = new ByteArrayOutputStream();

			aes128(mode, input, output, key, password);

			return output.toByteArray();
		} finally {
			Tool.release(output);
			Tool.release(input);
		}
	}
	
	private static void aes128(int mode, InputStream data, OutputStream result, String key, String password) throws IOException, GeneralSecurityException {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec paramSepc = new IvParameterSpec(Tool.rpad(key, 16, " ").substring(0, 16).getBytes());
		SecretKeySpec aesSpec = new SecretKeySpec(Tool.rpad(password, 16, " ").substring(0, 16).getBytes(), "AES");

        CipherInputStream input = null;
        CipherOutputStream output = null;
        
        byte[] buffer = new byte[2048];
        int readed = -1;

		cipher.init(mode, aesSpec, paramSepc);
		
		try {
			if (mode == Cipher.ENCRYPT_MODE && (output = new CipherOutputStream(result, cipher))  != null) while ((readed = data.read(buffer, 0, buffer.length)) != -1) output.write(buffer, 0, readed);
			else if (mode == Cipher.DECRYPT_MODE && (input = new CipherInputStream(data, cipher)) != null) while ((readed = input.read(buffer, 0, buffer.length)) != -1) result.write(buffer, 0, readed);
		} finally {
			if (input != data) Tool.release(input);
			if (output != result) Tool.release(output);
		}
	}
}
