package com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.dom4j.Element;

/**
 * Openbucks 帮助类
 * @author caowl
 *
 */
public class OpenbucksHelper {
	
	public static final String PUBLIC_KEY = "publicKey";
	public static final String SECRET_KEY = "secretKey";
	
	public static final String PAYMENT_URL = "paymentURL";
	
	public static final String API_SECRET_KEY = "apiSecretKey";
	public static final String TRANSACTION_DETAILS_API_CALL = "tranDetailsAPICall";
	public static final String API_CALL_VERSION = "version";
	
	public static final String SERVER_IPS = "serverIps";
	
	//public static final String SUB_PROPERTY_NAME = "subPropertyName";
	public static final String SUB_PROPERTY_URL = "subPropertyURL";
	
	public static final String RATING_MODEL = "ratingModel";
	public static final String PRODUCT_RATING = "productRating";
	
	/**
	 * token 
	 * formatted: YYYY-MM-DD HH:MM:SS[space][at least 10 random printable characters]
	 * contain: a-z A-Z 0-9 space : . - / ( ) _ [ ] | =
	 * 下面方法只随机字母和数字 
	 * @return
	 */
	public static String getToken() {
		String dataFormat = DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
		return dataFormat + RandomStringUtils.randomAlphanumeric(10);
	}
	
	/**
	 * 解析xml到Map
	 * @param element
	 * @return
	 * @throws Exception 
	 */
	
	public static Map<String, String> parseElement(Element element) {
		Map<String, String> paramMap = new HashMap<String, String>();
		if(element != null) {
			getElementValue(paramMap, element);
		}
		return paramMap;
	}
	
	/**
	 * 迭代取值
	 * @param paramMap
	 * @param element
	 */
	private static void getElementValue(final Map<String, String> paramMap ,Element element) {
		List<?> children = element.elements();
		if (children != null && children.size() > 0) {
			for (Object object : children) {
				getElementValue(paramMap, (Element)object);
			}
		} else {
			paramMap.put(element.getName(), element.getTextTrim());
		}
	}

}
