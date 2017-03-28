package com.woniu.sncp.pay.core.service.monitor;

import com.woniu.sncp.properties.ConfigurableConstants;

public class MonitorUrlProperties extends ConfigurableConstants {
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
