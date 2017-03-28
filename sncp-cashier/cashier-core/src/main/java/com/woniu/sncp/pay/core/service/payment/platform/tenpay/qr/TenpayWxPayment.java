package com.woniu.sncp.pay.core.service.payment.platform.tenpay.qr;

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

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptStringUtils;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.tools.AlipayHelper;
import com.woniu.sncp.pojo.payment.PaymentOrder;

/**
 * 微信扫码支付方式
 * 
 * 银行类型bank_type为WX
 * 
 * @author luzz
 *
 */
@Service("tenpayWxPayment")
public class TenpayWxPayment extends AbstractPayment {
	
	/**
	 * 财付通微信支付编码
	 */
	private final String _charset_encode = "UTF-8";
	
	@Override
	public String encode(Map<String, Object> inParams) throws ValidationException {
		String source = (String) inParams.get("source");
		String encrypted = StringUtils.upperCase(MD5Encrypt.encrypt(source));
		if (logger.isInfoEnabled()) {
			logger.info("=========微信扫码支付加密开始=========");
			logger.info("source：" + source);
			logger.info("encrypted：" + encrypted);
			logger.info("=========微信扫码支付加密结束=========\n");
		}
		return encrypted;
	}
	
	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams) throws ValidationException {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		// 1.拼装参数
		LinkedHashMap<String, Object> linkedParams = new LinkedHashMap<String, Object>();
		linkedParams.put("service_version", "1.0");
		linkedParams.put("input_charset", _charset_encode); // 提交给对方的支付编码
		linkedParams.put("bank_type", "WX");
		linkedParams.put("body", inParams.get("productName")); // 交易的商品名称
		linkedParams.put("return_url", platform.getFrontUrl(paymentOrder.getMerchantId())); // 前台跳转地址
		linkedParams.put("notify_url", platform.getBehindUrl(paymentOrder.getMerchantId())); // 后台返回地址
		linkedParams.put("partner", platform.getMerchantNo()); // 商户ID
		linkedParams.put("out_trade_no", paymentOrder.getOrderNo()); // 我方格式订单号
//		linkedParams.put("total_fee", (int) (paymentOrder.getMoney() * 100)); // 总金额，单位分
		linkedParams.put("total_fee", (new BigDecimal(paymentOrder.getMoney().toString())).multiply(new BigDecimal(100)).intValue()); // 总金额，单位分
		linkedParams.put("fee_type", 1); // 人民币
		linkedParams.put("spbill_create_ip", inParams.get(PaymentConstant.CLIENT_IP)); // 用户IP（非商户服务器IP），为了防止欺诈，支付时财付通会校验此IP

		// 2.参数加密
		LinkedHashMap<String, Object> sortSignMap = AlipayHelper.sortMap(linkedParams);
		sortSignMap.put("key", platform.getPayKey()); // 加密key
		String source = EncryptStringUtils.linkedHashMapToStringWithKey(sortSignMap, true);
		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", source);
		String encrypted = this.encode(encodeParams);
		linkedParams.put("sign", encrypted); // md5

		// 3.移除加密key
		linkedParams.remove("key");
		
		// 4.添加支付地址
		linkedParams.put("payUrl", platform.getPayUrl()); // 提交给对方的支付地址
		

		return linkedParams;
	}
	
	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request, Platform platform)
			throws ValidationException, DataAccessException, PaymentRedirectException {

		//校验加密串
		String sign = StringUtils.trim(request.getParameter("sign"));
		String localSign = generateBackParamSign(request, platform);
		if(!localSign.equalsIgnoreCase(sign)){
			if (logger.isInfoEnabled()) {
				logger.info("==============微信扫码后台加密处理失败=================");
				logger.info("我方加密串：" + localSign);
				logger.info("对方加密串：" + sign);
				logger.info("==============微信扫码后台加密处理结束=================\n");
			}
			throw new ValidationException("微信扫码支付平台加密校验失败");
		}
		
		String outTradeNo = StringUtils.trim(request.getParameter("out_trade_no"));
		String tradeState = StringUtils.trim(request.getParameter("trade_state"));
		String transactionId = StringUtils.trim(request.getParameter("transaction_id"));
		String totalFee = StringUtils.trim(request.getParameter("total_fee"));//单位分
		
		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(outTradeNo);
		Assert.notNull(paymentOrder, "支付订单查询为空,orderNo:" + outTradeNo);

		Map<String, Object> returned = new HashMap<String, Object>();
		if ("0".equals(tradeState)) { // 支付成功
			logger.info("微信扫码返回支付成功");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else { // 未支付 - 只有2个状态 0 和 非0
			logger.info("微信扫码返回未支付,pay_info:" + request.getParameter("pay_info"));
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}

		// 设置充值类型 - 不传则默认1-网银支付
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, transactionId);
		returned.put(PaymentConstant.OPPOSITE_MONEY, totalFee);
		return returned;
	}
	
	private String generateBackParamSign(HttpServletRequest request,Platform platform){
		LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
		Map<String, Object> requestParams = request.getParameterMap();
		for (Iterator<Entry<String, Object>> keyValuePairs = requestParams.entrySet().iterator(); keyValuePairs
				.hasNext();) {
			Map.Entry<String, Object> entry = keyValuePairs.next();
			String value = request.getParameter(entry.getKey());
			if(!"sign".equalsIgnoreCase(entry.getKey()) && !StringUtils.isBlank(value)){
				params.put(entry.getKey(),value);
			}
		}
		
		LinkedHashMap<String, Object> sortSignMap = AlipayHelper.sortMap(params);
		sortSignMap.put("key", platform.getPayKey());
		// 空值不加入
		String source = EncryptStringUtils.linkedHashMapToStringWithKey(sortSignMap, true);
		// 2.参数加密
		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", source);
		String encrypted = this.encode(encodeParams);
		
		return encrypted;
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		if(isImprestedSuccess){
			super.responseAndWrite(response, "success");
		} else {
			super.responseAndWrite(response, "fail");
		}
	}

	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		//获取数据
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		LinkedHashMap<String, Object> linkedParams = new LinkedHashMap<String, Object>();
		linkedParams.put("partner", platform.getMerchantNo());
		linkedParams.put("out_trade_no", paymentOrder.getOrderNo());
		linkedParams.put("input_charset", _charset_encode);
		
		// 升序排序
		LinkedHashMap<String, Object> sortMap = AlipayHelper.sortMap(linkedParams);
		sortMap.put("key", platform.getPayKey());

		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", EncryptStringUtils.linkedHashMapToStringWithKey(sortMap, true));
		String encrypted = this.encode(encodeParams);
		
		//订单验证请求
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(platform.getPayCheckUrl());

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("partner", platform.getMerchantNo()));
		params.add(new BasicNameValuePair("out_trade_no", paymentOrder.getOrderNo()));
		params.add(new BasicNameValuePair("input_charset", _charset_encode));
		params.add(new BasicNameValuePair("sign", encrypted));

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
			throw new ValidationException("微信扫码订单校验返回失败", e);
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		
		logger.info("微信扫码订单校验返回：" + responseBody);
		
		// 解析，校验及分析返回数据
		String retcode = readXmlNode(responseBody,"//root/retcode");
		String trade_state = readXmlNode(responseBody,"//root/trade_state");
		String retmsg = readXmlNode(responseBody,"//root/retmsg");
		String trade_mode = readXmlNode(responseBody,"//root/trade_mode");
		String partner = readXmlNode(responseBody,"//root/partner");
		
		String bank_type = readXmlNode(responseBody,"//root/bank_type");
		String bank_billno = readXmlNode(responseBody,"//root/bank_billno");
		String total_fee = readXmlNode(responseBody,"//root/total_fee");
		String fee_type = readXmlNode(responseBody,"//root/fee_type");
		String transaction_id = readXmlNode(responseBody,"//root/transaction_id");
		
		String out_trade_no = readXmlNode(responseBody,"//root/out_trade_no");
		String is_split = readXmlNode(responseBody,"//root/is_split");
		String is_refund = readXmlNode(responseBody,"//root/is_refund");
		String attach = readXmlNode(responseBody,"//root/attach");
		String time_end = readXmlNode(responseBody,"//root/time_end");
		
		String transport_fee = readXmlNode(responseBody,"//root/transport_fee");
		String product_fee = readXmlNode(responseBody,"//root/product_fee");
		String discount = readXmlNode(responseBody,"//root/discount");
		String buyer_alias = readXmlNode(responseBody,"//root/buyer_alias");
		String cash_ticket_fee = readXmlNode(responseBody,"//root/cash_ticket_fee");
		
		String sign_type = readXmlNode(responseBody,"//root/sign_type");
		String service_version = readXmlNode(responseBody,"//root/service_version");
		String input_charset = readXmlNode(responseBody,"//root/input_charset");
		String sign = readXmlNode(responseBody,"//root/sign");
		String sign_key_index = readXmlNode(responseBody,"//root/sign_key_index");
		
		LinkedHashMap<String, Object> returnLinkedParams = new LinkedHashMap<String, Object>();
		returnLinkedParams.put("retcode", retcode);
		returnLinkedParams.put("trade_state", trade_state);
		returnLinkedParams.put("retmsg", retmsg);
		returnLinkedParams.put("trade_mode", trade_mode);
		returnLinkedParams.put("partner", partner);
		
		returnLinkedParams.put("bank_type", bank_type);
		returnLinkedParams.put("bank_billno", bank_billno);
		returnLinkedParams.put("total_fee", total_fee);
		returnLinkedParams.put("fee_type", fee_type);
		returnLinkedParams.put("transaction_id", transaction_id);
		
		returnLinkedParams.put("out_trade_no", out_trade_no);
		returnLinkedParams.put("is_split", is_split);
		returnLinkedParams.put("is_refund", is_refund);
		returnLinkedParams.put("attach", attach);
		returnLinkedParams.put("time_end", time_end);
		
		returnLinkedParams.put("transport_fee", transport_fee);
		returnLinkedParams.put("product_fee", product_fee);
		returnLinkedParams.put("discount", discount);
		returnLinkedParams.put("buyer_alias", buyer_alias);
		returnLinkedParams.put("cash_ticket_fee", cash_ticket_fee);
		
		returnLinkedParams.put("sign_type", sign_type);
		returnLinkedParams.put("service_version", service_version);
		returnLinkedParams.put("input_charset", input_charset);
		returnLinkedParams.put("sign_key_index", sign_key_index);
		
		LinkedHashMap<String, Object> retSortSignMap = AlipayHelper.sortMap(returnLinkedParams);
		retSortSignMap.put("key", platform.getPayKey());

		Map<String, Object> retSortSignStrMap = new HashMap<String, Object>();
		retSortSignStrMap.put("source", EncryptStringUtils.linkedHashMapToStringWithKey(retSortSignMap, true));
		String localRetSign = this.encode(retSortSignStrMap);
		
		Map<String, Object> outParams = new HashMap<String, Object>();
		// 对方校验串校验 md5(参数排序 + key)
		if (!localRetSign.equalsIgnoreCase(sign)) {
			if (logger.isInfoEnabled()) {
				logger.info("==============微信扫码订单校验返回加密处理失败=================");
				logger.info("我方加密串：" + localRetSign);
				logger.info("对方加密串：" + sign);
				logger.info("==============微信扫码订单校验返回加密处理结束=================\n");
			}
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return outParams;
		}
		
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		if (!"0".equals(trade_state)) { // 支付不成功
			payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
			logger.info("微信扫码返回未成功支付，trade_state:" + trade_state + ",retmsg:" + retmsg);
			
			// 设置充值类型 - 不传则默认1-网银支付
			outParams.put(PaymentConstant.OPPOSITE_ORDERNO, transaction_id); // 对方订单号
			outParams.put(PaymentConstant.PAYMENT_STATE, payState);
			outParams.put(PaymentConstant.OPPOSITE_MONEY, total_fee); // 总金额，对方传回的单位已经是分
			return outParams;
		}
		
		payState = PaymentConstant.PAYMENT_STATE_PAYED;
		// 设置充值类型 - 不传则默认1-网银支付
		outParams.put(PaymentConstant.OPPOSITE_ORDERNO, transaction_id); // 对方订单号
		outParams.put(PaymentConstant.PAYMENT_STATE, payState);
		outParams.put(PaymentConstant.OPPOSITE_MONEY, total_fee); // 总金额，对方传回的单位已经是分
		
		return outParams;
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("out_trade_no");
	}
	
	private String readXmlNode(String resData,String nodePath) {
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(resData);
		} catch (DocumentException e) {
			throw new ValidationException("财付通初始化接口返回xml转换异常");
		}
		Node _requestToken = doc.selectSingleNode(nodePath);
		String requestToken = _requestToken == null ? "" : _requestToken.getText();
		return requestToken;
	}

}
