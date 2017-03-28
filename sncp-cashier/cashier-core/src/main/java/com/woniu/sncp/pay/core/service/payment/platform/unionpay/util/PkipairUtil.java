package com.woniu.sncp.pay.core.service.payment.platform.unionpay.util;

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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woniu.sncp.pay.common.exception.ValidationException;

public class PkipairUtil {
	
	private final static Logger logger = LoggerFactory.getLogger(PkipairUtil.class);
	private final static String certId = "certId";//证书id
	private final static String signature = "signature";//加密字段
	
	/**
	 * 银联wap加密
	 * @param params
	 * @param encode
	 * @param priKeyFilePath
	 * @param pfxPwd
	 * @return
	 */
	public static Map<String, Object> signMsg(Map<String, Object> params,String encode ,String priKeyFilePath,String pfxPwd) {
		if(StringUtils.isBlank(priKeyFilePath) || StringUtils.isBlank(pfxPwd)){
			logger.error("参数错误,path：" + priKeyFilePath + ",pwd:" + pfxPwd);
			return null;
		}
		
		if(StringUtils.isBlank(encode)){
			encode = "UTF-8";
		}
		
		try {
			// 密钥仓库
			KeyStore ks = KeyStore.getInstance("PKCS12");

			// 读取密钥仓库
			FileInputStream ksfis = new FileInputStream(priKeyFilePath);
			BufferedInputStream ksbufin = new BufferedInputStream(ksfis);

			// 生成pfx证书时的密码
			char[] keyPwd = pfxPwd.toCharArray();
			ks.load(ksbufin, keyPwd);
			Enumeration<String> aliasenum = ks.aliases();
			String keyAlias = null;
			if (aliasenum.hasMoreElements()) {
				keyAlias = aliasenum.nextElement();
			}
			//获取证书id
			X509Certificate cert = (X509Certificate) ks.getCertificate(keyAlias);
			String certId =  cert.getSerialNumber().toString();
			params.put(PkipairUtil.certId, certId);
			
			// 将Map信息转换成key1=value1&key2=value2的形式
			String stringData = coverMap2String(params);
			
			// 从密钥仓库得到私钥
			PrivateKey priK = (PrivateKey) ks.getKey(keyAlias, keyPwd);
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initSign(priK);
			byte[] signDigest = SecureUtil.sha1X16(stringData, encode);
			signature.update(signDigest);
			
			//base 64
			String base64 = null;
			//sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
			//base64 = encoder.encode(signature.sign());
			
			base64 = new String(SecureUtil.base64Encode(signature.sign())); 
			params.put(PkipairUtil.signature, base64);
			return params;
		} catch(FileNotFoundException e){
			logger.error("银联wap私钥文件不存在！"+e.getMessage(),e);
			throw new ValidationException("银联wap私钥文件不存在！");
		} catch (Exception ex) {
			logger.error("银联wap加密异常！"+ex.getMessage(),ex);
			throw new ValidationException("银联wap加密异常！"+ex.getMessage());
		}
	}
	
	/**
	 * 银联wap验签
	 * @param resData
	 * @param encode
	 * @param pubKeyFilePath
	 * @return
	 */
	public static boolean validate(Map<String, Object> resData, String encode, String pubKeyFilePath) {
		if(StringUtils.isBlank(pubKeyFilePath)){
			logger.error("参数错误,path：" + pubKeyFilePath);
			return false;
		}
		
		if(StringUtils.isBlank(encode)){
			encode = "UTF-8";
		}
		
		// 响应验签处理结果
		boolean flag = false;
		try {
			String signMsg = String.valueOf(resData.get(PkipairUtil.signature));
			// 将Map信息转换成key1=value1&key2=value2的形式
			String stringData = coverMap2String(resData);
			byte[] signDigest = SecureUtil.sha1X16(stringData,encode);
			
			//获得文件
			InputStream inStream = new FileInputStream(pubKeyFilePath);
			
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
			//获得公钥
			PublicKey pk = cert.getPublicKey();
			//签名
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initVerify(pk);
			signature.update(signDigest);
			
			//解码
			//sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
			//flag = signature.verify(decoder.decodeBuffer(signMsg));
			
			flag = signature.verify(SecureUtil.base64Decode(signMsg.getBytes(encode)));
		} catch (Exception e) {
			logger.error("银联wap验签异常！"+e.getMessage(),e);
			throw new ValidationException("银联wap验签异常！"+e.getMessage());
		} 
		return flag;
	}
	
	/**
	 * 将Map中的数据转换成key1=value1&key2=value2的形式 不包含签名域signature
	 * 
	 * @param data
	 *            待拼接的Map数据
	 * @return 拼接好后的字符串
	 */
	public static String coverMap2String(Map<String, Object> data) {
		TreeMap<String, Object> tree = new TreeMap<String, Object>();
		Iterator<Entry<String, Object>> it = data.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> en = it.next();
			if (PkipairUtil.signature.equals(en.getKey().trim())) {
				continue;
			}
			tree.put(en.getKey(), en.getValue());
		}
		it = tree.entrySet().iterator();
		StringBuffer sf = new StringBuffer();
		while (it.hasNext()) {
			Entry<String, Object> en = it.next();
			sf.append(en.getKey() + "=" + String.valueOf(en.getValue()) + "&");
		}
		return sf.substring(0, sf.length() - 1);
	}

}
