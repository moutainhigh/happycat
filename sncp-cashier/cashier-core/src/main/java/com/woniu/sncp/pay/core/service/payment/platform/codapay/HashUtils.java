package com.woniu.sncp.pay.core.service.payment.platform.codapay;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class HashUtils {
	private static BASE64Encoder encoder = new BASE64Encoder();
	private static BASE64Decoder decoder = new BASE64Decoder();

    public static String convertToHex(byte[] data) { 
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) { 
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do { 
				if ((0 <= halfbyte) && (halfbyte <= 9)) 
					buf.append((char) ('0' + halfbyte));
				else 
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while(two_halfs++ < 1);
		}

		return buf.toString();
    } 

    public static byte[] MD5(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException  {
    	if (null != text) {
    		MessageDigest md =  MessageDigest.getInstance("MD5");
    		md.update(text.getBytes(), 0, text.length());
    		return md.digest();
    	}

    	return null;
    }

    public static byte[] SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException  {
		if (null != text) {
			MessageDigest md =  MessageDigest.getInstance("SHA-1");
			md.update(text.getBytes(), 0, text.length());
			return md.digest();
		}
		
		return null;
	}

    public static String base64encode(byte[] data) {
    	return encoder.encode(data);
    }

    public static String encode(String data) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    	byte[] byteDate = SHA1(data);
    	return encoder.encode(byteDate);
    }

    public static byte[] base64decode(String data) throws IOException {
    	return decoder.decodeBuffer(data);
    }

    public static void main(String[] args) {
		try {
			System.out.println("SHA1 : " + convertToHex(SHA1("1234")));
		    System.out.println("SHA1 : " + base64encode(SHA1("1234")));
			System.out.println("MD5 : " + convertToHex(MD5("1234")));
		    System.out.println("MD5 : " + base64encode(MD5("1234")));
		} catch (NoSuchAlgorithmException e) {
		    e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
		    e.printStackTrace();
		}
    }
}