package com.woniu.sncp.pay.core.service.payment.platform.bluepay;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONArray;
import com.woniu.sncp.net.HttpclientUtil;
import com.woniu.sncp.pay.common.utils.encrypt.MD5Encrypt;
import com.woniu.sncp.pay.common.utils.http.HttpUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.codapay.schema.PaymentResult;
import com.woniu.sncp.pojo.payment.PaymentOrder;

//{
//		"country": {
//		"woniucurrency1": 1,
//		"woniucurrency2": 2
//
//		},
//		"currency": {
//		"woniucurrency1": 1,
//		"woniucurrency2": 2
//		},
//		"airtimeRestUrl": "https://sandbox.codapayments.com/airtime/api/restful/v1.0/Payment",
//		"apiKey": "",
//		"airtimeTxntype": 1,
//		"payUrlTemplate": "https://sandbox.codapayments.com/begin?type=3&txn_id=%s&browser_type=mobile-web"
//		}

/**
 * <pre>
 * 支付平台-CodaPay(CodaPay重试策略重发通知) - 公网环境，无测试环境
 * 1.商户merchantPwd是用于MD5加密使用，这里用的是DSA证书加密，不需要merchantPwd
 * 2.证书路径由EAI后台配置
 * 3.总价和单价不可以同时出现
 * 4.加密串是按照key的<font color=red>升序排列</font>，DSA加密，无需额外增加MD5加密串去校验
 * 5.我方收到消息处理流程
 * 	a.向CodaPay系统发送通知验证请求（URL验证）
 * 	b.通知参数的和我方是否一致
 * 	c.处理成功返回success给CodaPay
 * 	d.返回消息有时限限制(1分钟)，超时未处理则消息验证失败，CodaPay重发消息或我方通过订单查询接口去完成充值
 * 6.金额单位：元，需*100将精度设为分
 * </pre>
 * 
 */
@Service("BluepaySmsPayment")
public class BluepaySmsPayment extends AbstractPayment {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * CodaPay支付编码
	 */
	protected final String _charset_encode = "utf-8";

