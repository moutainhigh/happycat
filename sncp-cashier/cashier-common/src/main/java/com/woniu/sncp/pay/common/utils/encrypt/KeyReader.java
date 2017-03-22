package com.woniu.sncp.pay.common.utils.encrypt;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class KeyReader {

	/**
	 * 将X509格式的输入流转换成Certificate对象。
	 * 
	 * @param ins
	 * @return
	 */
	public static Certificate getCertificateFromX509(InputStream ins) {
		try {
			Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(ins);
			return certificate;
		} catch (CertificateException ex) {
			return null;
		}
	}

	/**
	 * @param algorithm
	 * @param ins
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static PublicKey getPublicKeyFromX509(String algorithm, InputStream ins) throws NoSuchAlgorithmException {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
			StringWriter writer = new StringWriter();
			IOUtils.copy(ins, writer);
			byte[] encodedKey = writer.toString().getBytes();
			// 先base64解码
			encodedKey = Base64.decodeBase64(encodedKey);
			return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
		} catch (IOException ex) {
			//不可能发生
		} catch (InvalidKeySpecException ex) {
			//不可能发生
		}
		return null;
	}

	/**
	 * @param algorithm
	 * @param ins
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static PrivateKey getPrivateKeyFromPKCS8(String algorithm, InputStream ins) throws NoSuchAlgorithmException {
		if (ins == null || StringUtils.isBlank(algorithm)) {
			return null;
		}
		try {
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
			byte[] encodedKey = IOUtils.toByteArray(ins);
			// 先base64解码
			encodedKey = Base64.decodeBase64(encodedKey);
			return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (InvalidKeySpecException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
