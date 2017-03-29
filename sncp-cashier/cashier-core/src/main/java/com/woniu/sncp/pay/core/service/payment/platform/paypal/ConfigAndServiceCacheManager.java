package com.woniu.sncp.pay.core.service.payment.platform.paypal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.woniu.sncp.pay.core.service.payment.platform.paypal.nvp.PaypalECNVPAPIService;

public class ConfigAndServiceCacheManager {
	
	private static final String API_VERSION = "100";
	
	private static final String PORT_NAME = "NVP";

	private static ConcurrentMap<String, ExpressCheckoutService> serviceCache = 
			new ConcurrentHashMap<String, ExpressCheckoutService>();
	
	public static ExpressCheckoutService getECService(String userID) {
		return serviceCache.get(userID);
	}
	
	public static void putECService(String userID, Map<String, String> configurationMap) {
		ExpressCheckoutService service = new PaypalECNVPAPIService(userID, API_VERSION, PORT_NAME, configurationMap);
		serviceCache.putIfAbsent(userID, service);
	}
}
