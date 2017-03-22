package com.woniu.sncp.pay.common.utils.encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Aes implements Encryption{
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static final String NAME = "AES";
	
	private String vector;

	public void setVector(String vector) {
		this.vector = vector;
	}

	@Override
	public String encrypt(String data, String key, String encode)
			throws Exception {
		try {
			if (StringUtils.isBlank(key) || StringUtils.length(key) != 16)
				return null;

			byte[] raw = key.getBytes();
			SecretKeySpec skeySpec = new SecretKeySpec(raw, NAME);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// "算法/模式/补码方式"
			IvParameterSpec iv = new IvParameterSpec(vector.getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			byte[] encrypted = cipher.doFinal(data.getBytes());

			return new BASE64Encoder().encode(encrypted);// 此处使用BASE64做转码功能
		} catch (Exception e) {
			logger.error("AES加密失败", e);
		}
		return null;
	}

	@Override
	public String decrypt(String data, String key, String encode)
			throws Exception {
		try {
			if (StringUtils.isBlank(key) || StringUtils.length(key) != 16)
				return null;

			byte[] raw = key.getBytes("ASCII");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, NAME);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			IvParameterSpec iv = new IvParameterSpec(vector.getBytes());
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] encrypted1 = new BASE64Decoder().decodeBuffer(data);// 先用base64解密
			try {
				byte[] original = cipher.doFinal(encrypted1);
				String originalString = new String(original);

				return originalString;
			} catch (Exception e) {
				logger.error("AES解密失败", e);
			}
		} catch (Exception e) {
			logger.error("AES解密失败", e);
		}
		return null;
	}

	@Override
	public String sign(String data, String key, String encode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean verify(String sign, String data, String key, String encode) {
		// TODO Auto-generated method stub
		return null;
	}

}
