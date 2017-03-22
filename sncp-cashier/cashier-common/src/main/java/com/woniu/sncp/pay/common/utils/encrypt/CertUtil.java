package com.woniu.sncp.pay.common.utils.encrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woniu.sncp.security.codec.CodecUtil;


/**
 * 
 *
 */
public class CertUtil {

	protected static final Logger logger = LoggerFactory
			.getLogger(CertUtil.class);

	/**
	 * 获取证书私钥
	 * 
	 * @return
	 * @throws IOException
	 */
	public static String getCertKey(String mi, String path) {

		String pwd = mi;// "";//"898000000000001";
		String certPath = path;// "D://dev2.pfx";
		String billRsaKey = "";
		try {

			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream is = new FileInputStream(certPath);
			ks.load(is, pwd.toCharArray());
			is.close();
			Enumeration enuma = ks.aliases();
			String keyAlias = null;
			if (enuma.hasMoreElements()) {
				keyAlias = (String) enuma.nextElement();
			}

			PrivateKey privatekey = (PrivateKey) ks.getKey(keyAlias,
					pwd.toCharArray());
			billRsaKey = CodecUtil.encodeBase64String(privatekey.getEncoded());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return billRsaKey;
	}

	/**
	 * 获取证书私钥
	 * 
	 * @return
	 * @throws IOException
	 */
	public static String getCertKey(String path) {
		String billRsaKey = "";
		try {
			PrivateKey privatekey = getPrivateKey(path);
			billRsaKey = CodecUtil.encodeBase64String(privatekey.getEncoded());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return billRsaKey;
	}

	/**
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static byte[] getFileByte(String path) throws IOException {
		byte[] b = (byte[]) null;
		InputStream in = null;
		try {
			in = new FileInputStream(new File(path));
			b = new byte[20480];
			in.read(b);
		} finally {
			if (in != null)
				in.close();
		}
		return b;
	}

	public static PrivateKey getPrivateKey(String path) throws Exception {
		byte[] keyBytes = getFileByte(path);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		return privateKey;

	}

	/**
	 * 获取证书公钥
	 * 
	 * @return
	 * @throws IOException
	 */
	public static String getPublicCertKey(String mi, String path) {

		String pwd = mi;// "898000000000001";
		String publicCertPath = path;// "D://dev2.cer";
		String billRsaKey = "";
		try {

			// 签名 公钥解密
			CertificateFactory cff = CertificateFactory.getInstance("X.509");
			FileInputStream fis1 = new FileInputStream(publicCertPath); // 证书文件
			Certificate cf = cff.generateCertificate(fis1);
			PublicKey publicKey = cf.getPublicKey();

			byte[] pk = publicKey.getEncoded();
			billRsaKey = Base64.encode(pk);

		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return billRsaKey;
	}
}
