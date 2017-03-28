package com.woniu.sncp.pay.core.service.payment.platform.cmpay.tools;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 
 * @version version2.0.0
 * 
 * */

public class CryptUtilImpl {

	/**
	 * 
	 * MD5加密.
	 * @param source 需要加密的报文，key 加密密钥
	 * @return 加密后的消息摘要
	 * 
	 * */
	
	public String cryptMd5(String source, String key) {
		byte[] keyb;
		byte[] value;
		byte[] k_ipad = new byte[64];
		byte[] k_opad = new byte[64];
		try {
			keyb = key.getBytes("UTF-8");
			value = source.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			keyb = key.getBytes();
			value = source.getBytes();
		}
		Arrays.fill(k_ipad, keyb.length, 64, (new Integer(54)).byteValue());
		Arrays.fill(k_opad, keyb.length, 64, (new Integer(92).byteValue()));
		for (int i = 0; i < keyb.length; ++i) {
			k_ipad[i] = (byte) (keyb[i] ^ 0x36);
			k_opad[i] = (byte) (keyb[i] ^ 0x5C);
		}
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		md.update(k_ipad);
		md.update(value);
		byte[] dg = md.digest();
		md.reset();
		md.update(k_opad);
		md.update(dg, 0, 16);
		dg = md.digest();
		return toHex(dg);
	}

	/**
	 * 
	 * 
	 * */
	
	public static String toHex(byte[] input) {
		if (input == null)
			return null;

		StringBuffer output = new StringBuffer(input.length * 2);
		for (int i = 0; i < input.length; ++i) {
			int current = input[i] & 0xFF;
			if (current < 16)
				output.append("0");
			output.append(Integer.toString(current, 16));
		}

		return output.toString();
	}

	public static void main(String[] args) {
		String source = "888009941120001201711020004310794000000SUCCESSMD52.0.0200CNY_AMT=200#CMY_AMT=0#RED_AMT=0#VCH_AMT=0#POT_CHG_AMT=0135****94072011021616401720171102163733SUCCESS";
		String key = "8Fb5j7RAYj4Dw3s6bZ11hVynY9oyx0LHdTz3Xww8xmNsfzGd1qABCerSz0ZRXMA3";
		CryptUtilImpl impl = new CryptUtilImpl();
        String md5 = impl.cryptMd5(source, "");
		md5 = impl.cryptMd5(md5, key);
		System.out.println(md5);
	}
}