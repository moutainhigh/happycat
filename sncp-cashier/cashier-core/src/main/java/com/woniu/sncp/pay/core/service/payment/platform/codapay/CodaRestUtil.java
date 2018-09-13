package com.woniu.sncp.pay.core.service.payment.platform.codapay;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.woniu.sncp.json.JsonUtils;

import org.apache.commons.lang.ObjectUtils;

import com.woniu.sncp.pay.core.service.payment.platform.codapay.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodaRestUtil {
	private static final Logger logger = LoggerFactory.getLogger(CodaRestUtil.class);

	private String airtimeRestURL = null;

	private String apiKey = null;
	private short country = 0;
	private short currency = 0;
	private short paymentType = 0;
	private String payUrlTemplate = null;

	public CodaRestUtil(String payUrlTemplate, String airtimeRestURL, String apiKey, short country, short currency, short paymentType) {
		this.airtimeRestURL = airtimeRestURL;

		this.apiKey = apiKey;
		this.country = country;
		this.currency = currency;
		this.paymentType = paymentType;
		this.payUrlTemplate = payUrlTemplate;
	}

	public String formatPayUrl(Object no) {
		return String.format(payUrlTemplate, ObjectUtils.toString(no));
	}

	//
	// static {
	// if (properties == null) {
	// properties = new Properties();
	// try {
	// properties.load(CodaRestUtil.class.getResourceAsStream("/coda.properties"));
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// if (properties != null) {
	// country = Short.parseShort(properties.getProperty("airtime.country"));
	// currency = Short.parseShort(properties.getProperty("airtime.currency"));
	//
	// log.info("AIRTIME CONFIG COUNTRY/CURRENCY : " + country);
	//
	// airtimeRestURL = properties.getProperty("airtime.rest.url");
	// log.info("AIRTIME CONFIG REST URL : " + airtimeRestURL);
	//
	// airtimeHost = properties.getProperty("airtime.host");
	// log.info("AIRTIME CONFIG Host URL : " + airtimeHost);
	//
	// airtimeURL = properties.getProperty("airtime.url");
	// log.info("AIRTIME CONFIG URL : " + airtimeURL);
	//
	// apiKey = properties.getProperty("airtime.apikey");
	// log.info("AIRTIME CONFIG API_KEY : " + apiKey);
	//
	// if (properties.getProperty("airtime.requesttype") != null) {
	// requestType = properties.getProperty("airtime.requesttype");
	// }
	// log.info("AIRTIME CONFIG REQUEST TYPE : " + requestType);
	//
	// paymentType = Short.parseShort(properties.getProperty("airtime.txntype"));
	//
	// }
	// }
	// }

	// public InitResult initTxn(RestTemplate restTemplate, String orderId,
	// ArrayList<ItemInfo> items, HashMap<String, String> profile) {
	// WebClient client = WebClient.create(airtimeRestURL);
	//
	// InitRequest initReq = new InitRequest();
	// initReq.setApiKey(this.apiKey);
	// initReq.setOrderId(orderId);
	// initReq.setCountry(this.country);
	// initReq.setCurrency(this.currency);
	// initReq.setPayType(this.paymentType);
	// initReq.setItems(items);
	// initReq.setProfile(profile);
	//
	// InitResult result = null;
	//
	//// if ("xml".equalsIgnoreCase(requestType)) {
	//// result = client.path("/init/")
	//// .type("application/xml")
	//// .accept("application/xml")
	//// .post(initReq, InitResult.class);
	//// } else {
	//
	// restTemplate.
	// logger.info("url:{},path:{},param:{}",airtimeRestURL,"/init/",
	// JsonUtils.toJson(initReq));
	//
	// result = client.path("/init/")
	// .type("application/json")
	// .accept("application/json")
	// .post(initReq, InitResult.class);
	//// }
	//
	// return result;
	// }

	private static final String AIRTIME_REST_INIT_URI = "/init/";

	public InitResult initTxn(String orderId, ArrayList<ItemInfo> items, HashMap<String, String> profile) {

		String initUrl = airtimeRestURL + AIRTIME_REST_INIT_URI;

		InitRequest initReq = new InitRequest();
		initReq.setApiKey(this.apiKey);
		initReq.setOrderId(orderId);
		initReq.setCountry(this.country);
		initReq.setCurrency(this.currency);
		initReq.setPayType(this.paymentType);
		initReq.setItems(items);
		initReq.setProfile(profile);
		Map<String, InitRequest> body = new HashMap<String, InitRequest>();
		body.put("initRequest", initReq);

		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");

		InitResult result = null;
		try {
			String resp = HttpClient.post(initUrl, headers, JsonUtils.toJson(body), 5000, "utf-8");
			logger.info("[initTxn] response", resp);
//			{"initResult":{"resultCode":0,"txnId":5367467180957773693}}
			JSONObject json=JSON.parseObject(resp);

			result =json.getJSONObject("initResult").toJavaObject(InitResult.class);
		} catch (Exception e) {
			logger.error("[initTxn] error", e);

			return null;
		}

		logger.debug("[initTxn] TxnId={}, Result={}", result.getTxnId(), result.getResultCode());
		return result;

	}

	// public PaymentResult inquiryTxn(RestTemplate restTemplate,long txnId) {
	// InquiryPaymentRequest request = new InquiryPaymentRequest();
	// request.setApiKey(apiKey);
	// request.setTxnId(txnId);
	//
	// WebClient client = WebClient.create(airtimeRestURL);
	// PaymentResult result = null;
	//
	// logger.info("url:{},path:{},param:{}",airtimeRestURL,"/inquiryPaymentResult/",
	// JsonUtils.toJson(request));
	// result = client.path("/inquiryPaymentResult/")
	// .type("application/xml")
	// .accept("application/xml")
	// .post(request, PaymentResult.class);
	//
	//
	// logger.debug(new StringBuffer("[inquiryTxn] TxnId=").append(txnId).append(",
	// Result=").append(result.getResultCode()).toString());
	// return result;
	// }

	public boolean validateChecksum(HttpServletRequest request) {
		try {
			String txnId = request.getParameter("TxnId");
			// String apiKey = ""; // Add Merchant APIKey
			String orderId = request.getParameter("OrderId");
			String resultCode = request.getParameter("ResultCode");
			String checksum = request.getParameter("Checksum");

			String values = txnId + apiKey + orderId + resultCode;

			byte[] b = HashUtils.MD5(values);
			String sum = HashUtils.convertToHex(b);

			return sum.equals(checksum);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public PaymentResult inquiryTxn(long txnId) {

		String inquiryPaymentUrl = airtimeRestURL + "/inquiryPaymentResult/";

		InquiryPaymentRequest request = new InquiryPaymentRequest();
		request.setApiKey(apiKey);
		request.setTxnId(txnId);

		Map<String, Object> body = new HashMap<String, Object>();
		body.put("inquiryPaymentRequest", request);

		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");

		PaymentResult result = null;
		try {
			String resp = HttpClient.post(inquiryPaymentUrl, headers, JsonUtils.toJson(body), 5000, "utf-8");

			logger.info("response:{}", resp);
			JSONObject json=JSON.parseObject(resp);
			result=	json.getJSONObject("inquiryPaymentResult").toJavaObject(PaymentResult.class);

		} catch (Exception e) {
			logger.error("[inquiryTxn] error:{}-{}", e.getMessage());
			return null;
		}

		logger.debug("[inquiryTxn] TxnId={}, Result={}", result.getTxnId(), result.getResultCode());

		return result;
	}

}