	protected BluepayConfig getConfig(Platform platform) {

		String ext = ObjectUtils.toString(platform.getExtend());
		if (StringUtils.isNotBlank(ext)) {

			return JSONObject.parseObject(ext).toJavaObject(BluepayConfig.class);

		}
		return null;
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams) {
		// 1.拼装参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		BluepayConfig config = getConfig(platform);

		String currencyStr = paymentOrder.getMoneyCurrency();
		logger.info("paymentOrder currency:{}", currencyStr);
		if (StringUtils.isBlank(currencyStr)) {
			currencyStr = paymentOrder.getCurrency();
			logger.info("paymentOrder currency:{}", currencyStr);
		}
		Map<String, String> urls = config.getSmsPayUrl();
		String url = MapUtils.getString(urls, currencyStr);
		String productId = platform.getMerchantNo();

		Assert.hasLength(url, "没有打到币种" + currencyStr + "对应的支付地址");
		String orderNo = paymentOrder.getOrderNo();
		String price =new BigDecimal(paymentOrder.getMoney().toString()).multiply(new BigDecimal(100)).intValue() +"";//请求单位为分

		Map<String, Object> params = new HashMap<String, Object>();

		String payUrl = url + String.format("?productId=%s&transactionId=%s&price=%s", productId, orderNo, price);
		params.put("payUrl", payUrl);

		return params;
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("transactionId");
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams, HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess)
			super.responseAndWrite(response, "success");
		else
			super.responseAndWrite(response, "fail");
	}

	public   Map<String, String> toParameterMap(HttpServletRequest req) {
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
	public Map<String, Object> validateBackParams(HttpServletRequest request, Platform platform)
			throws ValidationException, DataAccessException, PaymentRedirectException {
		logger.info("validateBackParams params:{}", JsonUtils.toJson(toParameterMap(request)));

		String queryString = request.getQueryString();
		String signStr = StringUtils.substringBefore(queryString, "&encrypt=") + platform.getPayKey();
		String sign = MD5Encrypt.encrypt(signStr, "utf-8");
		logger.info("签名字符串：{},签名{}", signStr, sign);
		String encrypt=request.getParameter("encrypt");
		if (!StringUtils.equalsIgnoreCase(sign,encrypt )) {
			logger.info("签名字符串：{},本地签名{},签名:{}", signStr, sign,encrypt);
			throw new ValidationException("支付平台加密校验失败");

		}

		String orderNo =  request.getParameter("t_id");;
		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "支付订单查询为空,orderNo:" + orderNo);

		String oppositeOrderNo = request.getParameter("bt_id");

		String payResult = StringUtils.trim(request.getParameter("status"));

		Map<String, Object> returned = new HashMap<String, Object>();
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		if (StringUtils.equals(payResult, "200")) { // 支付成功
			logger.info("CodaPay返回支付成功");
			String paymentMoney = StringUtils.trim(request.getParameter("price"));
			BigDecimal totalPrice = new BigDecimal(paymentMoney).setScale(0);
			BigDecimal money = new BigDecimal(paymentOrder.getMoney()).multiply(new BigDecimal("100")).setScale(0);
			if (money.compareTo(totalPrice) == 0) {
				logger.info("支付成功");
				returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf(money.intValue()));

				payState = PaymentConstant.PAYMENT_STATE_PAYED;
			} else {
				logger.info("支付失败，返回金额不一致");
			}
		} else { // 未支付
			logger.info("CodaPay返回未支付");
		}

		returned.put(PaymentConstant.PAYMENT_STATE, payState);

		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);

		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		return returned;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		// 1.拼装地址
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		BluepayConfig config = getConfig(platform);
		String currencyStr = paymentOrder.getMoneyCurrency();
		logger.info("paymentOrder currency:{}", currencyStr);
		if (StringUtils.isBlank(currencyStr)) {
			currencyStr = paymentOrder.getCurrency();
			logger.info("paymentOrder currency:{}", currencyStr);
		}
		String productId = platform.getMerchantNo();
		String orderNo = paymentOrder.getOrderNo();
		Map<String, String> payCheckUrl = config.getPayCheckUrl();
		String url = MapUtils.getString(payCheckUrl, currencyStr);
		JSONArray jsonArray = config.getOperator().getJSONArray(currencyStr);
		for (int i = 0; i < jsonArray.size(); i++) {
			try {
				String id = jsonArray.getString(i);
				if (StringUtils.isNotBlank(id) && StringUtils.isNumeric(id)) {
					// &encrypt=b198435f20fd032dab782b7a4d341678

					String paramString = String.format("operatorId=%s&productid=%s&t_id=%s", id, productId, orderNo);

					String query = url + "?" + paramString + "&encrypt=" + MD5Encrypt.encrypt(paramString + platform.getBackendKey());
					String response = HttpclientUtil.get(query);
					JSONObject   resp=JSONObject.parseObject(response);
//					resp.getJSONObject()
				}
			} catch (Exception e) {

				logger.info("", e);

			}
		}

		String oppositeOrderNo = paymentOrder.getOtherOrderNo();

		StringBuilder builder = new StringBuilder(url);

		// http://test.api.bluepay.tech/thaiCharge/service/queryTrans?operatorId=9&productid=1&t_id=20161223truemoney&encrypt=b198435f20fd032dab782b7a4d341678

		long txnId = Long.valueOf(oppositeOrderNo);
		PaymentResult result = null;

		logger.info("codapay 支付验证返回，resultCode:" + result.getResultCode() + ",retmsg:" + result.getResultDesc());

		Map<String, Object> outParams = new HashMap<String, Object>();
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		if (0 == result.getResultCode()) { // 支付不成功
			payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
			BigDecimal totalPrice = new BigDecimal(ObjectUtils.toString(result.getTotalPrice())).multiply(new BigDecimal("100")).setScale(0);
			BigDecimal money = new BigDecimal(paymentOrder.getMoney()).multiply(new BigDecimal("100")).setScale(0);
			if (money.compareTo(totalPrice) == 0) {
				logger.info("支付成功");
				outParams.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf(money.intValue()));

				payState = PaymentConstant.PAYMENT_STATE_PAYED;
			} else {
				logger.info("支付失败，返回金额不一致");
			}
			// 设置充值类型 - 不传则默认1-网银支付

		} else {
			logger.info("支付失败");
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
	// logger.error("CodaPay后台通知url验证异常,返回responseTxt=" + responseTxt +
	// ",notifyUrl:" + alipayNotifyURL);
	// throw new ValidationException("CodaPay后台通知url验证异常,返回responseTxt=" +
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
	// logger.info("==============CodaPay后台回调参数加密处理失败=================");
	// logger.info("我方加密参数：" + JsonUtils.toJson(params));
	// logger.info("==============CodaPay后台回调参数加密处理结束=================\n");
	// }
	// throw new ValidationException("支付平台回调参数加密校验失败");
	// }
	//
	// Map<String, Object> returned = new HashMap<String, Object>();
	// // 判断状态 组装数据
	// // success_details 流水号^收款方账号^收款账号姓名^付款金额^成功标识(S)^成功原因(null)^CodaPay内部流水号^完成时间
	// // fail_details 流水号^收款方账号^收款账号姓名^付款金额^失败标识(F)^失败原因^CodaPay内部流水号^完成时间。
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
		if (isSccess)
			super.responseAndWrite(response, "success");
		else
			super.responseAndWrite(response, "fail");
	}

}