package com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.dto.Authentication;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.dto.Payload;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.dto.PwgcApiRequest;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.dto.PwgcApiResponse;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.dto.TransactionPostback;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.helpers.JAXBHelper;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.helpers.OpenbucksHelper;
import com.woniu.sncp.pojo.payment.PaymentOrder;

@Service
public class OpenbucksPayment extends AbstractPayment {

	private final static Logger logger = LoggerFactory.getLogger(OpenbucksPayment.class);

	
	@Autowired
	private RestTemplate restTemplate;
	

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams) throws ValidationException {
		
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		
		Map<String, Object> extendsparam = JSON.parseObject(paymentOrder.getInfo(), new TypeReference<Map<String, Object>>(){});
		
		Map<String, Object> platformExt = JSON.parseObject(platform.getPlatformExt(), new TypeReference<Map<String, Object>>(){});
		
		String publicKey = platform.getBackendKey();
		String secretKey = platform.getPayKey();
		
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("payUrl", platform.getPayUrl());
		map.put("req_currency_code", paymentOrder.getMoneyCurrency());
		map.put("req_amount", paymentOrder.getMoney().toString());
		map.put("req_item_description", paymentOrder.getProductname());
		map.put("req_merchant_tracking_id", paymentOrder.getOrderNo());
		if (paymentOrder.getAid() != null) {
			map.put("req_customer_anonymous_id", String.valueOf(paymentOrder.getAid()));
		} else {
			map.put("req_customer_anonymous_id", "0");
		}
		map.put("req_public_key", publicKey);
		
		String token = OpenbucksHelper.getToken();
		map.put("req_token", token);
		//req_token + [secret_key] + req_merchant_tracking_id + req_amount + req_currency_code + req_force_cards
		String data = token  + secretKey + paymentOrder.getOrderNo() + paymentOrder.getMoney() + paymentOrder.getMoneyCurrency();
		map.put("req_hash", DigestUtils.sha256Hex(data));
		map.put("req_customer_info_email", MapUtils.getString(extendsparam, "email", ""));

		// optional values
		if (StringUtils.isNotBlank(paymentOrder.getGoodsDetail())) {
			Map<String, Object> productInfo = JSONObject.parseObject(paymentOrder.getGoodsDetail());
			map.put("req_product_id", MapUtils.getString(productInfo, "goods_id"));
		}
		map.put("req_success_url", platform.getFrontUrl(paymentOrder.getMerchantId()));
		map.put("req_cancel_url", platform.getFrontUrl(paymentOrder.getMerchantId()) + "?status=cancel");
		//map.put("req_select_card", "SELECT_CARD");
		//map.put("req_force_cards", "FORCE_CARD1,FORCE_CARD2,...");
		map.put("req_sub_property_id", paymentOrder.getGameId() + "");
		
		map.put("req_sub_property_name",  MapUtils.getString(extendsparam, "propertyName", ""));
		map.put("req_sub_property_url", MapUtils.getString(extendsparam, "propertyUrl", ""));
		
		String ratingModel = MapUtils.getString(platformExt, OpenbucksHelper.RATING_MODEL);
		String productRating = MapUtils.getString(platformExt, OpenbucksHelper.PRODUCT_RATING);
		if (StringUtils.isNotEmpty(ratingModel)) {
			map.put("req_rating_model", ratingModel);
		}
		if (StringUtils.isNotEmpty(productRating)) {
			map.put("req_product_rating", productRating);
		}
		
		return map;
	}


	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request, Platform platform)
			throws ValidationException, DataAccessException, PaymentRedirectException {
		
		String encoding = StringUtils.defaultIfEmpty(request.getCharacterEncoding(), "UTF-8");
		String postbackXml = null;
		try {
			postbackXml = IOUtils.toString(request.getInputStream(), encoding);
		} catch (IOException e) {
			throw new ValidationException("read request XML data error", e);
		}
		TransactionPostback postback = JAXBHelper.getInstance().fromXML(postbackXml, TransactionPostback.class);
		
		if (postback == null || !"0".equals(postback.getResponse().getError().getErrorCode())) {
			logger.info("交易失败 errorCode: {}, errorDescription: {}", 
					postback.getResponse().getError().getErrorCode(), postback.getResponse().getError().getErrorDescription());
			throw new ValidationException("Openbucks callback error");
		}
		
		String publicKey = platform.getBackendKey();
		String secretKey = platform.getPayKey();
		
		String transactionID = postback.getResponse().getPayment().getTransaction().getTransactionID();
		String pwgcTrackingID = postback.getResponse().getPayment().getTransaction().getPwgcTrackingID();
		String pwgcHash = postback.getResponse().getPayment().getTransaction().getPwgcHash();
		
		String currencyCode = postback.getResponse().getPayment().getAmount().getCurrencyCode();
		
		if (StringUtils.isEmpty(transactionID) || StringUtils.isEmpty(pwgcHash)) {
			logger.error("Billing result notification: parameter {} is empty or incorrect!", "transactionID or pwgcHash");
			throw new ValidationException("Openbucks check Parameter transactionID is incorrect");
		}
		
		//publicKey 
		if (!publicKey.equals(postback.getResponse().getPayment().getMerchantData().getPublicKey())) {
			logger.error("Billing result notification: verify publicKey is incorrect");
			throw new ValidationException("Billing result notification: verify publicKey is incorrect");
		}
		
		//Validate hash  publicKey + ":" + pwgcTrackingID + ":" + [secret_key] 
		String hash = DigestUtils.sha256Hex(publicKey + ":" + pwgcTrackingID + ":" + secretKey);
		if (!hash.equals(pwgcHash)) {
			logger.error("Billing result notification: verify pwgcHash is incorrect");
			throw new ValidationException("Billing result notification: verify pwgcHash is incorrect");
		}
		//Validate merchantTrackingID amountValue currencyCode
		String orderNo = postback.getResponse().getPayment().getMerchantData().getMerchantTrackingID();
		//TODO test
		orderNo = "20171129-1053-007-0000003733";
		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "Validate merchantTrackingID not empty,orderNo:" + orderNo);
		
		Map<String, Object> result = new LinkedHashMap<>();
		
		result.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		result.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		result.put(PaymentConstant.OPPOSITE_ORDERNO, transactionID);
		result.put(PaymentConstant.OPPOSITE_CURRENCY, currencyCode);
		result.put(PaymentConstant.OPPOSITE_MONEY, postback.getResponse().getPayment().getAmount().getAmountValue() * 100 + "");
		
		return result;
	}


	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		String publicKey = platform.getBackendKey();
		String token = OpenbucksHelper.getToken();
		//The SHA-256 hash of [token] + [api_secret_key]
		String hash = DigestUtils.sha256Hex(token + platform.getQueryKey());
		//Your merchant tracking ID as provided in req_merchant_tracking_id of the hidden form
		String trackingID = paymentOrder.getOrderNo();
		
