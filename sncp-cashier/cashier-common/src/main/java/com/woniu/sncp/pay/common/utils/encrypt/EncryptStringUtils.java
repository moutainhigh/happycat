package com.woniu.sncp.pay.common.utils.encrypt;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

public class EncryptStringUtils {
	/**
	 * 将参数以 key1=value1&key2=value2的形式按照放入顺序拼装
	 * 
	 * @param inParams
	 * @param blankIsNotIn
	 *            true - 空白字段不加入 false - 空白字段也加入
	 * @return
	 */
	public static String linkedHashMapToStringWithKey(LinkedHashMap<String, Object> inParams, boolean blankIsNotIn) {
		return linkedHashMapToStringWithKey(inParams, blankIsNotIn, null);
	}

	/**
	 * 将参数以 key1=value1&key2=value2的形式按照放入顺序拼装
	 * 
	 * @param inParams
	 * @param blankIsNotIn
	 *            true - 空白字段不加入 false - 空白字段也加入
	 * @param replaceString
	 *            blankIsNotIn=ture且该值不为空-则将inParams中不为空的字段替换为replaceString
	 * @return
	 */
	public static String linkedHashMapToStringWithKey(LinkedHashMap<String, Object> inParams, boolean blankIsNotIn,
			String replaceString) {
		StringBuffer buffer = new StringBuffer();
		for (Iterator<Entry<String, Object>> keyValuePairs = inParams.entrySet().iterator(); keyValuePairs.hasNext();) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) keyValuePairs.next();
			String key = entry.getKey();
			String value = ObjectUtils.toString(entry.getValue());
			if (!blankIsNotIn) {
				if (StringUtils.isNotBlank(replaceString) && StringUtils.isBlank(value))
					buffer.append(key).append("=").append(replaceString).append("&");
				else
					buffer.append(key).append("=").append(value).append("&");
			} else if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value))
				buffer.append(key).append("=").append(value).append("&");
		}
		return StringUtils.substringBeforeLast(buffer.toString(), "&");
	}

	/**
	 * 将参数以 value1value2的形式按照放入顺序拼装
	 * 
	 * @param inParams
	 * @param blankIsNotIn
	 *            true - 空白字段不加入 false - 空白字段也加入
	 * @return
	 */
	public static String linkedHashMapToStringWithNoKey(LinkedHashMap<String, Object> inParams, boolean blankIsNotIn) {
		StringBuffer buffer = new StringBuffer();
		for (Iterator<Entry<String, Object>> keyValuePairs = inParams.entrySet().iterator(); keyValuePairs.hasNext();) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) keyValuePairs.next();
			String key = entry.getKey();
			String value = ObjectUtils.toString(entry.getValue());
			if (!blankIsNotIn)
				buffer.append(value);
			else if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value))
				buffer.append(value);
		}
		return buffer.toString();
	}
}
