package com.woniu.sncp.pay.core.service.payment.platform.kuaiqian.encrypt;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woniu.sncp.pay.common.exception.ValidationException;


/**
 * 快钱PKI加密类
 * 
 */
public class Pkipair {
	
	private final static Logger logger = LoggerFactory.getLogger(Pkipair.class);
	
	// 请求加密方法
	public static String signMsg(String signMsg,String priKeyFilePath,String pfxPwd) {
		String base64 = "";
		try {
			// 密钥仓库
			KeyStore ks = KeyStore.getInstance("PKCS12");

			// 读取密钥仓库
			FileInputStream ksfis = new FileInputStream(priKeyFilePath);
			BufferedInputStream ksbufin = new BufferedInputStream(ksfis);

			// 生成pfx证书时的密码
			char[] keyPwd = pfxPwd.toCharArray();
			ks.load(ksbufin, keyPwd);
			// 从密钥仓库得到私钥
			PrivateKey priK = (PrivateKey) ks.getKey("test-alias", keyPwd);
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initSign(priK);
			signature.update(signMsg.getBytes());
			sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
			base64 = encoder.encode(signature.sign());
			
		} catch(FileNotFoundException e){
			logger.error("快钱RSA私钥文件不存在！"+e.getMessage(),e);
			throw new ValidationException("快钱RSA私钥文件不存在！");
		} catch (Exception ex) {
			logger.error("快钱RSA加密异常！"+ex.getMessage(),ex);
			throw new ValidationException("快钱RSA加密异常！"+ex.getMessage());
		}
		return base64;
	}

	// 接受响应验签方法
	public static boolean enCodeByCer(String val, String msg,String pubKeyFilePath) {
		// 响应验签处理结果
		boolean flag = false;
		try {
			//获得文件
			InputStream inStream = new FileInputStream(pubKeyFilePath);
			
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
			//获得公钥
			PublicKey pk = cert.getPublicKey();
			//签名
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initVerify(pk);
			signature.update(val.getBytes());
			
			//解码
			sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
			
			flag = signature.verify(decoder.decodeBuffer(msg));
//			System.out.println(flag);
		} catch (Exception e) {
			logger.error("快钱RSA验签异常！"+e.getMessage(),e);
			throw new ValidationException("快钱RSA验签异常！"+e.getMessage());
		} 
		return flag;
	}

	// 拼接方法，已做非空判断
	public static String appendParam(String returns, String paramId,
			String paramValue) {
		if (returns != "") {
			if (isNotEmpty(paramValue)) {

				returns += "&" + paramId + "=" + paramValue;
			}

		} else {

			if (isNotEmpty(paramValue)) {
				returns = paramId + "=" + paramValue;
			}
		}

		return returns;
	}

	// 非空判断
	public static boolean isNotEmpty(String str) {
		if ("".equals(str) || null == str) {
			return false;
		}
		return true;
	}
}

