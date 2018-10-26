package com.woniu.sncp.pay.core.service.payment.platform.boa;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.HttpClient;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;

import okhttp3.Response;

@Service("boaHostPayment")
public class BoaHostPayment extends AbstractPayment {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final String _charset_encode = "utf-8";

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams) {
		// 1.拼装参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);


		// String payUrl = url +
		// String.format("?productId=%s&transactionId=%s&price=%s", productId, orderNo,
		// price);
		Map<String, Object> params = new HashMap<String, Object>();

//		Map<String, Object> data = new HashMap<String, Object>();
		String currency_code = null;
		String language=null;
		String client_email = null;
		String country=null;
		String currencyStr = paymentOrder.getMoneyCurrency();
		logger.info("paymentOrder:{}, MoneyCurrency:{}",paymentOrder.getOrderNo(), currencyStr);

		boolean test_mode=false;
	 
		try {
			JSONObject json = JSON.parseObject(platform.getExtend());
			test_mode="1".equals(json.getString("test_mode"));
			JSONObject object = json.getJSONObject("currency_code").getJSONObject(currencyStr);
			currency_code=	object.getString("currency");
			language=object.getString("language");
			country=object.getString("country");
 			
		} catch (Exception e) {

		}
	 
		Assert.hasLength(currency_code, "找不到currency_code."+currencyStr+"配置");
		try {
			JSONObject json = JSON.parseObject(paymentOrder.getInfo());
			client_email = json.getString("client_email");
		} catch (Exception e) {

		}

		 
		params.put("test_mode", test_mode?"1":"0");	
		params.put("store_id", platform.getMerchantNo());
		params.put("return", platform.getFrontUrl(paymentOrder.getMerchantId())+"?orderNo="+paymentOrder.getOrderNo());
		params.put("notify_url", platform.getBehindUrl(paymentOrder.getMerchantId())+"?orderNo="+paymentOrder.getOrderNo());

		params.put("currency_code", currency_code);
		params.put("order_id", paymentOrder.getOrderNo());
		params.put("order_description", paymentOrder.getProductname()+"("+paymentOrder.getOrderNo()+")");
		//这里一定要保留两们小数，不然会有问题
		params.put("amount", new BigDecimal( paymentOrder.getMoney().toString()).setScale(2).toString());

		if (StringUtils.isNotBlank(client_email)) {
			params.put("client_email",client_email);
		}
		if(StringUtils.isNotBlank(language)) {
			params.put("language", language);
		}
		if(StringUtils.isNotBlank(country)) {
			params.put("country_payment", country);

		}
 
 		

		StringBuilder builder = new StringBuilder();
		builder.append(params.get("store_id"));
		builder.append(params.get("notify_url")); 
		builder.append(params.get("order_id"));
		builder.append(params.get("amount"));
		builder.append(params.get("currency_code"));
		
		try {
		    Mac mac = Mac.getInstance("HmacSHA256");
		    mac.init(new SecretKeySpec(platform.getPayKey().getBytes("UTF8"), "HmacSHA256"));

		      
		    
			String hash_key =Hex.encodeHexString(mac.doFinal(builder.toString().getBytes("UTF-8")));

			params.put("hash_key", hash_key);
		}catch(Exception e) {
			
		}
		
		
//		builder.append(platform.getPayKey());
//		
//		String hash_key = DigestUtils.sha256Hex(builder.toString());
//
//		params.put("hash_key", hash_key);

		// <input type="hidden" name="store_id" id="store_id" value="10">
		// <input type="hidden" name="return"
		// value="http://www.virtualstore.com/return.php">
		// <input type="hidden" name="notify_url"
		// value="http://www.virtualstore.com/notify.php">
		// <input type="hidden" name="currency_code" id="currency_code" value="BRL">
		// <input type="hidden" name="order_id" id="order_id" value="16598">
		// <input type="hidden" name="order_description" value="Premium Account 3 months
		// ">
		// <input type="hidden" name="amount" id="amount" value="1740">
		// <input type="hidden" name="client_email" id="client_email"
		// value="test@boacompra.com">
		// <input type="hidden" name="hash_key" id="hash_key"
		// value="ac87ffee901a1af2b24a6d05f617f152">
		String payUrl=platform.getPayUrl();

		params.put("payUrl", payUrl);
//		params.put("method", "post");

 
		return params;
	}
 	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
 		return request.getParameter("orderNo");
