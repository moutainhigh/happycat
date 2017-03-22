package com.woniu.sncp.pay.common.utils.encrypt;

import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;//import org.apache.log4j.Logger;;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


/**
 * @author maocongchang
 * 
 */
@SuppressWarnings("restriction")
public class DesUtil {

	private static DesUtil desSecurity;
	private BASE64Encoder encoder;
	private BASE64Decoder decoder;
	private Cipher enCipher;
	private Cipher deCipher;

	public DesUtil(String key,String iv) throws Exception {

//		Properties properties = new Properties();
//		properties.load(this.getClass().getClassLoader().getResourceAsStream("des_key.properties"));
//		String iv = "";
//		String key = "";

//		key = (String) properties.get("key");
//		iv = (String) properties.get("salt");
		initCipher(key.getBytes(), iv.getBytes());
	}

	public static DesUtil getInstance(String key,String iv) throws Exception {
		if (desSecurity == null) {
			desSecurity = new DesUtil(key,iv);
		}
		return desSecurity;
	}

	private void initCipher(byte[] secKey, byte[] secIv) throws Exception {
		//业务方不支持key超出8位，java支持8的倍数，业务方只能是8位，
//		MessageDigest md = MessageDigest.getInstance("MD5");
//		md.update(secKey);
		DESKeySpec dks = new DESKeySpec(secKey);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(dks);
		IvParameterSpec iv = new IvParameterSpec(secIv);
		AlgorithmParameterSpec paramSpec = iv;
		enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		deCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		enCipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
		deCipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		encoder = new BASE64Encoder();
		decoder = new BASE64Decoder();
	}

	public String encrypt(byte[] data) throws Exception {
		return encoder.encode(enCipher.doFinal(data)).replaceAll("\n", "").replaceAll("\r", "");
	}

	public byte[] decrypt(String data) throws Exception {
		return deCipher.doFinal(decoder.decodeBuffer(data));
	}

	public static final String MD5 = "M5"; // MD5方式
	public static final String NO = "AA"; // 不加密
	public static final String DES = "BB"; // DES方式
	protected static Logger logger = LoggerFactory.getLogger(DesUtil.class);

	/**
	 * 根据加密方法加密原文
	 * 
	 * @param encryptedPwd
	 *            加密后的密码
	 * @param plainPwd
	 *            员密码
	 * @return
	 */
	public static String getCString(String key,String iv,String encryptedPwd, String plainPwd) {
		logger.warn("encryptedPwd=" + encryptedPwd + " plainPwd=" + plainPwd);
		if (StringUtils.isBlank(encryptedPwd)|| StringUtils.isBlank(plainPwd)) {
			return null;
		}
		String subString = encryptedPwd.substring(0, 2);
		if (MD5.equals(subString)) {
			return MD5 + MD5Encrypt.encrypt(plainPwd);
		} else if (NO.equals(subString)) {
			return NO + plainPwd;
		} else if (DES.equals(subString)) {
			try {
				return DES + DesUtil.getInstance(key,iv).encrypt(plainPwd.getBytes());
			} catch (Exception e) {
				logger.warn("decryPwd error", e);
				return null;
			}
		}
		return subString;
	}

	/**
	 * 注册广告专用解密
	 * 
	 * @author zsc
	 * @param encryptedString
	 *            广告ID加密串
	 * @return (Long)广告ID
	 */
	public static Long decryptDes(String encryptedString) {
		Long adid = 0L;
		try {
			// Cipher对象实际完成加密操作
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			DESKeySpec key = new DESKeySpec("QkcurOnT".getBytes());// 加密key
			IvParameterSpec iv = new IvParameterSpec("yhdOjEXd".getBytes());
			byte[] src = new BASE64Decoder().decodeBuffer(encryptedString);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = keyFactory.generateSecret(key);
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
			// 现在,获取数据并解密,正式执行解密操作
			adid = Long.valueOf(new String(cipher.doFinal(src)));
		} catch (Exception e) {
		}
		return adid;
	}

	public static void main(String[] args) throws Exception {
		DesUtil util = DesUtil.getInstance("z@856!j4","l1PJiASc");
		byte[] decrypt = util.decrypt("7b4CETtf8aHFPMdNP7OlcLhBtGm5ioYS0o3tZyBcCQJRYydudYMBJExYboMces/W4U90205uv0LRj+ooqTyJX4A5xKzFc6z4IWb6T9mB0nrmMEE7grjJBlcmnJ8xcYQ6IPpuzxuheVHNzCZCRGG6+Dzmd9qreOSilKTwpRgDh9j5/itHVUoRCChf63kGksIw3no66ZV7SUZ2+SUFuPwwq1Hekdk+ZBoh+CS3l4/TJjwEfX0U4tu7Em6A+BuEn/CrAnn9Zdp499OX+L2qw1Nte+Daka4ddqOj6NmzrqhZrUVa8K5tqLSlOk0JD4kYd+xpWXYpY5p3ZwcWCBTKePsaZTzYUkPoKLsYSu6Lwem2KcMc0ePChdzQAYYUdjdjWbJl3cZSSxKEU7ems/vduXndLVh1bLSksuXE");
		String result = new String(decrypt);
		System.out.println(result);
		
//		result = StringUtils.trim(StringUtils.upperCase(result));
		String encrypt = util.encrypt("merchantid=100019&orderno=20170802-007-00000000005&money=0.01&clientip=172.18.70.15&productname=测试商品支付&backendurl=http://10.17.0.22:199/payment/backend/api/message/push&ext={'serverId':7440069,'terminalType':'1'}&fontendurl=http://222.92.116.36:90/payment/front/api/common&gameid=44&account=test2001".getBytes());
		System.out.println(encrypt);
	}
}