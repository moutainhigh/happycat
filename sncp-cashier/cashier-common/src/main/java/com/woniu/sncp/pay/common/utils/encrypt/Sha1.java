package com.woniu.sncp.pay.common.utils.encrypt;

import java.security.MessageDigest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sha1 extends Signature{
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String NAME = "SHA1";
	
	@Override
	public String sign(String data, String key, String encode) {
		if(StringUtils.isBlank(data)){
			return null;
		}
		
		if(StringUtils.isBlank(encode)){
			encode = Encryption.ENCODE_UTF8;
		}
		
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };

		try {
			MessageDigest mdTemp = MessageDigest.getInstance(NAME);
			mdTemp.update(data.getBytes(encode));

			byte[] md = mdTemp.digest();
			int j = md.length;
			char buf[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
				buf[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(buf);
		} catch (Exception e) {
			logger.error("sha1 sign:"+e.getMessage(),e);
			return null;
		}
	}

	@Override
	public Boolean verify(String sign, String data, String key, String encode) {
		if(StringUtils.isBlank(encode)){
			encode = Encryption.ENCODE_UTF8;
		}
		
		if(StringUtils.isBlank(sign)){
			logger.info("sha1 verify data:" + data + ",encode:" + encode +  ",sign:" + sign + ",verifyRet:false");
			return false;
		}
		
		String localSign = this.sign(data, key, encode);
		
		Boolean verifyRet = false;
		if(sign.equalsIgnoreCase(localSign)){
			verifyRet = true;
		}
		
		logger.info("sha1 verify data:" + data + ",encode:" + encode + 
					",localSign:" + localSign + ",sign:" + sign + ",verifyRet:" + verifyRet);
		return verifyRet;
	}

}
