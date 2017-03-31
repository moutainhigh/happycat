package com.woniu.sncp.pay.core.service.payment.platform.unionpay;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.unionpay.acp.sdk.HttpClient;
import com.unionpay.acp.sdk.LogUtil;
import com.unionpay.acp.sdk.SDKConfig;
import com.unionpay.acp.sdk.SDKConstants;
import com.unionpay.acp.sdk.SDKUtil;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.date.DateUtils;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;

/**
 * 银联在线支付 
 * 
 *  支付接口      
 *  异步通知      
 *  前台跳转      
 *  支付验证      
 * 
 * @author caowl
 *
 */
@Component("unionPayAppPayment")
public class UnionPayAppPayment extends AbstractPayment {

	
	private final static String VERSION = "5.0.0";
	private final static String DEF_CHARSET_ENCODING = "UTF-8";
	private final static String CURRENCY_RMB = "156";
	private final static String TRANS_TYPE_CONSUME = "01";//消费交易类型
	private final static String CHANNEL_TYPE = "08";
	private final static String SIGN_METHOD = "01";
	private final static String DATE_FORMAT = "yyyyMMddHHmmss";
	
	@Override
	public String encode(Map<String, Object> inParams) throws ValidationException {
		return null;
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		
		Assert.notEmpty(inParams, "inParams must have entries");
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		Assert.notNull(paymentOrder, "支付订单查询为空");
		Assert.notNull(platform, "支付平台对象为空");
		
		Map<String, String> data = new HashMap<String, String>();
		// 版本号
		data.put("version", VERSION);
		// 字符集编码 默认"UTF-8"
		data.put("encoding", DEF_CHARSET_ENCODING);
		// 签名方法 01 RSA
		data.put("signMethod", SIGN_METHOD);
		// 交易类型 01-消费
		data.put("txnType", TRANS_TYPE_CONSUME);
		// 交易子类型 01:自助消费 02:订购 03:分期付款
		data.put("txnSubType", "01");
		// 业务类型
		data.put("bizType", "000201");
		// 渠道类型，07-PC，08-手机
		data.put("channelType", CHANNEL_TYPE);
		// 前台通知地址 ，控件接入方式无作用
		data.put("frontUrl", platform.getFrontUrl(paymentOrder.getMerchantId()));
		// 后台通知地址
		data.put("backUrl", platform.getBehindUrl(paymentOrder.getMerchantId()));
		// 接入类型，商户接入填0 0- 商户 ， 1： 收单， 2：平台商户
		data.put("accessType", "0");
		// 商户号码，请改成自己的商户号
		data.put("merId", platform.getMerchantNo());
		// 商户订单号，8-40位数字字母
		data.put("orderId", convert2UnionOrderNo(paymentOrder.getOrderNo()));
		// 订单发送时间，取系统时间
		data.put("txnTime", new SimpleDateFormat(DATE_FORMAT).format(paymentOrder.getCreate()));
		BigDecimal money = new BigDecimal(paymentOrder.getMoney().toString());
		// 交易金额，单位分
		data.put("txnAmt", money.multiply(new BigDecimal(100)).intValue() + "");
		// 交易币种
		data.put("currencyCode", CURRENCY_RMB);
		// 请求方保留域，透传字段，查询、通知、对账文件中均会原样出现
		// data.put("reqReserved", "透传信息");
		// 订单描述，可不上送，上送时控件中会显示该信息
		// data.put("orderDesc", "订单描述");
		
	    //表单号:31586 modified by fuzl@snail.com
		//payTimeout  需要转换为YYYYMMDDhhmmss的时间-->订单创建时间+配置的超时时间(分钟)
		if(null != platform.getTransTimeout() && platform.getTransTimeout() >0 ){
			String payTimeout = DateUtils.format(
					org.apache.commons.lang.time.DateUtils.addMinutes(
							paymentOrder.getCreate(), platform.getTransTimeout().intValue()), DATE_FORMAT);
			data.put("payTimeout", payTimeout);//单位YYYYMMDDhhmmss
		}

		data = signData(data, DEF_CHARSET_ENCODING, platform.getPrivateUrl(), platform.getPrivatePassword());
		
		// 交易请求url 从配置文件读取
//		String requestAppUrl = SDKConfig.getConfig().getAppRequestUrl();
		String requestAppUrl = platform.getPayUrl();

		String resultStr = postData(requestAppUrl, data, DEF_CHARSET_ENCODING);
		
		Map<String, String> resData = new HashMap<String, String>();
		//验证签名
		if (StringUtils.isNotEmpty(resultStr)) {
			SDKConfig.getConfig().setValidateCertDir(getPathDir(platform.getPublicUrl()));
			// 将返回结果转换为map
			resData = SDKUtil.convertResultStringToMap(resultStr);
			if (SDKUtil.validate(resData, DEF_CHARSET_ENCODING)) {
				logger.info("验证签名成功");
			} else {
				logger.info("验证签名失败");
				throw new ValidationException("银联创建订单验证签名失败");
			}
			// 打印返回报文
			logger.info("打印返回报文：" + resultStr);
		} else {
			throw new ValidationException("银联创建订单请求返回信息[" + resultStr + "] 错误!");
		}
		
		String respCode = resData.get("respCode");
		//验证订单创建是否成功
		if (!"00".equals(respCode)) {
			throw new ValidationException("银联创建订单请求错误,respCode=" + respCode
					+ ",respMsg=" + resData.get("respMsg"));
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("unionMobilePayImprestSubmit", resultStr);
		// 提交给对方的支付地址
		params.put("payUrl", "");
		// 提交给对方的支付编码
		params.put("acceptCharset", DEF_CHARSET_ENCODING); 
		params.put("defaultBankName", "");
		
		return params;
	}
	
	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException,
			DataAccessException, PaymentRedirectException {
		
		String encoding = request.getParameter(SDKConstants.param_encoding);
		if (StringUtils.isEmpty(encoding)) {
			encoding = DEF_CHARSET_ENCODING;
		}
		// 获取请求参数中所有的信息
		Map<String, String> reqParam = getAllRequestParam(request);
		// 打印请求报文
		LogUtil.printRequestLog(reqParam);

		Map<String, String> valideData = null;
		if (null != reqParam && !reqParam.isEmpty()) {
			Iterator<Entry<String, String>> it = reqParam.entrySet().iterator();
			valideData = new HashMap<String, String>(reqParam.size());
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				try {
					value = new String(value.getBytes("ISO-8859-1"), encoding);
				} catch (UnsupportedEncodingException e1) {
					throw new ValidationException("Encoding String error", e1);
				}
				valideData.put(key, value);
			}
		}

		SDKConfig.getConfig().setValidateCertDir(getPathDir(platform.getPublicUrl()));
		// 验证签名
		if (!SDKUtil.validate(valideData, encoding)) {
			LogUtil.writeLog("验证签名结果[失败].");
			throw new ValidationException("验证签名结果[失败].");
		}
		String orderId = valideData.get("orderId");
		// 订单查询
		String snailOrderNo = convert2SnailOrderNo(orderId);
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(snailOrderNo);
		Assert.notNull(paymentOrder, "支付订单查询为空,orderNo:" + snailOrderNo);
		//消费交易的流水号，供后续查询用
		String queryId = valideData.get("queryId");
		Assert.hasText(queryId, "交易流水号不能为空");
		//交易
//		String txnAmt = valideData.get("txnAmt");
//		String currencyCode = valideData.get("currencyCode");
		
		//清算
		String settleAmt = valideData.get("settleAmt");
//		String settleCurrencyCode = valideData.get("settleCurrencyCode");
		
		String merId = valideData.get("merId");
		Assert.hasText(merId, "商户代码不能为空");
		
		//由于没有IP白名单  回调后直接去验证
		Map<String, Object> inParams = new HashMap<String, Object>();
		inParams.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		inParams.put(PaymentConstant.PAYMENT_PLATFORM, platform);
		inParams.put(PaymentConstant.OPPOSITE_ORDERNO, queryId);
		Map<String, Object> checkOrderMap = checkOrderIsPayed(inParams);
		
		if (!PaymentConstant.PAYMENT_STATE_PAYED.equals(checkOrderMap.get(PaymentConstant.PAYMENT_STATE))
				|| !queryId.equals(checkOrderMap.get(PaymentConstant.OPPOSITE_ORDERNO))
				|| !settleAmt.equals(checkOrderMap.get(PaymentConstant.OPPOSITE_MONEY))) {
			logger.warn("验证订单失败");
			throw new ValidationException("验证订单失败");
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		params.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		params.put(PaymentConstant.OPPOSITE_MONEY, settleAmt);
		params.put(PaymentConstant.PAYMENT_MODE, "");
		params.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		params.put(PaymentConstant.OPPOSITE_ORDERNO, queryId);
		
		request.setAttribute("merchantId", merId);
		request.setAttribute("merchantOrderId", orderId);

		return params;
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		
		if (isImprestedSuccess){
			response.setStatus(HttpServletResponse.SC_OK);
			super.responseAndWrite(response, "");
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			HttpServletRequest request = (HttpServletRequest) inParams.get("request");
			super.responseAndWrite(response, request.getParameter("respMsg"));
		}
	}

	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		Map<String, Object> outParams = new HashMap<String, Object>();
		// 封装请求数据
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		Map<String, String> data = new HashMap<String, String>();
		// 版本号
		data.put("version", VERSION);
		// 字符集编码 默认"UTF-8"
		data.put("encoding", DEF_CHARSET_ENCODING);
		// 签名方法 01 RSA
		data.put("signMethod", SIGN_METHOD);
		// 交易类型 
		data.put("txnType", "00");
		// 交易子类型 
		data.put("txnSubType", "00");
		// 业务类型
		data.put("bizType", "000201");
		// 渠道类型，07-PC，08-手机
//		data.put("channelType", CHANNEL_TYPE);
		// 接入类型，商户接入填0 0- 商户 ， 1： 收单， 2：平台商户
		data.put("accessType", "0");
		// 商户号码，请改成自己的商户号
		data.put("merId", platform.getMerchantNo());
		if (inParams.get(PaymentConstant.OPPOSITE_ORDERNO) != null) {
			data.put("queryId", inParams.get(PaymentConstant.OPPOSITE_ORDERNO).toString());
		}
		// 商户订单号，请修改被查询的交易的订单号
		data.put("orderId", convert2UnionOrderNo(paymentOrder.getOrderNo()));
		// 订单发送时间，请修改被查询的交易的订单发送时间
		data.put("txnTime", new SimpleDateFormat(DATE_FORMAT).format(paymentOrder.getCreate()));
		
		data.put("reqReserved", "");

		data = signData(data, DEF_CHARSET_ENCODING,platform.getPrivateUrl(), platform.getPrivatePassword());

		// 交易请求url 从配置文件读取
//		String url = SDKConfig.getConfig().getSingleQueryUrl();
		String url = platform.getPayCheckUrl();
				
		String resultStr = postData(url,data, DEF_CHARSET_ENCODING);
		
		Map<String, String> resData = Collections.emptyMap();
		//验证签名
		if (StringUtils.isNotEmpty(resultStr)) {
			SDKConfig.getConfig().setValidateCertDir(getPathDir(platform.getPublicUrl()));
			// 将返回结果转换为map
			resData = SDKUtil.convertResultStringToMap(resultStr);
			if (SDKUtil.validate(resData, DEF_CHARSET_ENCODING)) {
				logger.info("验证签名成功");
			} else {
				logger.info("消费交易应答签名错误 orderNo:{},resp:{}", new Object[]{paymentOrder.getOrderNo(),resultStr});
				outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
				return outParams;
			}
			// 打印返回报文
			logger.info("打印返回报文：" + resultStr);
		} else {
			logger.info("消费交易应答返回为空，请求参数:{}", data);
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return outParams;
		}
		
		
		String origRespCode = resData.get("origRespCode");
		String respCode = resData.get("respCode");
		
		if("00".equals(origRespCode) && "00".equals(respCode)){
			String oppositeOrderNo = String.valueOf(resData.get("queryId"));
			String settleAmt = String.valueOf(resData.get("settleAmt"));
			outParams.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo); // 对方订单号
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
			outParams.put(PaymentConstant.OPPOSITE_MONEY, settleAmt); // 总金额，对方传回的单位已经是分
		} else {
			logger.info("消费交易应答未支付 返回:{}", resultStr);
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}
		
