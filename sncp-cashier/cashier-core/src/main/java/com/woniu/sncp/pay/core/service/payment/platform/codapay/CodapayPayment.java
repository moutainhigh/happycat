package com.woniu.sncp.pay.core.service.payment.platform.codapay;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.woniu.sncp.pay.core.service.payment.platform.codapay.schema.InitResult;
import com.woniu.sncp.pay.core.service.payment.platform.codapay.schema.ItemInfo;
import com.woniu.sncp.pay.core.service.payment.platform.codapay.schema.PaymentResult;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.common.utils.RefundmentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.cbss.api.imprest.direct.request.DIOrderRefundQueryRequest;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundBackCallData;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundQueryData;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundQueryResponse;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.encrypt.Dsa;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptFactory;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptStringUtils;
import com.woniu.sncp.pay.common.utils.encrypt.Rsa;
import com.woniu.sncp.pay.common.utils.http.PayCheckUtils;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.tools.AlipayHelper;
import com.woniu.sncp.pay.core.transfer.model.TransferModel;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.pojo.payment.TransferOrder;
import com.woniu.sncp.pojo.refund.PayRefundBatch;
import org.springframework.web.client.RestTemplate;
//Myanmar:



//泰铢THB
//		马来币 MYR
//		新加坡 SGD
//		菲律宾 PHP
//		印尼 IDR
//		缅甸 BUK
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
@Service("codapayPayment")
public class CodapayPayment extends AbstractPayment {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * CodaPay支付编码
	 */
	protected final String _charset_encode = "utf-8";

