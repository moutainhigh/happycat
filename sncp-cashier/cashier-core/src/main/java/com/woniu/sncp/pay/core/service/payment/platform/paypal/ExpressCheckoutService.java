package com.woniu.sncp.pay.core.service.payment.platform.paypal;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.paypal.core.Constants;
import com.paypal.core.rest.APIContext;
import com.paypal.core.rest.HttpMethod;
import com.paypal.core.rest.OAuthTokenCredential;
import com.paypal.core.rest.PayPalRESTException;
import com.paypal.core.rest.PayPalResource;
import com.paypal.core.rest.RESTUtil;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pojo.payment.PaymentOrder;


public abstract class ExpressCheckoutService {
	
	private static Logger logger = LoggerFactory.getLogger(ExpressCheckoutService.class);
	
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
	
	private OAuthTokenCredential credential;
	
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
	
	public String setTransactionContextToPaypal(String merchantId, String token, String payLoad) throws PayPalRESTException {
		if (credential == null) {
			String clientId = getConfigurationMap().get(Constants.CLIENT_ID);
			String clientSecret = getConfigurationMap().get(Constants.CLIENT_SECRET);
			credential = new OAuthTokenCredential(clientId, clientSecret, getConfigurationMap());
		}
		String accessToken = null;
		if (credential.expiresIn() <= 0) {
			accessToken = credential.getAccessToken();
		}
		APIContext context = new APIContext(accessToken);
		Map<String,String> httpHeaders = new HashMap<>();
		httpHeaders.put("Content-Type", "application/json");
		context.setHTTPHeaders(httpHeaders);
		context.setConfigurationMap(getConfigurationMap());
		
		String resourcePath = "v1/risk/transaction-contexts/{0}/{1}";
		resourcePath = RESTUtil.formatURIPath(resourcePath, new Object[] {merchantId, token});
		String payload = createJson(payLoad);
		logger.info("Send transactionContexts payload:{}", payload);
		return PayPalResource.configureAndExecute(context, HttpMethod.POST, resourcePath, payload, String.class);
		
	}
	
	private static String createJson(String parameter){
		Map<String, Object> map = JSONObject.parseObject(parameter);
		
		Map<String, Object> partnerAccount = new LinkedHashMap<String, Object>();
		partnerAccount.put("email", map.get("email"));
		partnerAccount.put("create_date", map.get("createDate"));
		partnerAccount.put("last_good_transaction_date", MapUtils.getString(map, "lastGoodTransactionDate"));
		partnerAccount.put("transaction_count_total", MapUtils.getIntValue(map, "transactionCountTotal", 0));
		partnerAccount.put("transaction_count_three_months", MapUtils.getIntValue(map, "transactionCountThreeMmonths", 0));
		
		Map<String, Object> senderAccount = new LinkedHashMap<String, Object>(1);
		senderAccount.put("partner_account", partnerAccount);
		
		Map<String, Object> ipAddressInfo = new LinkedHashMap<String, Object>();
		ipAddressInfo.put("ip_address", map.get("ip"));
		
		Map<String, Object> root = new LinkedHashMap<String, Object>();
		root.put("sender_account", senderAccount);
		root.put("ip_address_info", ipAddressInfo);
		
		return JSONObject.toJSONString(root);
	}
	
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
