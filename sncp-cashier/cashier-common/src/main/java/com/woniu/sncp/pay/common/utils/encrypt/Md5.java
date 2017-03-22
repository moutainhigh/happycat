package com.woniu.sncp.pay.common.utils.encrypt;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Md5 extends Signature{
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String NAME = "MD5";
	
	@Override
	public String sign(String data, String key, String encode) {
		if(StringUtils.isBlank(data)){
			return null;
		}
		
		if(StringUtils.isBlank(encode)){
			encode = Encryption.ENCODE_UTF8;
		}
		
		String sign = MD5Encrypt.encrypt(data+key,encode);
		
		logger.info("md5 sign data:" + data + ",encode:" + encode + ",sign:"+sign);
		return sign;
	}

	@Override
	public Boolean verify(String sign, String data, String key, String encode) {
		if(StringUtils.isBlank(sign)){
			logger.info("md5 verify data:" + data + ",encode:" + encode +  ",sign:" + sign + ",verifyRet:false");
			return false;
		}
		
		if(StringUtils.isBlank(encode)){
			encode = Encryption.ENCODE_UTF8;
		}
		
		String localSign = this.sign(data, key, encode);
		
		Boolean verifyRet = false;
		if(sign.equalsIgnoreCase(localSign)){
			verifyRet = true;
		}
		
		logger.info("md5 verify data:" + data + ",encode:" + encode + 
					",localSign:" + localSign + ",sign:" + sign + ",verifyRet:" + verifyRet);
		return verifyRet;
	}

}