//		PwgcApi.java
		PwgcApiRequest apiRequest = new PwgcApiRequest();
		PwgcApiRequest.Request request = new PwgcApiRequest.Request();
		request.setAuthentication(new Authentication(publicKey, token, hash));
		request.setTransactionPostback(new PwgcApiRequest.GetTransactionPostback("1.0", trackingID));
		apiRequest.setRequest(request);
		
		String requestXML = JAXBHelper.getInstance().toXML(apiRequest, "UTF-8", false, false);
		String apiUrl = platform.getQueryPrivateUrl();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_XML);
		
		HttpEntity<String> entity = new HttpEntity<String>(requestXML, headers);

		PwgcApiResponse response = restTemplate.postForObject(apiUrl, entity, PwgcApiResponse.class);;
	
		
		//验证errorCode
		if (!"0".equals(response.getResponse().getError().getErrorCode())) {
			logger.info("交易失败 errorCode: {}, errorDescription: {}", 
					response.getResponse().getError().getErrorCode(), response.getResponse().getError().getErrorDescription());
			throw new ValidationException("validate errorCode error");
		}
		
		//Validate the identity of the response by checking that the public key provided is actually yours
		if (!StringUtils.equals(publicKey, response.getResponse().getAuthentication().getPublicKey())) {
			logger.error("Billing result notification: verify publicKey is incorrect");
			throw new ValidationException("validate publicKey error");
		}
		Payload payload = response.getResponse().getTransactionPostback().getPayload();
		// Validate merchantTrackingID
		if (!StringUtils.equals(paymentOrder.getOrderNo(), payload.getPayment().getMerchantData().getMerchantTrackingID())) {
			logger.error("Billing result notification: parameter {} is empty or incorrect!", "merchantTrackingID");
			throw new ValidationException("validate merchantTrackingID error");
		}
		
		//验证token hash
		String respHash = "";
		String respToken = "";
		if (respHash == null || respToken == null) {
			
		}
		//publicKey + ":" + token + ":" + [API secret_key] 
		String backHash = DigestUtils.sha256Hex(publicKey + ":" + respToken + ":" + platform.getQueryKey());
		if (!StringUtils.equals(backHash, respHash)) {
			logger.error("Billing result notification: verify hash is incorrect");
			throw new ValidationException("validate hash error");
		}
		
		Map<String, Object> result = new LinkedHashMap<>();
		
		result.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		result.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		result.put(PaymentConstant.OPPOSITE_ORDERNO, payload.getPayment().getTransaction().getTransactionID());
		result.put(PaymentConstant.OPPOSITE_CURRENCY, payload.getPayment().getAmount().getCurrencyCode());
		result.put(PaymentConstant.OPPOSITE_MONEY, payload.getPayment().getAmount().getAmountValue() * 100);
		
		return result;
	}


	@Override
	public String encode(Map<String, Object> inParams) throws ValidationException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void paymentReturn(Map<String, Object> inParams, HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess) {
			response.setStatus(200);
			super.responseAndWrite(response, "{\"status\":200}");
		} else {
			response.setStatus(403);
			super.responseAndWrite(response, "{\"status\":403}");
		}
	}


	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		String tid = request.getParameter("tid");
		String status = request.getParameter("status");
		if (StringUtils.isNotEmpty(status)) {
			request.setAttribute(PaymentConstant.ORDER_FRONT_CALLBACK_STATUS, status);
		}
		return tid;
	}
}
