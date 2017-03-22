package com.woniu.sncp.pay.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import com.alibaba.fastjson.JSONObject;

/**
 * <p>
 * descrption:
 * </p>
 * 
 * @author fuzl
 * @date 2015年12月21日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class ExchargeUtils {
	
	/** 
     * 除去数组中的空值和签名参数
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static Map<String, String> paraFilter(JSONObject sArray) {
        Map<String, String> result = new HashMap<String, String>();
        if (sArray == null || sArray.size() <= 0) {
            return result;
        }
        for (String key : sArray.keySet()) {
            String value = ObjectUtils.toString(sArray.get(key));
            if (key.equalsIgnoreCase("sign") || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(key, value==null?"":value);
        }
        return result;
    }
    
	/**
	 * 把数组所有元素排序，并按照“参数参数值”的模式拼接成字符串
	 * 
	 * @param params
	 *            需要排序并参与字符拼接的参数组
	 * @return 拼接后字符串
	 */
	public static String createLinkString(Map<String,String> params) {
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		String prestr = "";
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key);
			prestr = prestr + key + value;
		}
		return prestr;
	}
	
	/** 
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
	public static String createLinkStringForThird(Map<String, String> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
//        Collections.sort(keys);
        String prestr = "";
        for (int i = 0; i < keys.size(); i++) {
        	if(keys.get(i).equals("sign")){
        		continue;
        	}
            String key = keys.get(i);
            String value = params.get(key).toString();
            //prestr = prestr + key + value;
            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }
        return prestr;
    }
}
