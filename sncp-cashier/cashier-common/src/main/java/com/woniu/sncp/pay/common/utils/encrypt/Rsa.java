package com.woniu.sncp.pay.common.utils.encrypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * rsa 签名，验证，加密，解密
 * @author luzz
 *
 */
public class Rsa implements Encryption{
	
	static final String SIGN_ALGORITHMS = "SHA1WithRSA";
	public static final String NAME = "RSA";
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public String sign(String data, String key, String encode) {
		if(StringUtils.isBlank(encode)){
			encode = Encryption.ENCODE_UTF8;
		}
		
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
					Base64.decode(key));
			KeyFactory keyf = KeyFactory.getInstance(NAME);
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);

			java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

			signature.initSign(priKey);
			signature.update(data.getBytes(encode));

			byte[] signed = signature.sign();

			return Base64.encode(signed);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}

		return null;
	}

	@Override
	public Boolean verify(String sign, String data, String key, String encode) {
		if(StringUtils.isBlank(encode)){
			encode = Encryption.ENCODE_UTF8;
		}
		
		try {
			KeyFactory keyFactory = KeyFactory.getInstance(NAME);
			byte[] encodedKey = Base64.decode(key);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

			java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

			signature.initVerify(pubKey);
			signature.update(data.getBytes(encode));

			boolean bverify = signature.verify(Base64.decode(sign));
			return bverify;

		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}

		return false;
	}

	@Override
	public String encrypt(String data, String key, String encode) throws Exception {
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decode(key));
		KeyFactory keyFactory = KeyFactory.getInstance(NAME);
		PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);

		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, publicKey);

		return new String(cipher.doFinal(Base64.decode(data)));
	}

	@Override
	public String decrypt(String data, String key, String encode) throws Exception {
		if(StringUtils.isBlank(encode)){
			encode = Encryption.ENCODE_UTF8;
		}
		
		PrivateKey prikey = getPrivateKey(key);

		Cipher cipher = Cipher.getInstance(NAME);
		cipher.init(Cipher.DECRYPT_MODE, prikey);

		InputStream ins = new ByteArrayInputStream(Base64.decode(data));
		ByteArrayOutputStream writer = new ByteArrayOutputStream();
		byte[] buf = new byte[128];
		int bufl;

		while ((bufl = ins.read(buf)) != -1) {
			byte[] block = null;

			if (buf.length == bufl) {
				block = buf;
			} else {
				block = new byte[bufl];
				for (int i = 0; i < bufl; i++) {
					block[i] = buf[i];
				}
			}

			writer.write(cipher.doFinal(block));
		}

		return new String(writer.toByteArray(), encode);
	}
	
	/**
	 * 
	 * 得到私钥
	 * 
	 * @param key
	 *            密钥字符串（经过base64编码）
	 * 
	 * @throws Exception
	 */
	public static PrivateKey getPrivateKey(String key) throws Exception {
		byte[] keyBytes;
		keyBytes = Base64.decode(key);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		return privateKey;
	}

	public static void main(String[] args) {
//		String pkey = AlipayHelper.readText("e:/opt/security/keys/alipay/alipay_app_rsa_pubkey.pem");
//		String signStr = "body=熊猫道具&buyer_email=jishizhaidong@sina.com&buyer_id=2088202300159963&discount=0.00&gmt_create=2015-09-22 19:01:42&gmt_payment=2015-09-22 19:01:43&is_total_fee_adjust=N&notify_id=ee954f0c29916f85471066a509cece2f7c&notify_time=2015-09-22 19:01:43&notify_type=trade_status_sync&out_trade_no=20150922-2004-007-0000401583&payment_type=1&price=0.01&quantity=1&seller_email=snail.account10@snailgame.net&seller_id=2088901698590302&subject=熊猫道具&total_fee=0.01&trade_no=2015092200001000960059965060&trade_status=TRADE_SUCCESS&use_coupon=N";
//		String sign = "WQGPbKhnX5mXb3ewsmk3WukyzbogtESyDI5ywpKVCILB7RDYPjLnWdtQjXlj2N0I1yMe3nRsNmDDkMxq8R5cBiJFl5a+I/XWs3iiegM8woIOwq465G4DkOc/hsXdSTVoKn03hEflHPeETLYG3Yf0P9CyIqB8hd16QBl/RzfdH0o=";
//		System.out.println(EncryptFactory.getInstance(Rsa.NAME).verify(sign, signStr, pkey, "utf-8"));
	}
}
