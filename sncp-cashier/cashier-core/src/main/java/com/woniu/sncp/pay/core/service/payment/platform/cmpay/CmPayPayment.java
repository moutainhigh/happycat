package com.woniu.sncp.pay.core.service.payment.platform.cmpay;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.lang.DateUtil;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.cmpay.tools.HiiposmUtil;
import com.woniu.sncp.pojo.payment.PaymentOrder;

/**
 * <pre>
 * 中国移动-手机支付
 * 流程：
 * 1.向对方请求，对方处理，返回payurl
 * 2.重定向到对面平台，对方引导用户完成支付，并讲支付结果返回给我方(备注：这跟别的平台不大相同，别的平台没有重定向这一步)
 * 3.我方根据对方返回的结果，做相应的业务处理
 * @author sungs
 * @version 1.0
 * @update 2013-02-25
 * </pre>
 */
@Service("cmPayPayment")
public class CmPayPayment extends  AbstractPayment{
	
	private final String EMPTYSTRING = "";

	@Override
	public String encode(Map<String, Object> inParams){
		String encrypted = "";
		try {
			encrypted = MD5Encrypt.encrypt((String) inParams.get("source"), "utf-8");
		} catch (RuntimeException e) {
			logger.error("中国移动-手机支付加密异常", e);
			throw new ValidationException("中国移动-手机支付加密异常", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("=========中国移动-手机支付加密开始=========");
			logger.info("source：" + inParams.get("source"));
			logger.info("encrypted：" + encrypted);
			logger.info("=========中国移动-手机支付加密结束=========\n");
		}
		return encrypted;
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		//1.请求参数
		Map<String, Object> linkedParams = new LinkedHashMap<String, Object>();
		linkedParams.put("characterSet", "02");//字符集默认00-GBK，01-GB2312,02-UTF-8
		linkedParams.put("callbackUrl", platform.getFrontUrl(paymentOrder.getMerchantId()));
		linkedParams.put("notifyUrl", platform.getBehindUrl(paymentOrder.getMerchantId()));
		linkedParams.put("ipAddress", paymentOrder.getClientIp());
		linkedParams.put("merchantId", platform.getMerchantNo());
		linkedParams.put("requestId", paymentOrder.getOrderNo());
		linkedParams.put("signType", "MD5");//签名方式
		linkedParams.put("type", "DirectPayConfirm");//接口类型
		linkedParams.put("version", "2.0.0");
		BigDecimal m = new BigDecimal(paymentOrder.getMoney().toString());
		linkedParams.put("amount", m.multiply(new BigDecimal(100)).intValue());
//		linkedParams.put("amount", (int) (paymentOrder.getMoney() * 100));
		linkedParams.put("currency", "00");
		linkedParams.put("orderDate", DateUtil.parseDate2Str(paymentOrder.getCreateDate(), "yyyyMMdd"));
		linkedParams.put("orderId", paymentOrder.getOrderNo());
		linkedParams.put("merAcDate", DateUtil.parseDate2Str(paymentOrder.getCreateDate(), "yyyyMMdd"));
		linkedParams.put("period", "9999");
		linkedParams.put("periodUnit", "02");
		linkedParams.put("productName", String.valueOf(inParams.get("productName")));
		
		String source = getMd5Source(linkedParams);
		//2.参数加密
		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", source);
		HiiposmUtil util = new HiiposmUtil();
		String hmac = util.MD5Sign(source, platform.getPayKey());
		//String encrypted = this.encode(encodeParams);
		linkedParams.put("hmac", hmac);
		
		//3.由于，中国移动-手机支付，需要先请求，才能取得支付地址用于重定向，所以这里先将参数请求
		String buf = "characterSet=" + "02" + "&callbackUrl="
				+ platform.getFrontUrl(paymentOrder.getMerchantId()) + "&notifyUrl=" + platform.getBehindUrl(paymentOrder.getMerchantId())
				+ "&ipAddress=" + paymentOrder.getClientIp() + "&merchantId="
				+ platform.getMerchantNo() + "&requestId=" + paymentOrder.getOrderNo() + "&signType="
				+ "MD5" + "&type=" + "DirectPayConfirm" + "&version=" + "2.0.0"
				+ "&amount=" + String.valueOf(m.multiply(new BigDecimal(100)).intValue()) + "&bankAbbr=" + ""
				+ "&currency=" + "00" + "&orderDate=" + DateUtil.parseDate2Str(paymentOrder.getCreateDate(), "yyyyMMdd")
				+ "&orderId=" + paymentOrder.getOrderNo() + "&merAcDate=" + DateUtil.parseDate2Str(paymentOrder.getCreateDate(), "yyyyMMdd")
				+ "&period=" + "9999" + "&periodUnit=" + "02"
				+ "&merchantAbbr=" + "" + "&productDesc="
				+ "" + "&productId=" + ""
				+ "&productName=" + String.valueOf(inParams.get("productName")) + "&productNum="
				+ "" + "" + "&reserved1=" + ""
				+ "&reserved2=" + "" + "&userToken=" + ""
				+ "&showUrl=" + "" + "&couponsFlag=" + "";
		//带上消息摘要
		buf = "hmac=" + hmac + "&" + buf;
		
		//3.1发起http请求，并获取响应报文
		String responseBody = null;
		try {
			responseBody = util.sendAndRecv(platform.getPayUrl(), buf, "00");
		} catch (Exception e) {
			throw new ValidationException("中国移动-手机支付，请求订单返回失败", e);
		}
		
		
		if (StringUtils.isBlank(responseBody)) {
			throw new ValidationException("中国移动-手机支付,请求订单返回responseBody为空");
		}
		
		//4.请求返回内容
		if (logger.isInfoEnabled()) {
			logger.info("中国移动-手机支付订单校验返回结果：" + responseBody);
		}
		
		//5.验签
		String message  = null;
		try {
			message = URLDecoder.decode(util.getValue(responseBody,"message"),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new ValidationException("中文转码失败：" + e);
		}
		
		String vfsign = util.getValue(responseBody, "merchantId")
				+ util.getValue(responseBody, "requestId")
				+ util.getValue(responseBody, "signType")
				+ util.getValue(responseBody, "type")
				+ util.getValue(responseBody, "version")
				+ util.getValue(responseBody, "returnCode")
				+ message 
				+ util.getValue(responseBody, "payUrl");
		boolean flag = false;
		flag = util.MD5Verify(vfsign, util.getValue(responseBody, "hmac"), platform.getPayKey());
		if(!flag){
			throw new ValidationException("中国移动-手机支付，验签失败，"+message);
		}
		
		//6.判断是否请求成功，若成功将payUrl放入linkedParams
		linkedParams.put("returnCode",  util.getValue(responseBody, "returnCode"));
		if("000000".equals(util.getValue(responseBody, "returnCode"))){
			String payUrl = util.getValue(responseBody, "payUrl");
			linkedParams.put("payUrl", util.getRedirectUrl(payUrl));
		}else{
			linkedParams.put("message", message);
		}
		
		return linkedParams;
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,Platform platform) throws ValidationException,
			DataAccessException {
		
		// 得到中国移动-手机支付返回的数据
		String merId = StringUtils.trim(request.getParameter("merchantId"));
		String payNo = StringUtils.trim(request.getParameter("payNo"));
		String returnCode = StringUtils.trim(request.getParameter("returnCode"));
		String message = StringUtils.trim(request.getParameter("message"));
		String signType = StringUtils.trim(request.getParameter("signType"));
		String type = StringUtils.trim(request.getParameter("type"));
		String version = StringUtils.trim(request.getParameter("version"));
		String amount = StringUtils.trim(request.getParameter("amount"));
		String amtItem = StringUtils.trim(request.getParameter("amtItem"));
		String bankAbbr = StringUtils.trim(request.getParameter("bankAbbr"));
		String mobile = StringUtils.trim(request.getParameter("mobile"));
		String orderId = StringUtils.trim(request.getParameter("orderId"));
		String payDate = StringUtils.trim(request.getParameter("payDate"));
		String accountDate = StringUtils.trim(request.getParameter("accountDate"));
		String status = StringUtils.trim(request.getParameter("status"));
		String orderDate = StringUtils.trim(request.getParameter("orderDate"));
		String fee = StringUtils.trim(request.getParameter("fee"));
		String hmac = StringUtils.trim(request.getParameter("hmac"));

		// 1.我方加密数据
		LinkedHashMap<String, Object> encryParams = new LinkedHashMap<String, Object>();
		encryParams.put("merId", merId);
		encryParams.put("payNo", payNo);
		encryParams.put("returnCode", returnCode);
		encryParams.put("message", message);
		encryParams.put("signType", signType);
		encryParams.put("type", type);
		encryParams.put("version", version);
		encryParams.put("amount", amount);
		encryParams.put("amtItem", amtItem);
		encryParams.put("bankAbbr", bankAbbr);
		encryParams.put("mobile", mobile);
		encryParams.put("orderId", orderId);
		encryParams.put("payDate", payDate);
		encryParams.put("accountDate", accountDate);
		encryParams.put("status", status);
		encryParams.put("orderDate", orderDate);
		encryParams.put("fee", fee);
		String source = getMd5Source(encryParams);
		HiiposmUtil util = new HiiposmUtil();
		//String encrypted = util.MD5Sign(source, paymentPlatform.getAuthKey());
		//Map<String, Object> encodeParams = new HashMap<String, Object>();
		//encodeParams.put("source", source);
		//String encrypted = this.encode(encodeParams);

		// 2.比较是否相等
		
		boolean flag = false;
		flag = util.MD5Verify(source,hmac,platform.getPayKey());
		if(!flag){
			throw new ValidationException("中国移动-手机支付，接受返回，验签失败。");
		}
//		if (!encrypted.equals(hmac)) {
//			if (logger.isInfoEnabled()) {
//				logger.info("==============中国移动-手机支付后台加密处理失败=================");
//				logger.info("我方加密串：" + encrypted);
//				logger.info("对方加密串：" + hmac);
//				logger.info("==============中国移动-手机支付后台加密处理结束=================\n");
//			}
//			throw new ValidationException("支付平台加密校验失败");
//		}

		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderId);
		Assert.notNull(paymentOrder, "支付订单查询为空,orderId:" + orderId);

		Map<String, Object> returned = new HashMap<String, Object>();
		if ("SUCCESS".equals(status)) { // 支付成功
			logger.info("中国移动-手机支付返回支付成功");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else { // 支付失败
			logger.info("中国移动-手机支付返回支付失败");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_FAILED);
		}
		
		// 支付模式-从privateField中判断是神州行、联通、电信？
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());

		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, payNo);
		// 金额单位：分，校验无需乘以100
		returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf(NumberUtils.toFloat(amount)));
		return returned;
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams, HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess)
			super.responseAndWrite(response, "SUCCESS");
		else
			super.responseAndWrite(response, "cmpay_imprest_fail,maybe payment is invalid");
	}

	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		String version = "2.0.0";
		String merId = platform.getMerchantNo();
		String orderId = paymentOrder.getOrderNo();
		String signType = "MD5";
		String type = "OrderQuery";

		// 1.加密
		LinkedHashMap<String, Object> encryParams = new LinkedHashMap<String, Object>();
		encryParams.put("merchantId", merId);
		encryParams.put("requestId", orderId);
		encryParams.put("signType", signType);
		encryParams.put("type", type);
		encryParams.put("version", version);
		encryParams.put("orderId", orderId);
		String source = getMd5Source(encryParams);
		
		HiiposmUtil util = new HiiposmUtil();
		String hmac = util.MD5Sign(source, platform.getPayKey());
		
		//2.拼装请求报文
		String buf = "merchantId=" + merId + "&requestId="
				+ orderId + "&signType=" + signType + "&type=" + type
				+ "&version=" + version + "&orderId=" + orderId;
		buf = "hmac=" + hmac + "&" + buf;
		
		//3.向中国移动-手机支付请求,并获取响应报文
		String responseBody = null;
		try {
			responseBody = util.sendAndRecv(platform.getPayCheckUrl(), buf, "02");
		} catch (Exception e) {
			throw new ValidationException("中国移动-手机支付订单校验返回失败", e);
		}
		
		if (StringUtils.isBlank(responseBody)) {
			throw new ValidationException("中国移动-手机支付订单验证返回responseBody为空");
		}
		
		if (logger.isInfoEnabled()) {
			logger.info("中国移动-手机支付订单校验返回结果：" + responseBody);
		}
		
		//4.验签
		String message  = null;
		try {
			message = URLDecoder.decode(util.getValue(responseBody,"message"),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new ValidationException("中文转码失败：" + e);
		}
		String vfsign = util.getValue(responseBody, "merchantId")
				+ util.getValue(responseBody, "payNo")
				+ util.getValue(responseBody, "returnCode")
				+ message
				+ util.getValue(responseBody, "signType")
				+ util.getValue(responseBody, "type")
				+ util.getValue(responseBody, "version")
				+ util.getValue(responseBody, "amount")
				+ util.getValue(responseBody, "amtItem")
				+ util.getValue(responseBody, "bankAbbr")
				+ util.getValue(responseBody, "mobile")
				+ util.getValue(responseBody, "orderId")
				+ util.getValue(responseBody, "payDate")
				+ util.getValue(responseBody, "status")
				+ util.getValue(responseBody, "payType")
				+ util.getValue(responseBody, "orderDate")
				+ util.getValue(responseBody, "fee");
		boolean flag = false;
		flag = util.MD5Verify(vfsign, util.getValue(responseBody, "hmac"), platform.getPayKey());

		if (!flag){
			throw new ValidationException("中国移动-手机支付，验签失败。");
		}
		
		//5.业务处理
		Map<String, Object> outParams = new HashMap<String, Object>();
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		String code = util.getValue(responseBody, "returnCode");
		if("000000".equals(code)){
			String status = util.getValue(responseBody, "status");
			if("SUCCESS".equals(status)){
				payState = PaymentConstant.PAYMENT_STATE_PAYED;// 支付成功
			}else if("FAILED".equals(status)){
				payState = PaymentConstant.PAYMENT_STATE_FAILED;// 支付失败
			}else{
				payState = PaymentConstant.PAYMENT_STATE_NOPAYED;// 未支付
			}
			outParams.put(PaymentConstant.OPPOSITE_ORDERNO, util.getValue(responseBody, "orderId"));// 订单号
			outParams.put(PaymentConstant.OPPOSITE_MONEY, util.getValue(responseBody, "amount"));// 订单金额
		}else{
			throw new ValidationException("中国移动-手机支付订单验证返回:["+code+"-"+URLDecoder.decode(util.getValue(responseBody, "message"))+"。");
		}
		
		outParams.put(PaymentConstant.PAYMENT_STATE, payState);
		return outParams;
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("orderId");
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

}
