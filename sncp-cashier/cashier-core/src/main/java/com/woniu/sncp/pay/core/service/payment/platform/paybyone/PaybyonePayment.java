package com.woniu.sncp.pay.core.service.payment.platform.paybyone;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.woniu.sncp.pay.core.service.MemcachedService;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;

@Service("PaybyonePayment")
public class PaybyonePayment extends AbstractPayment {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final String _charset_encode = "utf-8";
	@Autowired
	MemcachedService memcachedService;
	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams) {
		// 1.拼装参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		// String payUrl = url +
		// String.format("?productId=%s&transactionId=%s&price=%s", productId, orderNo,
		// price);
		Map<String, Object> params = new HashMap<String, Object>();
		String orderNo = paymentOrder.getOrderNo();
		String merchantNo = platform.getMerchantNo();
		String paykey = platform.getPayKey();
		params.put("version", "1.0");
		params.put("client_id", merchantNo);
		params.put("app_user_id", orderNo);
		params.put("app_user_nickname", orderNo);
		params.put("item", paymentOrder.getProductname());
		params.put("amount", paymentOrder.getMoney());

		String access_key = accessKey(merchantNo, paykey, params.get("version"), params.get("app_user_id"), params.get("item"), params.get("amount"));

		params.put("access_key", access_key.toLowerCase());
		String payUrl = platform.getPayUrl();

		Map<String, Object> initRequest = new HashMap<String, Object>();
		initRequest.put("body", params);
		initRequest.put("method", "post");

	
		String resp = null;
		try {
 			resp = HttpClient.fetch(payUrl, initRequest).body().string();
		} catch (IOException e) {
			logger.error("orderNo:" + orderNo, e);
			e.printStackTrace();
		}
		logger.info("orderNo:{},response:{}", orderNo, resp);
		Map<String, Object> datas = JsonUtils.jsonToMap(resp);
		String code = ObjectUtils.toString(datas.get("code"));
		Map<String, Object> result = new HashMap<String, Object>();

		if (StringUtils.equals("MSG001", code)) {

			String oppositeOrderNo = ObjectUtils.toString(datas.get("token"));
			String url = ObjectUtils.toString(datas.get("url"));
			memcachedService.set("paybyone@token:"+oppositeOrderNo, 24*60*60, orderNo);
			

			result.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);
			result.put(PaymentConstant.OPPOSITE_CURRENCY, "USD");//paybyone  只支持USD
			result.put("payUrl", url);
		}

		// params.put("method", "post");

		return result;
	}

	private String accessKey(Object... strings) {
		String string = StringUtils.join(strings,":").toLowerCase();
		String sign= MD5Encrypt.encrypt(string,"utf-8");
		logger.info("sign string:{},value:{}",string,sign);
		return sign;

	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		String oppositeOrderNo=request.getParameter("token");
		String orderNo=ObjectUtils.toString( memcachedService.get("paybyone@token:"+oppositeOrderNo));
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

		String oppositeOrderNo = request.getParameter("token");
		String orderNo=ObjectUtils.toString( memcachedService.get("paybyone@token:"+oppositeOrderNo));

		// 订单查询
		PaymentOrder paymentOrder =	paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "支付订单查询为空,orderNo:" + orderNo);

		String merchantNo = platform.getMerchantNo();
		String paykey = platform.getPayKey();
		String access_key = accessKey(merchantNo, paykey, oppositeOrderNo, request.getParameter("amount"),request.getParameter("trnStatus"));
		
		Assert.isTrue(StringUtils.equalsIgnoreCase(access_key, request.getParameter("access_key")),"签名信息不一致");
		

		
		String payCheckUrl=platform.getPayCheckUrl();
		Map<String,Object> initRequest=new HashMap<String,Object>();
		initRequest.put("method", "get");
		Map<String,Object> headers=new HashMap<String,Object>();
		headers.put("token", oppositeOrderNo);
		initRequest.put("headers", headers);
		String resp=null;
		
 
		try {
			resp = HttpClient.fetch(payCheckUrl, initRequest).body().string();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("pay check  orderNo:{},resp:{}",paymentOrder.getOrderNo(),resp);
		Map<String,Object> map=JsonUtils.jsonToMap(resp);
		String code =ObjectUtils.toString(map.get("code"));
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		Map<String, Object> outParams = new HashMap<String, Object>();

		if(StringUtils.equals(code, "MSG001")) {
 			 
			Map<String,Object> data=(Map)map.get("data");
			String status=ObjectUtils.toString(data.get("status"));
			if(StringUtils.equals("2", status)) {
				logger.info("支付成功");
				payState = PaymentConstant.PAYMENT_STATE_PAYED;
				String amount=ObjectUtils.toString(data.get("realAmount"));
				String money=new BigDecimal(amount).multiply(new BigDecimal("100")).toString();//传回来的是负数，并转换为分单位
				outParams.put(PaymentConstant.OPPOSITE_MONEY,money);
//				outParams.put(PaymentConstant.OPPOSITE_CURRENCY, "US");
			}
			
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

		String currencyStr = paymentOrder.getMoneyCurrency();
		logger.info("paymentOrder:{}, MoneyCurrency:{}", paymentOrder.getOrderNo(), currencyStr);
		String oppositeOrderNo = paymentOrder.getOtherOrderNo();
 
		String payCheckUrl=platform.getPayCheckUrl();
		Map<String,Object> initRequest=new HashMap<String,Object>();
		initRequest.put("method", "post");
		Map<String,Object> body=new HashMap<String,Object>();
		body.put("token", oppositeOrderNo);
		initRequest.put("body", body);
		String resp=null;
		try {
			resp = HttpClient.fetch(payCheckUrl, initRequest).body().string();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("pay check  orderNo:{},resp:{}",paymentOrder.getOrderNo(),resp);
		Map<String,Object> map=JsonUtils.jsonToMap(resp);
		String code =ObjectUtils.toString(map.get("code"));
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		
		Map<String, Object> outParams = new HashMap<String, Object>();

		if(StringUtils.equals(code, "MSG001")) {
			Map<String,Object> data=(Map)map.get("data");
			String status=ObjectUtils.toString(data.get("status"));
			if(StringUtils.equals("2", status)) {
				logger.info("支付成功");
				payState = PaymentConstant.PAYMENT_STATE_PAYED;
				String amount=ObjectUtils.toString(data.get("realAmount"));
				String money=new BigDecimal(amount).multiply(new BigDecimal("100")).toString();//传回来的是负数，并转换为分单位
				outParams.put(PaymentConstant.OPPOSITE_MONEY,money);
			}
		}
		

		 

//		String payResult = StringUtils.trim(transaction.getString("status"));
//
//		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
//
//		if (StringUtils.equals(orderNo, transaction.getString("order-id")) && StringUtils.equals(payResult, "COMPLETE")) { // 支付成功
//			logger.info("BOA返回支付成功");
//
//			String paymentMoney = transaction.getString("amount");
//			BigDecimal orderModey = new BigDecimal(paymentOrder.getMoney().toString()).setScale(2).multiply(new BigDecimal("100"));
//			BigDecimal totalPrice = new BigDecimal(paymentMoney).setScale(2).multiply(new BigDecimal("100"));
//
//			outParams.put(PaymentConstant.OPPOSITE_MONEY, orderModey.toString());
//			if (totalPrice.compareTo(orderModey) == 0) {
//				logger.info("支付成功");
//				payState = PaymentConstant.PAYMENT_STATE_PAYED;
//			}
//
//		} else { // 未支付
//			logger.info("BOA返回未支付");
//		}
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

	// @Override
	// public Map<String, Object> backendParamsValidate(HttpServletRequest request,
	// Platform platform) throws ValidationException, DataAccessException {
	// // 验证notify_url
	// String notifyId = StringUtils.trim(request.getParameter("notify_id"));
	// String partner = platform.getMerchantNo();
	// String alipayNotifyURL = platform.getPayCheckUrl() +
	// "?service=notify_verify&partner=" + partner + "&notify_id=" + notifyId;
	// String responseTxt = AlipayHelper.checkURL(alipayNotifyURL);
	// if (!"true".equals(responseTxt)) {
	// logger.error("BOA后台通知url验证异常,返回responseTxt=" + responseTxt +
	// ",notifyUrl:" + alipayNotifyURL);
	// throw new ValidationException("BOA后台通知url验证异常,返回responseTxt=" +
	// responseTxt);
	// }
	//
	// // 订单验证
	// String batchNo = StringUtils.trim(request.getParameter("batch_no"));
	// TransferOrder queryOrder = null;// transferOrderService.queryOrder(batchNo);
	// Assert.notNull(queryOrder, "转账订单查询为空,orderNo:" + batchNo);
	//
	// // 获取数据
	// String sign = StringUtils.trim(request.getParameter("sign"));
	// Map alipay = request.getParameterMap();
	// Properties params = new Properties();
	// for (Iterator<Entry<String, Object>> keyValuePairs =
	// alipay.entrySet().iterator(); keyValuePairs.hasNext();) {
	// Map.Entry<String, Object> entry = (Map.Entry<String, Object>)
	// keyValuePairs.next();
	// String key = entry.getKey();
	// String value = request.getParameter(key);
	// if (!"sign".equalsIgnoreCase(key) && !"sign_type".equalsIgnoreCase(key)) {
	// try {
	// params.put(key, URLDecoder.decode(value, _charset_encode));
	// } catch (UnsupportedEncodingException e) {
	// logger.error(e.getMessage(), e);
	// }
	// }
	// }
	//
	// // 验证签名
	// String pubKey = AlipayHelper.readText(platform.getPublicUrl());
	// boolean result = AlipayHelper.dsaCheck(params, pubKey, _charset_encode,
	// sign);
	// if (!result) {
	// // 加密校验失败
	// if (logger.isInfoEnabled()) {
	// logger.info("==============BOA后台回调参数加密处理失败=================");
	// logger.info("我方加密参数：" + JsonUtils.toJson(params));
	// logger.info("==============BOA后台回调参数加密处理结束=================\n");
	// }
	// throw new ValidationException("支付平台回调参数加密校验失败");
	// }
	//
	// Map<String, Object> returned = new HashMap<String, Object>();
	// // 判断状态 组装数据
	// // success_details 流水号^收款方账号^收款账号姓名^付款金额^成功标识(S)^成功原因(null)^BOA内部流水号^完成时间
	// // fail_details 流水号^收款方账号^收款账号姓名^付款金额^失败标识(F)^失败原因^BOA内部流水号^完成时间。
	// String successDetails =
	// StringUtils.trim(request.getParameter("success_details"));
	// if (StringUtils.isBlank(successDetails)) {
	// String failDetails = StringUtils.trim(request.getParameter("fail_details"));
	// String[] failDtlArr = failDetails.split("^");
	// returned.put(PaymentConstant.OPPOSITE_MONEY, failDtlArr[3]);
	// returned.put(PaymentConstant.TRANSFER_STATE,
	// TransferOrder.TRANSFER_STATE_FAILED);
	// returned.put(PaymentConstant.TRANSFER_STATE_MESSAGE, failDtlArr[5]);
	// returned.put(PaymentConstant.OPPOSITE_ORDERNO, failDtlArr[6]);
	// returned.put(PaymentConstant.TRANSFER_ACCOUNT, failDtlArr[1]);
	// } else {
	// String[] successDtlArr = successDetails.split("^");
	// returned.put(PaymentConstant.OPPOSITE_MONEY, successDtlArr[3]);
	// returned.put(PaymentConstant.TRANSFER_STATE,
	// "S".equalsIgnoreCase(successDtlArr[4]) ?
	// TransferOrder.TRANSFER_STATE_COMPLETED :
	// TransferOrder.TRANSFER_STATE_NOT_COMPLETED);
	// returned.put(PaymentConstant.TRANSFER_STATE_MESSAGE, successDtlArr[5]);
	// returned.put(PaymentConstant.OPPOSITE_ORDERNO, successDtlArr[6]);
	// returned.put(PaymentConstant.TRANSFER_ACCOUNT, successDtlArr[1]);
	// }
	//
	// return returned;
	// }

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
