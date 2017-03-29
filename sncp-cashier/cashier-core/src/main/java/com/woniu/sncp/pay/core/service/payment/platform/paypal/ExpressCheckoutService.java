package com.woniu.sncp.pay.core.service.payment.platform.paypal;

import java.util.Map;

import com.paypal.core.Constants;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pojo.payment.PaymentOrder;


public abstract class ExpressCheckoutService {
	
	
	public static final String ITEMCATEGORY = "Digital";
	public static final String PAYMENTACTION = "Sale";
	/** 验证消息返回结果 返回成功 */
	public static final String SUCCESS = "SUCCESS";
	
	public static final String TOKEN = "token";
	public static final String PAY_URL = "payUrl";
	public static final String PAY_CMD = "cmd";
	public static final String PAYER_ID = "payerID";
	
	public static final String TOTAL_AMT = "amt";
	public static final String CURRENCY = "currency";
	public static final String INVOICE_ID = "invoiceID";
	public static final String TRANSTRATION_ID = "transtrationID";
	public static final String ITEM_NAME = "itemName";
	public static final String ITEM_PRICE = "price";
	public static final String ITEM_QTY = "qty";
	public static final String ITEM_CATEGORY = "category";
	public static final String PAYMENT_STATUS = "paymentStatus";
	
	protected Map<String, String> configurationMap = null;
	
	public Map<String, String> getConfigurationMap() {
		return configurationMap;
	}

	public void setConfigurationMap(Map<String, String> configurationMap) {
		this.configurationMap = configurationMap;
	}

	public abstract Map<String, String> setExpressCheckout(final PaymentOrder order, final Platform platform);
	
	public abstract Map<String, String> getExpressCheckoutDetails(String token);
	
	public abstract Map<String, String> doExpressCheckoutPayment(String token, String payerID, String paymentAction,
			Map<String, Object> item, String notifyURL);
	
	protected boolean isSandbox() {
		return (Constants.SANDBOX.equalsIgnoreCase(this.configurationMap.get(Constants.MODE).trim()));
	}
	
	
	protected PaypalPaymentException newException(String msg) {
		return new PaypalPaymentException(msg);
	}
	
	protected PaypalPaymentException newException(String msg,Throwable ex) {
		return new PaypalPaymentException(msg, ex);
	}
	
	protected void throwException(String msg) {
		throw newException(msg);
	}
	
	protected void throwException(String msg,Throwable ex) {
		throw newException(msg, ex);
	}
}
