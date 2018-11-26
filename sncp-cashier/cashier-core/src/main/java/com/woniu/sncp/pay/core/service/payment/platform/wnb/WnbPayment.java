package com.woniu.sncp.pay.core.service.payment.platform.wnb;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
 
import com.woniu.sncp.pay.common.utils.http.IpUtils;
import com.woniu.sncp.pay.core.security.SecuritySSOAuth;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;


import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.HttpClient;
import com.woniu.sncp.pay.core.service.IssuerComparisonService;
import com.woniu.sncp.pay.core.service.MemcachedService;
import com.woniu.sncp.pay.core.service.PaymentOrderService;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.repository.pay.IssuerComparison;
import com.woniu.sncp.pojo.payment.PaymentOrder;

import okhttp3.HttpUrl;

@Service("WnbPayment")
public class WnbPayment extends AbstractPayment {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Resource
	IssuerComparisonService issuerComparisonService;
	protected final String _charset_encode = "utf-8";
	@Autowired
	MemcachedService memcachedService;
	@Resource
	private PaymentOrderService paymentOrderService;
	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams) {

		// {
		// accessId: 2010,
		// accessType: 8,
		// accessKey: 'grssiAYYUjvRPUV',
		// accessPasswd: 'v2fz2wN8hnPqB3'
		// }
		// 1.拼装参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		String orderNo = paymentOrder.getOrderNo();

		Long aid = SecuritySSOAuth.getLoginId();
		Assert.notNull(aid, "用户没有登录");
		
		
		 
			String info = paymentOrder.getInfo();
			JSONObject infoJson=JSON.parseObject(info);
 			String productCode =infoJson.getString("productcode");
			
			Assert.hasLength(productCode, "orderNo:" + orderNo + ",蜗牛币支付，productcode不能为空");
			IssuerComparison issuerComparison=issuerComparisonService.findIssuerComparison(1L, productCode);
			Assert.notNull(issuerComparison, "orderNo:" + orderNo + ",运营商对照表，productcode 不存在");
			String otherMark=issuerComparison.getOtherMark();			
			int wnbPoint=new BigDecimal( JSON.parseObject(otherMark).getString("wnb")).intValue();			
			
			infoJson.put("wnbpoints", wnbPoint);
	 
 
			paymentOrderService.updateOrderInfo(paymentOrder, infoJson.toString());
 	 
		String ext = ObjectUtils.toString(platform.getExtend());
		String accessId = null;
		String accessType = null;
		String accessKey = null;
		String accessPasswd = null;

		if (StringUtils.isNotBlank(ext)) {
			JSONObject extend = JSONObject.parseObject(ext);
			accessId = extend.getString("accessId");
			accessType = extend.getString("accessType");
			accessKey = extend.getString("accessKey");
			accessPasswd = extend.getString("accessPasswd");
		}
		String payUrl = platform.getPayUrl();
		String resp = null;
		try {
			Map<String, Object> initRequest = new HashMap<String, Object>();
			Map<String, String> headers = new HashMap<String, String>();

			headers.put("accessId", accessId);
			headers.put("accessType", accessType);
			headers.put("accessPasswd", accessPasswd);
			headers.put("second", System.currentTimeMillis() + "");
			headers.put("signVersion", "1.0");
			Map<String, String> params = new HashMap<>();
			String note = paymentOrder.getProductname();
			String serverId = ObjectUtils.toString(paymentOrder.getGareaId());
			String gameId = ObjectUtils.toString(paymentOrder.getGameId());
			String clientIp = IpUtils.longToIp(paymentOrder.getIp());
		

 			String backendUrl = platform.getBehindUrl(paymentOrder.getMerchantId());
 			 
//	        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
//	        request.getParameter("paypwd")
			String payPassword =ObjectUtils.toString( inParams.get("paypwd"));
			Assert.hasLength(payPassword, "payPassword 不能为空");
			String query = String.format(
					// @formatter:off
					"{\r\n" + 
					"  woniuCoinDecuct(globalRequest:{aid:\"%s\",note:\"%s\",serverId:\"%s\",orderNo:\"%s\",gameId:\"%s\",point:\"%s\",clientIp:\"%s\",paypasswd:\"%s\",callback:\"%s\"}){\r\n" + 
					"    code,\r\n" + 
					"    message,\r\n" + 
					"    success,\r\n" + 
					"		data{\r\n" + 
					"      aid\r\n" + 
					"      balance\r\n" + 
					"      orderId\r\n" + 
					"      orderNo\r\n" + 
					"    }    \r\n" + 
					"    \r\n" + 
					"  }\r\n" + 
					"}",
					// @formatter:on


					ObjectUtils.toString(aid), note, serverId, orderNo, gameId, wnbPoint, clientIp, payPassword, backendUrl);

			params.put("query", query);

			String body=JsonUtils.toJson(params);
			// {query:'{ checkPay(globalRequest: {}) { code message success data { balance
			// email existPwd } } }'},
			headers.put("content-type", "application/json");

			String accessVerify = SignatureUtils.signature(HttpUrl.get(payUrl).encodedPath(), "POST", body, headers, null, accessKey);

			headers.put("accessVerify", accessVerify);
			
			
			initRequest.put("headers", headers);
 			initRequest.put("body", body);
 			initRequest.put("method", "POST");

			resp = HttpClient.fetch(payUrl, initRequest).body().string();
		} catch (IOException e) {
			logger.error(" orderNo: " + orderNo, e);
			e.printStackTrace();
		}

		logger.info("orderNo:{}, response:{}", orderNo, resp);
		JSONObject json = JSONObject.parseObject(resp);
		JSONObject	woniuCoinDecuct=json.getJSONObject("data").getJSONObject("woniuCoinDecuct");
			Map<String, Object> result = new HashMap<String, Object>();

		if (woniuCoinDecuct.getBoolean("success")) {
			JSONObject data = woniuCoinDecuct.getJSONObject("data");
			if (data != null) {
				String oppositeOrderNo = data.getString("orderId");
				result.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);
			}
			String frontUrl = platform.getFrontUrl(paymentOrder.getMerchantId()) + "?orderNo=" + orderNo;
			result.put("payUrl", frontUrl);
		}

		// Map<String, Object> datas = JsonUtils.jsonToMap(resp);
		// String code = ObjectUtils.toString(datas.get("code"));
		//
		// if (StringUtils.equals("MSG001", code)) {
		//
		// String oppositeOrderNo = ObjectUtils.toString(datas.get("token"));
		// String url = ObjectUtils.toString(datas.get("url"));
		// memcachedService.set("paybyone@token:"+oppositeOrderNo, 24*60*60, orderNo);
		//
		//
		//
		// Assert.isTrue(StringUtils.equalsIgnoreCase(paymentOrder.getMoneyCurrency(),"USD"),paymentOrder.getOrderNo()+"只支持美元");
		//// result.put(PaymentConstant.OPPOSITE_CURRENCY, "USD");//paybyone 只支持USD
		// result.put("payUrl", url);
		// }

		// params.put("method", "post");

		return result;
	}



	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		String oppositeOrderNo = request.getParameter("token");
		String orderNo = ObjectUtils.toString(memcachedService.get("paybyone@token:" + oppositeOrderNo));
		return orderNo;
		// return request.getParameter("transactionId");

	}

	@Override
	public void paymentReturn(Map<String, Object> inParams, HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess)
			super.responseAndWrite(response, "success");
		else

			super.responseAndWrite(response, "fail");
	}

	public Map<String, String> toParameterMap(HttpServletRequest req) {
		Map<String, String> map = new HashMap<String, String>();
		Enumeration e = req.getParameterNames();

		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String val = req.getParameter(key);

			map.put(key, val);
		}

		return map;
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request, Platform platform) throws ValidationException, DataAccessException, PaymentRedirectException {
		logger.info("validateBackParams params:{}", JsonUtils.toJson(toParameterMap(request)));
 
		String orderNo = request.getParameter("payorderno");

		
		
		
		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "支付订单查询为空,orderNo:" + orderNo);

 
		 
		String ext = ObjectUtils.toString(platform.getExtend());
		String accessId = null;
		String accessType = null;
		String accessKey = null;
		String accessPasswd = null;

		if (StringUtils.isNotBlank(ext)) {
			JSONObject extend = JSONObject.parseObject(ext);
			accessId = extend.getString("accessId");
			accessType = extend.getString("accessType");
			accessKey = extend.getString("accessKey");
			accessPasswd = extend.getString("accessPasswd");
		}
		String url = platform.getPayCheckUrl();
	 
			Map<String, Object> initRequest = new HashMap<String, Object>();
			Map<String, String> headers = new HashMap<String, String>();

			headers.put("accessId", accessId);
			headers.put("accessType", accessType);
			headers.put("accessPasswd", accessPasswd);
			headers.put("second", System.currentTimeMillis() + "");
			headers.put("signVersion", "1.0");
			Map<String, String> params = new HashMap<>();
		
	String query=String.format("{\r\n" + 
			"  woniuCoinOrderQuery(globalRequest:{orderNo:\"%s\"}){\r\n" + 
			"    code,\r\n" + 
			"    message,\r\n" + 
			"    success,\r\n" + 
			"		data{\r\n" + 
			"      aid\r\n" + 
			"      gameId\r\n" + 
			"      points\r\n" + 
			"      serverId\r\n" + 
			"      orderId\r\n" + 
			"    }    \r\n" + 
			"    \r\n" + 
			"  }\r\n" + 
			"}",orderNo);
		
		params.put("query", query);

		String body = JsonUtils.toJson(params);
		headers.put("content-type", "application/json");

		String accessVerify = SignatureUtils.signature(HttpUrl.get(url).encodedPath(), "POST", body, headers, null, accessKey);

		headers.put("accessVerify", accessVerify);

		initRequest.put("headers", headers);
		initRequest.put("body", body);
		initRequest.put("method", "POST");
		String resp = null;
		try {
			resp = HttpClient.fetch(url, initRequest).body().string();
		} catch (IOException e) {
			logger.error(" orderNo: " + orderNo, e);
			e.printStackTrace();
		}

		logger.info("orderNo:{}, response:{}", orderNo, resp);
		JSONObject json = JSONObject.parseObject(resp);
		JSONObject data = json.getJSONObject("data").getJSONObject("woniuCoinOrderQuery").getJSONObject("data");
		String points=null;
		String oppositeOrderNo =null;
		 if(data!=null) {
			 points=data.getString("points");
			 oppositeOrderNo=data.getString("orderId");
		 }
			String info = paymentOrder.getInfo();
			JSONObject infoJson=JSON.parseObject(info);
			infoJson.put("wnbpoints", points);
 
			String wnb=infoJson.getString("wnbpoints");
		
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		Map<String, Object> outParams = new HashMap<String, Object>();
		if(new BigDecimal(wnb).compareTo(new BigDecimal(points))==0) {
			payState = PaymentConstant.PAYMENT_STATE_PAYED;
			outParams.put(PaymentConstant.OPPOSITE_MONEY, new BigDecimal(paymentOrder.getMoney()+"").multiply(new BigDecimal("100")).toString());//直接使用定单金额
		}

 

		outParams.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);

		outParams.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo); // 对方订单号
		outParams.put(PaymentConstant.PAYMENT_STATE, payState);
		outParams.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());

		return outParams;

	}

	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		// 1.拼装地址
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		String orderNo=paymentOrder.getOrderNo();
		 
			String ext = ObjectUtils.toString(platform.getExtend());
			String accessId = null;
			String accessType = null;
			String accessKey = null;
			String accessPasswd = null;

			if (StringUtils.isNotBlank(ext)) {
				JSONObject extend = JSONObject.parseObject(ext);
				accessId = extend.getString("accessId");
				accessType = extend.getString("accessType");
				accessKey = extend.getString("accessKey");
				accessPasswd = extend.getString("accessPasswd");
			}
			String url = platform.getPayCheckUrl();
		 
				Map<String, Object> initRequest = new HashMap<String, Object>();
				Map<String, String> headers = new HashMap<String, String>();

				headers.put("accessId", accessId);
				headers.put("accessType", accessType);
				headers.put("accessPasswd", accessPasswd);
				headers.put("second", System.currentTimeMillis() + "");
				headers.put("signVersion", "1.0");
				Map<String, String> params = new HashMap<>();
			
		String query=String.format("{\r\n" + 
				"  woniuCoinOrderQuery(globalRequest:{orderNo:\"%s\"}){\r\n" + 
				"    code,\r\n" + 
				"    message,\r\n" + 
				"    success,\r\n" + 
				"		data{\r\n" + 
				"      aid\r\n" + 
				"      gameId\r\n" + 
				"      points\r\n" + 
				"      serverId\r\n" + 
				"      orderId\r\n" + 
				"    }    \r\n" + 
				"    \r\n" + 
				"  }\r\n" + 
				"}}",orderNo);
			
			params.put("query", query);

			String body = JsonUtils.toJson(params);
			headers.put("content-type", "application/json");

			String accessVerify = SignatureUtils.signature(HttpUrl.get(url).encodedPath(), "POST", body, headers, null, accessKey);

			headers.put("accessVerify", accessVerify);

			initRequest.put("headers", headers);
			initRequest.put("body", body);
			initRequest.put("method", "POST");
			String resp = null;
			try {
				resp = HttpClient.fetch(url, initRequest).body().string();
			} catch (IOException e) {
				logger.error(" orderNo: " + orderNo, e);
				e.printStackTrace();
			}

			logger.info("orderNo:{}, response:{}", orderNo, resp);
			JSONObject json = JSONObject.parseObject(resp);
			JSONObject data = json.getJSONObject("data").getJSONObject("woniuCoinOrderQuery").getJSONObject("data");
			String points=null;
			String oppositeOrderNo=null;
			 if(data!=null) {
				 points=data.getString("points");
				 oppositeOrderNo=data.getString("orderId");
 			 }
				String info = paymentOrder.getInfo();
				JSONObject infoJson=JSON.parseObject(info);
 
				String wnb=infoJson.getString("wnbpoints");
			
			String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
			Map<String, Object> outParams = new HashMap<String, Object>();
			if(new BigDecimal(wnb).compareTo(new BigDecimal(points))==0) {
				payState = PaymentConstant.PAYMENT_STATE_PAYED;
				outParams.put(PaymentConstant.OPPOSITE_MONEY, new BigDecimal(paymentOrder.getMoney()+"").multiply(new BigDecimal("100")).toString());//直接使用定单金额
			}

	 

			outParams.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);

			outParams.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo); // 对方订单号
			outParams.put(PaymentConstant.PAYMENT_STATE, payState);
			outParams.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());

			return outParams;

		
		
		
		
 

	}

	@Override
	public String encode(Map<String, Object> inParams) throws ValidationException {
		return null;
	}

	 

	@Override
	public void backendResponse(Map<String, Object> params, HttpServletResponse response, boolean isSccess) {
		if (isSccess) {

			super.responseAndWrite(response, "success");
		} else {
			response.setStatus(500);
			super.responseAndWrite(response, "fail");

		}

	}

}