	@Autowired
	RestTemplate restTemplate;
	protected CodaRestUtil createCodaRestUtil(PaymentOrder paymentOrder, Platform platform) {

		String ext = ObjectUtils.toString(platform.getExtend());
		String country = null;
		String currency = null;
		String airtimeRestUrl = null;
		String apiKey = null;
		String paymentType = null;
		String payUrlTemplate = null;
		if (StringUtils.isNotBlank(ext)) {
			JSONObject extend = JSONObject.parseObject(ext);
			JSONObject countryObj = extend.getJSONObject("country");
			JSONObject currencyObj = extend.getJSONObject("currency");
			String currencyStr=paymentOrder.getMoneyCurrency();
			logger.info("paymentOrder currency:{}",currencyStr);
			if(StringUtils.isBlank(currencyStr)){
				currencyStr=paymentOrder.getCurrency();
				logger.info("paymentOrder currency:{}",currencyStr);
			}

			country = countryObj.getString(currencyStr);
			currency = currencyObj.getString(currencyStr);
			Assert.notNull(country,"找不到货币"+currencyStr+"对应的国家编码");
			Assert.notNull(currency,"找不到货币"+currencyStr+"对应的货币编码");
			// appId = extend.getString("appId");
			airtimeRestUrl = extend.getString("airtimeRestUrl");
			Assert.hasLength(airtimeRestUrl,"airtimeRestUrl配置不能为空");

			apiKey = extend.getString("apiKey");

			Assert.hasLength(apiKey,"apiKey配置不能为空");
			paymentType = extend.getString("airtimeTxntype");
			Assert.hasLength(paymentType,"airtimeTxntype配置不能为空");

			payUrlTemplate = extend.getString("payUrlTemplate");
			Assert.hasLength(payUrlTemplate,"payUrlTemplate配置不能为空");

		}
		CodaRestUtil codaRestUtil = new CodaRestUtil(payUrlTemplate, airtimeRestUrl, apiKey, Short.valueOf(country), Short.valueOf(currency),Short.valueOf(paymentType));
		return codaRestUtil;
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams) {
		// 1.拼装参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		CodaRestUtil CodaRestUtil = createCodaRestUtil(paymentOrder, platform);

		String orderNo = paymentOrder.getOrderNo();
		ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
		ItemInfo itemInfo = new ItemInfo();
		paymentOrder.getCardTypeId();

		itemInfo.setCode(orderNo);
		itemInfo.setName(paymentOrder.getProductname());

		itemInfo.setPrice(paymentOrder.getMoney());
		items.add(itemInfo);
		InitResult result = CodaRestUtil.initTxn(orderNo, items, new HashMap<String, String>());

		logger.info("[InitTxn] ResultCode=" + result.getResultCode() + ", TxnId=" + result.getTxnId());
		Map<String, Object> params = new HashMap<String, Object>();
		if (result.getResultCode() == 0) {
			String payUrl = CodaRestUtil.formatPayUrl(result.getTxnId());
			params.put("payUrl", payUrl);
			params.put("oppositeOrderNo", ObjectUtils.toString(result.getTxnId()));

		}

		return params;
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("OrderId");
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams, HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess)
			super.responseAndWrite(response, "success");
		else
			super.responseAndWrite(response, "fail");
	}

	public static Map<String, String> toParameterMap(HttpServletRequest req) {
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

		// // 1.到CodaPay平台验证URL是否合法
		// String notify_id = StringUtils.trim(request.getParameter("notify_id"));
		// String sign = StringUtils.trim(request.getParameter("sign"));
		// String partner = platform.getMerchantNo();
		// String alipayNotifyURL = platform.getPayCheckUrl() +
		// "?service=notify_verify&partner=" + partner + "&notify_id=" + notify_id;
		// String responseTxt = AlipayHelper.checkURL(alipayNotifyURL);
		//
		// if (!"true".equals(responseTxt)) {
		// throw new ValidationException("CodaPay后台验证加密异常：验证url异常,返回responseTxt=" +
		// responseTxt);
		// }

		String orderNo = getOrderNoFromRequest(request);
		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "支付订单查询为空,orderNo:" + orderNo);
		CodaRestUtil CodaRestUtil = createCodaRestUtil(paymentOrder, platform);
		// 加密校验
		Assert.isTrue(CodaRestUtil.validateChecksum(request), "签名验证不正确");
		String oppositeOrderNo = request.getParameter("TxnId");

		// if (!CodaRestUtil.validateChecksum(request)) {
		// // 加密校验失败
		// if (logger.isInfoEnabled()) {
		// logger.info("==============CodaPay后台加密处理失败=================");
		// logger.info("我方加密参数：" + JsonUtils.toJson(params));
		// logger.info("==============CodaPay后台加密处理结束=================\n");
		// }
		// throw new ValidationException("支付平台加密校验失败");
		// }

		String payResult = StringUtils.trim(request.getParameter("ResultCode"));

		Map<String, Object> returned = new HashMap<String, Object>();
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		if (StringUtils.equals(payResult, "0")) { // 支付成功
			logger.info("CodaPay返回支付成功");
			String paymentMoney = StringUtils.trim(request.getParameter("TotalPrice"));
			BigDecimal totalPrice = new BigDecimal(paymentMoney).multiply(new BigDecimal("100")).setScale(0);
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
		// returned.put(PaymentConstant.IMPREST_OPPOSITE_MONEY,
		// String.valueOf(NumberUtils.toFloat(paymentMoney) * 100));

		// 不验证imprestMode,直接取订单中imprestMode
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		return returned;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		// 1.拼装地址
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		CodaRestUtil CodaRestUtil = createCodaRestUtil(paymentOrder, platform);

		String oppositeOrderNo = paymentOrder.getOtherOrderNo();
		long txnId = Long.valueOf(oppositeOrderNo);
		PaymentResult result = CodaRestUtil.inquiryTxn(txnId);

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

//	@Override
//	public Map<String, Object> backendParamsValidate(HttpServletRequest request, Platform platform) throws ValidationException, DataAccessException {
//		// 验证notify_url
//		String notifyId = StringUtils.trim(request.getParameter("notify_id"));
//		String partner = platform.getMerchantNo();
//		String alipayNotifyURL = platform.getPayCheckUrl() + "?service=notify_verify&partner=" + partner + "&notify_id=" + notifyId;
//		String responseTxt = AlipayHelper.checkURL(alipayNotifyURL);
//		if (!"true".equals(responseTxt)) {
//			logger.error("CodaPay后台通知url验证异常,返回responseTxt=" + responseTxt + ",notifyUrl:" + alipayNotifyURL);
//			throw new ValidationException("CodaPay后台通知url验证异常,返回responseTxt=" + responseTxt);
//		}
//
//		// 订单验证
//		String batchNo = StringUtils.trim(request.getParameter("batch_no"));
//		TransferOrder queryOrder = null;// transferOrderService.queryOrder(batchNo);
//		Assert.notNull(queryOrder, "转账订单查询为空,orderNo:" + batchNo);
//
//		// 获取数据
//		String sign = StringUtils.trim(request.getParameter("sign"));
//		Map alipay = request.getParameterMap();
//		Properties params = new Properties();
//		for (Iterator<Entry<String, Object>> keyValuePairs = alipay.entrySet().iterator(); keyValuePairs.hasNext();) {
//			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) keyValuePairs.next();
//			String key = entry.getKey();
//			String value = request.getParameter(key);
//			if (!"sign".equalsIgnoreCase(key) && !"sign_type".equalsIgnoreCase(key)) {
//				try {
//					params.put(key, URLDecoder.decode(value, _charset_encode));
//				} catch (UnsupportedEncodingException e) {
//					logger.error(e.getMessage(), e);
//				}
//			}
//		}
//
//		// 验证签名
//		String pubKey = AlipayHelper.readText(platform.getPublicUrl());
//		boolean result = AlipayHelper.dsaCheck(params, pubKey, _charset_encode, sign);
//		if (!result) {
//			// 加密校验失败
//			if (logger.isInfoEnabled()) {
//				logger.info("==============CodaPay后台回调参数加密处理失败=================");
//				logger.info("我方加密参数：" + JsonUtils.toJson(params));
//				logger.info("==============CodaPay后台回调参数加密处理结束=================\n");
//			}
//			throw new ValidationException("支付平台回调参数加密校验失败");
//		}
//
//		Map<String, Object> returned = new HashMap<String, Object>();
//		// 判断状态 组装数据
//		// success_details 流水号^收款方账号^收款账号姓名^付款金额^成功标识(S)^成功原因(null)^CodaPay内部流水号^完成时间
//		// fail_details 流水号^收款方账号^收款账号姓名^付款金额^失败标识(F)^失败原因^CodaPay内部流水号^完成时间。
//		String successDetails = StringUtils.trim(request.getParameter("success_details"));
//		if (StringUtils.isBlank(successDetails)) {
//			String failDetails = StringUtils.trim(request.getParameter("fail_details"));
//			String[] failDtlArr = failDetails.split("^");
//			returned.put(PaymentConstant.OPPOSITE_MONEY, failDtlArr[3]);
//			returned.put(PaymentConstant.TRANSFER_STATE, TransferOrder.TRANSFER_STATE_FAILED);
//			returned.put(PaymentConstant.TRANSFER_STATE_MESSAGE, failDtlArr[5]);
//			returned.put(PaymentConstant.OPPOSITE_ORDERNO, failDtlArr[6]);
//			returned.put(PaymentConstant.TRANSFER_ACCOUNT, failDtlArr[1]);
//		} else {
//			String[] successDtlArr = successDetails.split("^");
//			returned.put(PaymentConstant.OPPOSITE_MONEY, successDtlArr[3]);
//			returned.put(PaymentConstant.TRANSFER_STATE,
//					"S".equalsIgnoreCase(successDtlArr[4]) ? TransferOrder.TRANSFER_STATE_COMPLETED : TransferOrder.TRANSFER_STATE_NOT_COMPLETED);
//			returned.put(PaymentConstant.TRANSFER_STATE_MESSAGE, successDtlArr[5]);
//			returned.put(PaymentConstant.OPPOSITE_ORDERNO, successDtlArr[6]);
//			returned.put(PaymentConstant.TRANSFER_ACCOUNT, successDtlArr[1]);
//		}
//
//		return returned;
//	}

	@Override
	public void backendResponse(Map<String, Object> params, HttpServletResponse response, boolean isSccess) {
		if (isSccess)
			super.responseAndWrite(response, "{\"ResultCode\":0}");
		else
			super.responseAndWrite(response, "{\"ResultCode\":-1}");
	}

}