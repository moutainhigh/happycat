package com.woniu.sncp.pay.common.errorcode;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woniu.sncp.properties.ConfigurableConstants;

/**
 * 错误编码类 <br>
 * msgcode:错误编码 - 必返回 <br>
 * desc:提示信息 - 必返回 <br>
 * errorInfo:提示错误 - 数据库异常等等
 * 
 * @author yanghao 2010-3-1
 * 
 */
public class ErrorCode extends ConfigurableConstants {

	static {
		init("errorCode.properties");
	}

	private static Logger logger = LoggerFactory.getLogger(ErrorCode.class);

	/**
	 * 提示编码 - 必返回 - msgcode
	 */
	public static final String TIP_CODE = "msgcode";
	/**
	 * 提示信息 - 必返回 - message
	 */
	public static final String TIP_INFO = "message";
	/**
	 * 提示错误 - 数据库异常等等 - errorInfo
	 */
	public static final String ERROR_INFO = "errorInfo";

	/**
	 * 根据错误编码获取错误信息
	 * 
	 * @param code
	 * @return
	 */
	public static String translate(String code) {
		String errorMsg = getValue(code);
		if (errorMsg == null) {
			logger.error("[can't find errorCode(" + code + ") msg!]");
		}
		return errorMsg;
	}

	public static Map<String, Object> getErrorCode(int code) {
		String info = translate(String.valueOf(code));
		Map<String, Object> result = null;
		if (StringUtils.isNotBlank(info)) {
			result = new HashMap<String, Object>();
			result.put(TIP_CODE, code); // 类型int
			result.put(TIP_INFO, info);
		}
		return result;
	}
	
	/**
	 * 根据错误编码获取键值对参数 - 错误编码和错误信息
	 * 
	 * @param code
	 * @return
	 */
	public static Map<String, Object> getErrorCode(String code) {
		String info = translate(code);
		Map<String, Object> result = null;
		if (StringUtils.isNotBlank(info)) {
			result = new HashMap<String, Object>();
			result.put(TIP_CODE, code); // 类型string
			result.put(TIP_INFO, info);
		}
		return result;
	}

	/**
	 * 向现有map中追加键值对
	 * 
	 * @param in
	 * @param key
	 * @param value
	 * @return
	 */
	public static Map<String, Object> put(Map<String, Object> in, String key, Object value) {
		in.put(key, value);
		return in;
	}
}
