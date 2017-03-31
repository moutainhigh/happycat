package com.woniu.sncp.pay.core.service.payment.platform.unionpay.new1;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.unionpay.acp.sdk.HttpClient;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.date.DateUtils;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.unionpay.new1.utils.SDKConfig;
import com.woniu.sncp.pay.core.service.payment.platform.unionpay.new1.utils.SDKUtil;
import com.woniu.sncp.pojo.payment.PaymentOrder;

/**
 * 
 * <p>descrption: 银联在线支付 PC端新版
 * 
 *  支付接口      
 *  异步通知      
 *  前台跳转      
 *  支付验证   </p>
 * 
 * @author fuzl
 * @date   2016年9月13日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@Service("unionPayPaymentNew_1")
public class UnionPayPaymentNew_1 extends AbstractPayment {

	private final static String VERSION = "5.0.0";
	private final static String DEF_CHARSET_ENCODING = "UTF-8";
	private final static String CURRENCY_RMB = "156";
	private final static String TRANS_TYPE_CONSUME = "01";//消费交易类型
	private final static String CHANNEL_TYPE = "07";//07 PC,08 手机
	private final static String SIGN_METHOD = "01";
	private final static String DATE_FORMAT = "yyyyMMddHHmmss";
	
	@Value("${pub.key.path}")
	private String pubKeyPath;
	
	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		return null;
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		
		// 1.拼装参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
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
		
		
		// 商户号码，请改成自己的商户号
		data.put("merId", platform.getMerchantNo());
		// 接入类型，商户接入填0 0- 商户 ， 1： 收单， 2：平台商户
		data.put("accessType", "0");
		// 商户订单号，8-40位数字字母
		data.put("orderId", convert2UnionOrderNo(paymentOrder.getOrderNo()));
		// 订单发送时间，取系统时间
		data.put("txnTime", new SimpleDateFormat(DATE_FORMAT).format(paymentOrder.getCreate()));
		// 交易币种
		data.put("currencyCode", CURRENCY_RMB);
		BigDecimal money = new BigDecimal(paymentOrder.getMoney().toString());
		// 交易金额，单位分
		data.put("txnAmt", money.multiply(new BigDecimal(100)).intValue() + "");
		
		if(null != platform.getTransTimeout() && platform.getTransTimeout() >0 ){
			String payTimeout = DateUtils.format(
					org.apache.commons.lang.time.DateUtils.addMinutes(
							paymentOrder.getCreate(), platform.getTransTimeout().intValue()), DATE_FORMAT);
			data.put("payTimeout", payTimeout);
		}
		
		// 前台通知地址 ，控件接入方式无作用
		data.put("frontUrl", platform.getFrontUrl(paymentOrder.getMerchantId()));
		// 后台通知地址
		data.put("backUrl", platform.getBehindUrl(paymentOrder.getMerchantId()));
		// data.put("reqReserved", "透传字段");        		      //请求方保留域，如需使用请启用即可；透传字段（可以实现商户自定义参数的追踪）本交易的后台通知,对本交易的交易状态查询交易、对账文件中均会原样返回，商户可以按需上传，长度为1-1024个字节		

		data = signData(data, DEF_CHARSET_ENCODING, platform.getPrivateUrl(), platform.getPayKey());
		
		Map<String, Object> params = new HashMap<String, Object>();
		// 提交给对方的支付地址
		params.put("payUrl", platform.getPayUrl()); // 提交给对方的支付地址
		params.putAll(data);
		return params;
	}
	
	
	
	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException,
			DataAccessException, PaymentRedirectException {
		
		// 获取请求参数中所有的信息
		Map<String, String> reqParam = getAllRequestParam(request);
		// 打印请求报文
		logger.info(reqParam.toString());
		Map<String, String> valideData = null;
		if (null != reqParam && !reqParam.isEmpty()) {
			Iterator<Entry<String, String>> it = reqParam.entrySet().iterator();
			valideData = new HashMap<String, String>(reqParam.size());
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				try {
					value = new String(value.getBytes("ISO-8859-1"), DEF_CHARSET_ENCODING);
				} catch (UnsupportedEncodingException e1) {
					throw new ValidationException("Encoding String error", e1);
				}
				valideData.put(key, value);
			}
		}
		
		String orderId = valideData.get("orderId");
		// 订单查询
		String snailOrderNo = convert2SnailOrderNo(orderId);
		// 验证签名
		SDKConfig.getConfig().setValidateCertDir(this.pubKeyPath+platform.getPlatformId()+"/"+platform.getMerchantNo());
		if (!SDKUtil.validate(valideData, DEF_CHARSET_ENCODING)) {
			if (logger.isInfoEnabled()) {
				logger.info("==============银联在线支付后台回调加密处理失败=================");
				logger.info("校验参数串：" + valideData);
			}
			throw new ValidationException("银联在线支付回调接口加密校验失败");
		}
		
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(snailOrderNo);
		if (paymentOrder == null) {
			logger.info("支付订单查询为空,orderNo:" + snailOrderNo);
			throw new ValidationException("支付订单查询为空,orderNo:" + snailOrderNo);
		}
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
		inParams.put(PaymentConstant.OPPOSITE_MONEY, paymentOrder.getMoney());
		
		
		Map<String, Object> checkOrderMap = checkOrderIsPayed(inParams);
		
		
		if (!PaymentConstant.PAYMENT_STATE_PAYED.equals(checkOrderMap.get(PaymentConstant.PAYMENT_STATE))
				|| !queryId.equals(checkOrderMap.get(PaymentConstant.OPPOSITE_ORDERNO))
				|| !settleAmt.equals(checkOrderMap.get(PaymentConstant.OPPOSITE_MONEY))) {
			logger.info("银联在线支付返回验证失败，订单号："+snailOrderNo+",对方订单号："+queryId);
			throw new ValidationException("银联在线支付返回验证失败，订单号："+snailOrderNo+",对方订单号："+queryId);
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		params.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		params.put(PaymentConstant.OPPOSITE_MONEY, settleAmt);
		params.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		params.put(PaymentConstant.OPPOSITE_ORDERNO, queryId);
		
		request.setAttribute("merchantId", merId);
		request.setAttribute("merchantOrderId", orderId);
		
		return params;
		
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		HttpServletRequest request = (HttpServletRequest) inParams.get("request");
		if (isImprestedSuccess){
			response.setStatus(HttpServletResponse.SC_OK);
			super.responseAndWrite(response, "");
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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

		data = signData(data, DEF_CHARSET_ENCODING, platform.getPrivateUrl() , platform.getPayKey());

		// 交易请求url 
		String url = platform.getPayCheckUrl();
				
		String resultStr = postData(url,data, DEF_CHARSET_ENCODING);
		
		Map<String, String> resData = Collections.emptyMap();
		//验证签名
		SDKConfig.getConfig().setValidateCertDir(this.pubKeyPath+platform.getPlatformId()+"/"+platform.getMerchantNo());
		if (StringUtils.isNotEmpty(resultStr)) {
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
//		SDKConfig.getConfig().setSignCertPath(certPath);
		SDKUtil.signByCertInfo(submitFromData, certPath, certPwd, encoding);
		return submitFromData;
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return convert2SnailOrderNo(request.getParameter("orderId"));
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
	 *            eg:201402181980070000350921 to 20140218-198-007-0000350921
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
			throw new ValidationException("银联创建订单请求失败",e);
		}
		return resultStr;
	}
}
