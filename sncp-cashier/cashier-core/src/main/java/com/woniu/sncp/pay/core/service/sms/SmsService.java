package com.woniu.sncp.pay.core.service.sms;

import java.util.Map;

public interface SmsService {
	
	/**
	 * 发送验证码
	 * @param mobileNo
	 * @param ext
	 * @return
	 */
	Map<String, Object> sendSmsValidateCode(String mobileNo,String type);
	
	/**
	 * 验证短信验证码
	 * @param mobileNo
	 * @param smsCode
	 * @return
	 */
	Map<String, Object> validateSmsCode(String mobileNo,String type,String smsCode);

}
