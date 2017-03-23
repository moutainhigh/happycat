package com.woniu.sncp.pay.core.service.payment.platform.shenzpay;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.PaymentConstant;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.repository.pay.PaymentOrder;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service("shenzpayDPPayment")
public class ShenzpayDPPayment extends AbstractPayment {
	
	
	private final String EMPTYSTRING = "";
	
	/**
	 * 神州付支付网关常量 - 神州行 - 0
	 */
	private final static String SHENZPAY_PRODUCT_TYPE_MOBILE = "0";
	/**
	 * 神州付支付网关常量 - 联通 - 1
	 */
	private final static String SHENZPAY_PRODUCT_TYPE_UNICOM = "1";
	/**
	 * 神州付支付网关常量 - 电信 - 2
	 */
	private final static String SHENZPAY_PRODUCT_TYPE_TELECOM = "2";

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		Map<String, Object> mapResult = new HashMap<String, Object>();
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		Map<String, Object> linkedParams = new LinkedHashMap<String, Object>();
		linkedParams.put("version", 3);
		linkedParams.put("merId", platform.getMerchantNo());
		linkedParams.put("payMoney",String.valueOf((new BigDecimal(paymentOrder.getMoney().toString())).multiply(new BigDecimal(100)).intValue()));
		linkedParams.put("orderId", paymentOrder.getOrderNo());
		linkedParams.put("returnUrl", platform.getBehindUrl(paymentOrder.getMerchantId()));
		
		String extend = platform.getPlatformExt();//渠道信息扩展
		String cardMoney = "";
		if(StringUtils.isNotBlank(extend) && extend.contains("cardMoney")){
			com.alibaba.fastjson.JSONObject extJson = com.alibaba.fastjson.JSONObject.parseObject(extend);
			cardMoney = extJson.getString("cardMoney");
		}
		//cardinfo 充值卡加密信息
		String cardInfo = ServerConnSzxUtils.getDesEncryptBase64String(
				StringUtils.isBlank(cardMoney)?String.valueOf((new BigDecimal(paymentOrder.getMoney().toString())).intValue()):cardMoney,
				(String)inParams.get("cardNo"), 
				(String)inParams.get("cardPwd"), platform.getPayKey());
		linkedParams.put("cardInfo", cardInfo);
		// 私有值：神州行、联通、电信？
		linkedParams.put("privateField",this.getPaymentProductTypeByProductType((String) inParams.get("procductType")));
		linkedParams.put("verifyType", 1); // 1.MD5校验 2.MD5校验+证书校验（暂不支持）
		linkedParams.put("privateKey", platform.getBackendKey());
		
		// 1.参数加密
		//md5String=MD5(version +merId+ payMoney + orderId + returnUrl + cardInfo 
		//+ privateField + verifyType+privateKey)
		String md5SourceString = getMd5Source(linkedParams);
		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", md5SourceString);
		String encrypted = this.encode(encodeParams);
		