		return outParams;
	}
	

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("orderNumber");
	}
	
	/**
	 * java main方法 数据提交 　　 对数据进行签名
	 * 
	 * @param contentData
	 * @return　签名后的map对象
	 */
	private static Map<String, String> signData(Map<String, String> contentData, String encoding,
			String certPath, String certPwd) {
		if (StringUtils.isEmpty(encoding)) {
			encoding = "UTF-8";
	    }
		Entry<String, String> obj = null;
		Map<String, String> submitFromData = new HashMap<String, String>();
		for (Iterator<Entry<String, String>> it = contentData.entrySet().iterator(); it.hasNext();) {
			obj = it.next();
			String value = obj.getValue();
			if (StringUtils.isNotBlank(value)) {
				// 对value值进行去除前后空处理
				submitFromData.put(obj.getKey(), value.trim());
			}
		}
		//签名
		SDKUtil.signByCertInfo(submitFromData, encoding, certPath, certPwd);
		return submitFromData;
	}
	
	/**
	 * @param snailOrderNo
	 *            eg:20140218-198-007-0000350921
	 * @return <pre>
	 * </pre>
	 */
	private static String convert2UnionOrderNo(String snailOrderNo) {
		return snailOrderNo.replaceAll("-", "");
	}

	/**
	 * @param unionOrderNo
	 *            eg:2016031420070070000002528 to 20160314-2007-007-0000002528
	 * @return <pre>
	 * </pre>
	 */
	private static String convert2SnailOrderNo(String unionOrderNo) {
		StringBuffer stringbuffer = new StringBuffer(unionOrderNo);
		int[] indexofInsert = new int[] { 8, 13, 17 };
		for (int i = 0; i < indexofInsert.length; i++) {
			stringbuffer.insert(indexofInsert[i], "-");
		}
		return stringbuffer.toString();
	}
	
	/**
	 * 获取请求参数中所有的信息
	 * 
	 * @param request
	 * @return
	 */
	private static Map<String, String> getAllRequestParam(final HttpServletRequest request) {
		Map<String, String> res = new HashMap<String, String>();
		Enumeration<?> temp = request.getParameterNames();
		if (null != temp) {
			while (temp.hasMoreElements()) {
				String en = (String) temp.nextElement();
				String value = request.getParameter(en);
				//在报文上送时，如果字段的值为空，则不上送<下面的处理为在获取所有参数数据时，判断若值为空，则删除这个字段>
				if (StringUtils.isNotEmpty(value)) {
					res.put(en, value);
				}
			}
		}
		return res;
	}

	
	/**
	 * 提交请求
	 * @param url
	 * @param data
	 * @param encoding
	 * @return
	 */
	private String postData(String url, Map<String, String> data, String encoding) {
		logger.info("request URL[" + url + "] params[" + data.toString()+"]");
		String resultStr = "";
		HttpClient hc = new HttpClient(url, 30000, 30000);
		try {
			// 查询交易
			int status = hc.send(data, DEF_CHARSET_ENCODING);
			logger.info("response status:" + status);
			if (200 == status) {
				resultStr = hc.getResult();
			} else {
				logger.warn("response body:" + hc.getResult());
			}
		} catch (Exception e) {
			throw new ValidationException("银联创建订单请求失败");
		}
		return resultStr;
	}
	
	private String getPathDir(String path){
		if(StringUtils.isEmpty(path)){
			return "";
		}
		
		int lastIndexOf = path.lastIndexOf("/");
		return path.substring(0, lastIndexOf);
	}
	
}
