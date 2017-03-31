package com.woniu.sncp.pay.core.service.payment.platform.paypal.nvp;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paypal.core.APIService;
import com.paypal.core.Constants;
import com.paypal.core.CredentialManager;
import com.paypal.core.NVPUtil;
import com.paypal.core.credential.ICredential;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.MissingCredentialException;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.core.service.payment.platform.paypal.ExpressCheckoutService;
import com.woniu.sncp.pojo.payment.PaymentOrder;

public class PaypalECNVPAPIService extends ExpressCheckoutService {
	
	private static final Logger logger = LoggerFactory.getLogger(PaypalECNVPAPIService.class);
	
	public static final String SET_EC_METHOD = "SetExpressCheckout";
	public static final String GET_EC_DETAILS_METHOD = "GetExpressCheckoutDetails";
	public static final String DO_EC_PAYMENT_METHOD = "DoExpressCheckoutPayment";

	private static final String PORT_NAME = "NVP";
	
	private APIService apiService;
	private ICredential credential;
	
	private String userID;
	private String version = "98";
	private String portName = PORT_NAME;
	
	public ICredential getCredential() {
		return credential;
	}

	public void setCredential(ICredential credential) {
		this.credential = credential;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public void setApiService(APIService apiService) {
		this.apiService = apiService;
	}
	
	public PaypalECNVPAPIService() {
		
	}

	public PaypalECNVPAPIService(String userID, String version, String portName,
			ICredential credential, Map<String, String> configurationMap) {
		this.userID = userID;
		this.version = version;
		this.portName = portName;
		this.configurationMap = configurationMap;
		this.apiService = new APIService(configurationMap);
		this.credential = credential;
	}
	
	public PaypalECNVPAPIService(String userID, String version, String portName, Map<String, String> configurationMap) {
		this.userID = userID;
		this.version = version;
		this.portName = portName;
		this.configurationMap = configurationMap;
		this.apiService = new APIService(configurationMap);
		CredentialManager credentialmgr = new CredentialManager(configurationMap);
		try {
			this.credential = credentialmgr.getCredentialObject(userID);
		} catch (MissingCredentialException e) {
			throw newException("Create ICredential error", e);
		} catch (InvalidCredentialException e) {
			throw newException("Create ICredential error", e);
		}
	}
	
	@Override
	public Map<String, String> setExpressCheckout(final PaymentOrder paymentOrder, final Platform platform) {
		
		
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("RETURNURL",  platform.getFrontUrl(paymentOrder.getMerchantId()));
		params.put("CANCELURL", paymentOrder.getPaypartnerFrontCall());
		
		params.put("PAYMENTREQUEST_0_PAYMENTACTION", PAYMENTACTION);
		
		params.put("PAYMENTREQUEST_0_AMT", new DecimalFormat("0.00").format(paymentOrder.getMoney()) );
		params.put("PAYMENTREQUEST_0_ITEMAMT", new DecimalFormat("0.00").format(paymentOrder.getMoney()));
		params.put("PAYMENTREQUEST_0_CURRENCYCODE", paymentOrder.getMoneyCurrency());
		
		//paypal提供的自定义参数,传入我方订单号。用于paypal的IPN消息异步回传给我方
		params.put("PAYMENTREQUEST_0_INVNUM", paymentOrder.getOrderNo());
		
		params.put("REQCONFIRMSHIPPING", "0");
		params.put("NOSHIPPING", "1");
		//设置paypal支付界面左侧购买物品名称金额等信息的显示
		//货物名称 订单的金额 数量
		params.put("L_PAYMENTREQUEST_0_NAME0", paymentOrder.getImprestCardName());
		params.put("L_PAYMENTREQUEST_0_AMT0", new DecimalFormat("0.00").format(paymentOrder.getMoney()));
		params.put("L_PAYMENTREQUEST_0_QTY0", String.valueOf(paymentOrder.getAmount()));
		
//		params.put("L_PAYMENTREQUEST_0_ITEMCATEGORY0", ITEMCATEGORY);
		
		Map<String, String> nvpMap = executeRequest(params, SET_EC_METHOD);
		
		if(nvpMap == null || !SUCCESS.equalsIgnoreCase(nvpMap.get("ACK"))){
			throw newException("Paypal SetExpressCheckOut response ack is not success.");
		}
		
		String token = nvpMap.get("TOKEN");
		if(StringUtils.isBlank(token)){
			throw newException("Paypal SetExpressCheckOut response token is not found.");
		}
		String endpoint = "";
		if (isSandbox()) {
			endpoint = Constants.IPN_SANDBOX_ENDPOINT + "?cmd=_express-checkout&token=" + token;
		} else {
			endpoint = Constants.IPN_LIVE_ENDPOINT + "?cmd=_express-checkout&token=" + token;
		}
		Map<String, String> result = new HashMap<String, String>();
		result.put(ExpressCheckoutService.PAY_URL, endpoint);
//		result.put(ExpressCheckoutService.PAY_CMD, "_express-checkout");
//		result.put(ExpressCheckoutService.TOKEN, token);
		logger.info("SetExpressCheckOut request successfully. url : {}" + platform.getPayUrl());
		return result;
	}

	@Override
	public Map<String, String> getExpressCheckoutDetails(String token) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("TOKEN", token);
		
		
		Map<String, String> nvpMap = executeRequest(params, GET_EC_DETAILS_METHOD);
		
		if(nvpMap == null || !SUCCESS.equalsIgnoreCase(nvpMap.get("ACK"))){
			throw newException("Paypal GetExpressCheckoutDetails response ack is not success.");
		}
		
		Map<String, String> result = new LinkedHashMap<String, String>();
		result.put(TOKEN, nvpMap.get("TOKEN"));
		result.put(PAYER_ID, nvpMap.get("PAYERID"));
		
		result.put(TOTAL_AMT, nvpMap.get("PAYMENTREQUEST_0_AMT"));
		result.put(CURRENCY, nvpMap.get("PAYMENTREQUEST_0_CURRENCYCODE"));
		result.put(INVOICE_ID, nvpMap.get("PAYMENTREQUEST_0_INVNUM"));
		
		result.put(ITEM_NAME, nvpMap.get("L_PAYMENTREQUEST_0_NAME0"));
		result.put(ITEM_PRICE, nvpMap.get("L_PAYMENTREQUEST_0_AMT0"));
		result.put(ITEM_QTY, nvpMap.get("L_PAYMENTREQUEST_0_QTY0"));
//		result.put(ITEM_CATEGORY, nvpMap.get("L_PAYMENTREQUEST_0_ITEMCATEGORY0"));
		return result;
	}