		// 2.向神州付平台发送请求
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(platform.getPayUrl());
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("version", "3"));
		params.add(new BasicNameValuePair("merId", platform.getMerchantNo()));
		params.add(new BasicNameValuePair("payMoney", String.valueOf((new BigDecimal(paymentOrder.getMoney().toString())).multiply(new BigDecimal(100)).intValue())));
		params.add(new BasicNameValuePair("orderId", paymentOrder.getOrderNo()));
		params.add(new BasicNameValuePair("returnUrl", platform.getBehindUrl(paymentOrder.getMerchantId())));
		params.add(new BasicNameValuePair("cardInfo", cardInfo));
		params.add(new BasicNameValuePair("merUserName", ""));//商户姓名
		params.add(new BasicNameValuePair("merUserMail", ""));//商户邮箱
		params.add(new BasicNameValuePair("privateField", this.getPaymentProductTypeByProductType((String) inParams.get("procductType"))));
		params.add(new BasicNameValuePair("verifyType", "1"));
		params.add(new BasicNameValuePair("cardTypeCombine", this.getPaymentProductTypeByProductType((String) inParams.get("procductType"))));
		params.add(new BasicNameValuePair("md5String", encrypted));
		params.add(new BasicNameValuePair("signString", ""));
		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(params, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		httpPost.setEntity(entity);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		try {
			logger.info("请求神州付支付，请求：" + params);
			responseBody = httpclient.execute(httpPost, responseHandler);
			logger.info("请求神州付支付，返回：" + responseBody);
			//如果返回200 表示请求成功
			mapResult.put("msgcode", StringUtils.trim(responseBody));
		} catch (Exception e) {
			throw new ValidationException("神州付请求订单返回失败", e);
		} finally{
			httpclient.getConnectionManager().shutdown();
		}
		return mapResult;
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException, DataAccessException,
			PaymentRedirectException {
		String version = StringUtils.trim(request.getParameter("version"));
		String merId = StringUtils.trim(request.getParameter("merId"));
		String payMoney = StringUtils.trim(request.getParameter("payMoney"));
		String orderId = StringUtils.trim(request.getParameter("orderId"));
		String payResult = StringUtils.trim(request.getParameter("payResult"));
		String privateField = StringUtils.trim(request.getParameter("privateField"));
		String payDetails = StringUtils.trim(request.getParameter("payDetails"));
		String md5String = StringUtils.trim(request.getParameter("md5String"));
		String signString = request.getParameter("signString");//神州付证书签名
	    String cardMoney = request.getParameter("cardMoney");   

		// 1.我方加密数据
		LinkedHashMap<String, Object> encryParams = new LinkedHashMap<String, Object>();
		encryParams.put("version", version);
		encryParams.put("merId", merId);
		encryParams.put("payMoney", payMoney);
		encryParams.put("orderId", orderId);
		encryParams.put("payResult", payResult);
		encryParams.put("privateField", privateField);
		encryParams.put("payDetails", payDetails);
		encryParams.put("privateKey", platform.getBackendKey());

		String source = getMd5Source(encryParams);

		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", source);
		String encrypted = this.encode(encodeParams);

		// 2.比较是否相等
		if (!encrypted.equalsIgnoreCase(md5String)) {
			if (logger.isInfoEnabled()) {
				logger.info("==============神州付后台加密处理失败=================");
				logger.info("我方加密串：" + encrypted);
				logger.info("对方加密串：" + md5String);
				logger.info("==============神州付后台加密处理结束=================\n");
			}
			throw new ValidationException("支付平台加密校验失败");
		}

		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderId);
		Assert.notNull(paymentOrder, "神州付支付订单查询为空,orderId:" + orderId);

		Map<String, Object> returned = new HashMap<String, Object>();
		if ("1".equals(payResult)) { // 支付成功
			logger.info("神州付返回支付成功");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else { // 支付失败
			String errcode = request.getParameter("errcode");//实际错误码
			logger.info("神州付返回支付失败，订单号： "+orderId+",errcode：" +  errcode);
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_FAILED);
		}
		
		// 支付模式-从privateField中判断是神州行、联通、电信？
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.ORDER_NO, orderId);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, orderId);
		// 金额单位：分，校验无需乘以100
		returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf(NumberUtils.toFloat(payMoney)));
		return returned;
	}

	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		String version = "3";
		String merId = platform.getMerchantNo();
		String orderId = paymentOrder.getOrderNo();

		// 1.加密
		LinkedHashMap<String, Object> encryParams = new LinkedHashMap<String, Object>();
		encryParams.put("version", version);
		encryParams.put("merId", merId);
		encryParams.put("orderIds", orderId);
		encryParams.put("queryBegin", "");
		encryParams.put("queryEnd", "");
		encryParams.put("privateKey", platform.getPrivatePassword());

		String source = getMd5Source(encryParams);
		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", source);
		String encrypted = this.encode(encodeParams);

		// 2.向神州付请求参数
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(platform.getPayCheckUrl());
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("version", version));
		params.add(new BasicNameValuePair("merId", merId));
		params.add(new BasicNameValuePair("orderIds", orderId));
		params.add(new BasicNameValuePair("queryBegin", ""));
		params.add(new BasicNameValuePair("queryEnd", ""));
		params.add(new BasicNameValuePair("md5", encrypted.toLowerCase()));
		params.add(new BasicNameValuePair("resultFormat", "0"));
		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(params, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		httpPost.setEntity(entity);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		try {
			responseBody = httpclient.execute(httpPost, responseHandler);
		} catch (Exception e) {
			throw new ValidationException("神州付订单校验返回失败", e);
		} finally{
			httpclient.getConnectionManager().shutdown();
		}
		

		Map<String, Object> outParams = new HashMap<String, Object>();

		if (StringUtils.isBlank(responseBody)) {
			throw new ValidationException("神州付订单验证返回responseBody为空");
		}

		// 3.请求返回
		if (logger.isInfoEnabled()) {
			logger.info("神州付订单校验返回结果：" + responseBody);
		}
		Map<String, Object> map = JsonUtils.jsonToMap(responseBody);

		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		if (map != null) {
			if ("001".equals(map.get("queryResult"))) {
				throw new ValidationException("神州付订单验证返回:[001-参数错误]");
			} else if ("002".equals(map.get("queryResult"))) {
				throw new ValidationException("神州付订单验证返回:[002-商户不存在]");
			} else if ("003".equals(map.get("queryResult"))) {
				throw new ValidationException("神州付订单验证返回:[003-md5校验失败]");
			} else if ("000".equals(map.get("queryResult"))) {
				JSONArray jsonArray = JSONArray.fromObject(map.get("orders"));
				if (jsonArray == null || jsonArray.isEmpty()){ // 神州付未查询到我方订单
					outParams.put(PaymentConstant.PAYMENT_STATE, payState);
					return outParams;
				}
				JSONObject obj = (JSONObject) jsonArray.get(0);// 由于我们只有一个订单。这里直接get(0)就可以了
				String orderNo = (String) obj.get("orderId");
				Integer payMy = (Integer) obj.get("payMoney");
				Integer payStatus = (Integer) obj.get("payStatus");
				if ("0".equals(payStatus.toString())) {
					payState = PaymentConstant.PAYMENT_STATE_FAILED;// 支付失败
				} else if ("1".equals(payStatus.toString())) {
					payState = PaymentConstant.PAYMENT_STATE_PAYED;// 支付成功
				} else {
					payState = PaymentConstant.PAYMENT_STATE_NOPAYED;// 未支付
				}
				outParams.put(PaymentConstant.OPPOSITE_ORDERNO, orderNo);// 订单号
				outParams.put(PaymentConstant.OPPOSITE_MONEY, payMy);// 订单金额
			}
		}

		outParams.put(PaymentConstant.PAYMENT_STATE, payState);
		return outParams;
	}

	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		String encrypted = "";
		try {
			encrypted = MD5Encrypt.encrypt((String) inParams.get("source"), "utf-8");
		} catch (RuntimeException e) {
			logger.error("神州付支付加密异常", e);
			throw new ValidationException("神州付支付加密异常", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("=========神州付支付加密开始=========");
			logger.info("source：" + inParams.get("source"));
			logger.info("encrypted：" + encrypted);
			logger.info("=========神州付支付加密结束=========\n");
		}
		return encrypted;
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess){
			HttpServletRequest request = (HttpServletRequest) inParams.get("request");
			String orderId = this.getOrderNoFromRequest(request);
			super.responseAndWrite(response, orderId);
		}else{
			super.responseAndWrite(response, "shenzPay_imprest_fail,maybe payment is invalid");
		}
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("orderId");
	}
	
	/**
	 * 根据前台传过来的产品类型(SZX、UNICOM、TELECOM)获取支付平台的产品类型(生成订单提交到对方时的参数)
	 * 
	 * @param productType
	 * @return
	 */
	private String getPaymentProductTypeByProductType(String productType) {
		if (PaymentConstant.PAYMENT_PRODUCTION_TYPE_SZX.equals(productType))
			return ShenzpayDPPayment.SHENZPAY_PRODUCT_TYPE_MOBILE;
		else if (PaymentConstant.PAYMENT_PRODUCTION_TYPE_UNICOM.equals(productType))
			return ShenzpayDPPayment.SHENZPAY_PRODUCT_TYPE_UNICOM;
		else if (PaymentConstant.PAYMENT_PRODUCTION_TYPE_TELECOM.equals(productType))
			return ShenzpayDPPayment.SHENZPAY_PRODUCT_TYPE_TELECOM;
		else
			throw new ValidationException("神州付只支持[SZX、UNICOM、TELECOM]，页面传递类型不符[" + productType + "]");
	}
	
	private String getMd5Source(Map<String, Object> map) {
		StringBuffer sb = new StringBuffer();
		for (Iterator<Entry<String, Object>> keyValue = map.entrySet().iterator(); keyValue.hasNext();) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) keyValue.next();
			// String key = entry.getKey();
			String value = ObjectUtils.toString(entry.getValue());
			if (value != null) {
				sb.append(value);
			} else {
				sb.append(EMPTYSTRING);
			}

		}
		String source = sb.toString();
		return source;
	}
	
	/**
     * 服务商       卡号位数   	密码位数
     * 江苏移动	16		17
     * 辽宁移动	16		21
     * 全国移动	17		18
     * 浙江移动	10		8
     * 福建移动	16		17
     * 全国电信	19		18
     * 全国联通	15		19
     * 
     * 未定义 ： -1
     * 移动: 0
     * 联通 : 1
     * 电信 : 2
     * @param cardNo
     * @param cardPassword
     * @return
     */
    public static String getCardType(String cardNo,String cardPassword){
    	if(isChinaMobileCard(cardNo, cardPassword)){
    		return PaymentConstant.PAYMENT_PRODUCTION_TYPE_SZX;
    	} else if(isUnicomCard(cardNo, cardPassword)){
    		return PaymentConstant.PAYMENT_PRODUCTION_TYPE_UNICOM;
    	} else if(isTelecCard(cardNo, cardPassword)){
    		return PaymentConstant.PAYMENT_PRODUCTION_TYPE_TELECOM;
    	}
    	
    	return "-1";
    }
    
    /**
     * 验证卡和密码是否正确
     * @param cardNo
     * @param cardPassword
     * @return
     */
    public static boolean isValidCard(String cardNo,String cardPassword){
    	if(isChinaMobileCard(cardNo, cardPassword)
    			|| isTelecCard(cardNo, cardPassword)
    			|| isUnicomCard(cardNo, cardPassword)){
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public static boolean isChinaMobileCard(String cardNo,String cardPassword){
    	int cardNoLen = cardNo.length();
    	int cardPwdLen = cardPassword.length();
    	
    	if(cardNoLen == 16 && cardPwdLen == 17){
			return true;
		}
		
		if(cardNoLen == 16 && cardPwdLen == 21){
			return true;
		}
		
		if(cardNoLen == 17 && cardPwdLen == 18){
			return true;
		}
		
		if(cardNoLen == 10 && cardPwdLen == 8){
			return true;
		}
		
		if(cardNoLen == 16 && cardPwdLen == 17){
			return true;
		}
		
		return false;
    }
    
    public static boolean isTelecCard(String cardNo,String cardPassword){
    	int cardNoLen = cardNo.length();
    	int cardPwdLen = cardPassword.length();
    	
    	if(cardNoLen == 19 && cardPwdLen == 18){
			return true;
		}
		
		return false;
    }

    public static boolean isUnicomCard(String cardNo,String cardPassword){
    	int cardNoLen = cardNo.length();
    	int cardPwdLen = cardPassword.length();
    	
    	if(cardNoLen == 15 && cardPwdLen == 19){
			return true;
		}
		
		return false;
    }

}