//		return request.getParameter("transactionId");
		
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

 		
		String orderNo=request.getParameter("orderNo");		

		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "支付订单查询为空,orderNo:" + orderNo);
 
		
		boolean test_mode=false;
		 
		try {
			JSONObject json = JSON.parseObject(platform.getExtend());
			test_mode="1".equals(json.getString("test_mode"));
		 
 			
		} catch (Exception e) {

		}
		String oppositeOrderNo = request.getParameter("transaction_id");

		String test_mode_str = request.getParameter("test_mode");
		String url=null;
		if(StringUtils.isNotBlank(test_mode_str)&&(StringUtils.equals(test_mode_str, "1")||StringUtils.equals(test_mode_str, "true"))&&test_mode) {
			url="https://api.sandbox.boacompra.com/transactions/"+oppositeOrderNo;
			logger.info("定单{}是测试定单",orderNo);
		}else {
			url="https://api.boacompra.com/transactions/"+oppositeOrderNo;
		}

		JSONObject transaction=null;
		try {
			 
			Header header=new Header(url,"");
			header.setSecretKey(platform.getPayKey());
			header.setStoreId(platform.getMerchantNo());
 
			Response resp=HttpClient.callGet(url,header.generateHeader(), 20000);
			String body=resp.body().string();
			logger.info("定单号:{} 查询transactionCode:{},状态码:{}, 响应:{}",orderNo,oppositeOrderNo,resp.code(),body);
			if(resp.code()==200) {
				transaction=	JSON.parseObject(body).getJSONObject("transaction-result").getJSONArray( "transactions").getJSONObject(0);
			

			}else {
				 throw new RuntimeException("状态码:"+resp.code());
			}
		}catch(Exception e) {
			logger.error("", e);
	 
		 
		}
		
 
		String payResult = StringUtils.trim(transaction.getString("status"));

		Map<String, Object> returned = new HashMap<String, Object>();
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;

		if (StringUtils.equals(orderNo, transaction.getString("order-id"))&&StringUtils.equals(payResult, "COMPLETE")) { // 支付成功
			logger.info("BOA返回支付成功");
			
			String paymentMoney =transaction.getString("amount");
			BigDecimal orderModey = new BigDecimal(paymentOrder.getMoney().toString()).setScale(2).multiply(new BigDecimal("100"));
			BigDecimal totalPrice = new BigDecimal(paymentMoney).setScale(2).multiply(new BigDecimal("100"));


			returned.put(PaymentConstant.OPPOSITE_MONEY,orderModey.toString());
			if(totalPrice.compareTo(orderModey)==0) {
				logger.info("支付成功");
				payState = PaymentConstant.PAYMENT_STATE_PAYED;	
			}
			

		} else { // 未支付
			logger.info("BOA返回未支付");
		}

		returned.put(PaymentConstant.PAYMENT_STATE, payState);

		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);

		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		return returned;
	}

 	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
  		// 1.拼装地址
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
 		String currencyStr = paymentOrder.getMoneyCurrency();
		logger.info("paymentOrder:{}, MoneyCurrency:{}",paymentOrder.getOrderNo(), currencyStr);
		String oppositeOrderNo=paymentOrder.getOtherOrderNo();
	 
		String productId = platform.getMerchantNo();
		String orderNo = paymentOrder.getOrderNo();
 
 

 		
		
		
		boolean test_mode=false;
		 
		try {
			JSONObject json = JSON.parseObject(platform.getExtend());
			test_mode="1".equals(json.getString("test_mode"));
		 
 			
		} catch (Exception e) {

		}
		
		String url=null;
		if(test_mode) {
			url="https://api.sandbox.boacompra.com/transactions/"+oppositeOrderNo;
			logger.info("定单{}是测试定单",orderNo);
		}else {
			url="https://api.boacompra.com/transactions/"+oppositeOrderNo;
		}

		JSONObject transaction=null;
		try {
			 
			Header header=new Header(url,"");
			header.setSecretKey(platform.getPayKey());
			header.setStoreId(platform.getMerchantNo());
 
			Response resp=HttpClient.callGet(url,header.generateHeader(), 20000);
			String body=resp.body().string();
			logger.info("定单号:{} 查询transactionCode:{},状态码:{}, 响应:{}",orderNo,oppositeOrderNo,resp.code(),body);
			if(resp.code()==200) {
				transaction=	JSON.parseObject(body).getJSONObject("transaction-result").getJSONArray( "transactions").getJSONObject(0);
			

			}else {
				 throw new RuntimeException("状态码:"+resp.code());
			}
		}catch(Exception e) {
			logger.error("", e);
	 
		 
		}
		
 
		String payResult = StringUtils.trim(transaction.getString("status"));

		Map<String, Object> outParams = new HashMap<String, Object>();
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		
		
		

		if (StringUtils.equals(orderNo, transaction.getString("order-id"))&&StringUtils.equals(payResult, "COMPLETE")) { // 支付成功
			logger.info("BOA返回支付成功");
			
			String paymentMoney =transaction.getString("amount");
			BigDecimal orderModey = new BigDecimal(paymentOrder.getMoney().toString()).setScale(2).multiply(new BigDecimal("100"));
			BigDecimal totalPrice = new BigDecimal(paymentMoney).setScale(2).multiply(new BigDecimal("100"));


			outParams.put(PaymentConstant.OPPOSITE_MONEY,orderModey.toString());
			if(totalPrice.compareTo(orderModey)==0) {
				logger.info("支付成功");
				payState = PaymentConstant.PAYMENT_STATE_PAYED;	
			}
			

		} else { // 未支付
			logger.info("BOA返回未支付");
		}

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
		}
		else {
			response.setStatus(500);
			super.responseAndWrite(response, "fail");
			 
		}
			
	}

}