	@Override
	public Map<String, String> doExpressCheckoutPayment(String token,
			String payerID, String paymentAction, Map<String, Object> item, String notifyURL) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("TOKEN", token);
		params.put("PAYERID", payerID);
		params.put("PAYMENTREQUEST_0_PAYMENTACTION", paymentAction);
		params.put("PAYMENTREQUEST_0_AMT", item.get(TOTAL_AMT).toString());
		params.put("PAYMENTREQUEST_0_CURRENCYCODE", MapUtils.getString(item, CURRENCY));
		
		
		if (notifyURL != null) {
			params.put("PAYMENTREQUEST_0_NOTIFYURL", notifyURL);
		}
		
		params.put("L_PAYMENTREQUEST_0_NAME0", MapUtils.getString(item, ITEM_NAME));
		params.put("L_PAYMENTREQUEST_0_AMT0", MapUtils.getString(item, ITEM_PRICE));
		params.put("L_PAYMENTREQUEST_0_QTY0", MapUtils.getString(item, ITEM_QTY));
		//Digital
//		params.put("L_PAYMENTREQUEST_0_ITEMCATEGORY0", MapUtils.getString(item, ITEM_CATEGORY));
		
		Map<String, String> nvpMap = executeRequest(params, DO_EC_PAYMENT_METHOD);
		
		String ack = nvpMap.get("ACK");
		if(nvpMap == null || !StringUtils.startsWithIgnoreCase(ack, SUCCESS)){
			throw newException("Paypal doExpressCheckout response ack is not success.");
		}
		
		Map<String, String> result = new LinkedHashMap<String, String>();
		result.put(TRANSTRATION_ID, nvpMap.get("PAYMENTINFO_0_TRANSACTIONID"));
		result.put(INVOICE_ID, nvpMap.get("PAYMENTREQUEST_0_INVNUM"));
		result.put(TOTAL_AMT, nvpMap.get("PAYMENTINFO_0_AMT"));
		result.put(CURRENCY, nvpMap.get("PAYMENTINFO_0_CURRENCYCODE"));
		result.put(PAYMENT_STATUS, nvpMap.get("PAYMENTINFO_0_PAYMENTSTATUS"));
		return result;
	}
	
	
	private Map<String, String> executeRequest(Map<String, String> params, String methodName) {
		String payload = null;
		try {
			payload = encodeParameter(params);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			throw newException(e.getMessage(), e.getCause());
		}
		
		String response = null;
		try {
			DefaultNVPAPICallHandler platformApiCaller = new DefaultNVPAPICallHandler(methodName,
					getVersion(), getPortName(), payload, getCredential(), getConfigurationMap());
			response = this.apiService.makeRequestUsing(platformApiCaller);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw newException(e.getMessage(), e.getCause());
		} 
		
		if(response == null) {
			throw newException("Paypal SetExpressCheckOut response is null.");
		}
		
		Map<String, String> nvpMap = null;
		try {
			nvpMap = NVPUtil.decode(response);
		} catch (UnsupportedEncodingException e) {
			throw newException("Paypal SetExpressCheckOut decode error.", e);
		}
		return nvpMap;
	}
	
	private String encodeParameter(Map<String, String> params) throws UnsupportedEncodingException {
		StringBuilder builder = new StringBuilder("&");
		for (Map.Entry<String, String> entry : params.entrySet()) {
			builder.append(entry.getKey()).append("=").append(NVPUtil.encodeUrl(entry.getValue())).append("&");
		}
		builder = builder.deleteCharAt(builder.length() -1);
		return builder.toString();
	}

}
