package com.woniu.sncp.pay.core.service.payment.conf;

import com.woniu.sncp.properties.ConfigurableConstants;

/**
 * 
 * @author luzz
 *
 */
public class PaymentProperties extends ConfigurableConstants {

	/**
	 * 初始化属性文件
	 */
	static {
		init("application.properties");
	}

	/**
	 * 获取值
	 * 
	 * @param key
	 * @return
	 */
	public static String getProperty(String key) {
		return getValue(key);
	}
}