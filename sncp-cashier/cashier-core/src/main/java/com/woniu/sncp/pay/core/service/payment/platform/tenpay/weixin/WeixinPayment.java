package com.woniu.sncp.pay.core.service.payment.platform.tenpay.weixin;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.springframework.dao.DataAccessException;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.net.NetServiceException;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptFactory;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptStringUtils;
import com.woniu.sncp.pay.common.utils.encrypt.Md5;
import com.woniu.sncp.pay.common.utils.xml.XmlConvertUtil;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.tools.AlipayHelper;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.tools.IpUtils;

/**
 * 
 * <p>descrption: 
 * 微信支付通用平台 
 * 
 * 统一下单接口
 * </p>
 * 
 * @author fuzl
 * @date   2016年9月2日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */

public class WeixinPayment extends AbstractPayment {
	
	/**
	 * 微信公众号支付编码
	 */
	private final String _charset_encode = HTTP.UTF_8;

	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		String source = (String) inParams.get("source");
		String encrypted = StringUtils.upperCase(MD5Encrypt.encrypt(source,_charset_encode));
		if (logger.isInfoEnabled()) {
			logger.info("=========微信公众号Wap支付加密开始=========");
			logger.info("source：" + source);
			logger.info("encrypted：" + encrypted);
			logger.info("=========微信公众号Wap支付加密结束=========\n");
		}
		return encrypted;
	}

	/**
	 * 统一下单接口,返回预支付订单号或者微信二维码链接
	 */
	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {

		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		if(StringUtils.isEmpty(platform.getPayUrl())){
			throw new ValidationException("微信公众号支付Url配置不正确");
		}
		
		String mchIdAndAppId = platform.getMerchantNo();
		String mchId = mchIdAndAppId.split("-")[0];//商户号
		String appId = mchIdAndAppId.split("-")[1];//公众账号ID
		String tradeType = mchIdAndAppId.split("-")[2];//交易类型
		
		Map<String,Object> params = new TreeMap<String,Object>();
		params.put("appid", appId);
		params.put("mch_id", mchId);
		params.put("device_info", "WEB");//PC网页或公众号内支付请传"WEB"
		params.put("nonce_str", MD5Encrypt.encrypt(String.valueOf(RandomUtils.nextLong())));
		params.put("body", StringUtils.trim((String) inParams.get("productName")));//产品名称
		params.put("out_trade_no", paymentOrder.getOrderNo());
		params.put("total_fee", String.valueOf((new BigDecimal(paymentOrder.getMoney().toString())).multiply(new BigDecimal(100)).intValue()));//订单总金额，单位为分
		params.put("spbill_create_ip", IpUtils.longToIp(paymentOrder.getIp()));
		params.put("notify_url", platform.getBehindUrl());
		params.put("trade_type", tradeType);//二维码链接需要配置为Native,取值如下：JSAPI，NATIVE，APP，WAP,
		
		//params.put("openid", String.valueOf(inParams.get("openid")));//接口参数传入
		//openid 用户标识    trade_type=JSAPI时，此参数必传，用户在商户appid下的唯一标识。
		//下单前需要调用【网页授权获取用户信息】接口获取到用户的Openid。
		//企业号请使用【企业号OAuth2.0接口】获取企业号内成员userid，再调用【企业号userid转openid接口】进行转换。
		
		Map<String,Object> signMap = new HashMap<String, Object>();
		signMap.put("source", genSignStr(params));
		signMap.put("key", platform.getPayKey());
		
		params.put("sign", this.encode(signMap));
		
		String postContent = mapToXml(params);
		
		String response = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;
		String prePayUrl = null;
		try {
			httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
			httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
			
			prePayUrl = platform.getPayUrl();
			
			httppost = new HttpPost(prePayUrl);
			logger.info("PostData:"+postContent);
			StringEntity entity = new StringEntity(postContent, _charset_encode);
			httppost.setEntity(entity);
			
			supportHttps(httpclient);
			HttpResponse res = httpclient.execute(httppost);
			HttpEntity ent = res.getEntity();
			response = EntityUtils.toString(ent , "UTF-8").trim();
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("微信扫码支付预支付接口出错，请与客服联系", e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("微信扫码支付预支付接口出错，请与客服联系", e);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("微信扫码支付接口出错，请与客服联系", e);
		}  finally {
			logger.info("微信扫码支付预支付 url:{},返回:{}",prePayUrl,response);
			abortConnection(httppost, httpclient);
		}
		
		Map<String, Object> retMap = XmlConvertUtil.XmlNodesToMap(response, "//xml/*");
		
		Map<String,Object> retSignMap = new HashMap<String, Object>();
		retSignMap.put("key", platform.getPayKey());
		retSignMap.put("source", genSignStr(retMap));
		
		String localSign = this.encode(retSignMap);
		String returnCode = ObjectUtils.toString(retMap.get("return_code"));
		String resultCode = ObjectUtils.toString(retMap.get("result_code"));
		
		Map<String,Object> apiParams = new TreeMap<String,Object>();
		if("SUCCESS".equalsIgnoreCase(returnCode)){
			//验证签名
			String sign = String.valueOf(retMap.get("sign"));
			if(!localSign.equalsIgnoreCase(sign)){
				logger.error("orderNo:"+paymentOrder.getOrderNo()+",localSign:"+localSign+",sign:"+sign);
				apiParams.put("code", "fail");
				apiParams.put("msg", "获取prepay_id时签名失败");
			} else {
				//获取prepay_id
				if("SUCCESS".equalsIgnoreCase(resultCode)){
					//返回二维码链接
					apiParams.put("code", "success");
					apiParams.put("msg", "获取prepay_id成功");
					apiParams.put("trade_type", retMap.get("trade_type"));
					apiParams.put("prepay_id", retMap.get("prepay_id"));
					apiParams.put("code_url", retMap.get("code_url"));
					
					
					//生成JSAPI签名
//					apiParams.put("appId", appId);
//					apiParams.put("timeStamp", (new Date()).getTime()/1000);
//					apiParams.put("nonceStr", MD5Encrypt.encrypt(String.valueOf(RandomUtils.nextLong())));
//					apiParams.put("package", "prepay_id="+retMap.get("prepay_id"));
//					apiParams.put("signType", "MD5");
//					
//					Map<String,Object> apiSignMap = new HashMap<String, Object>();
//					apiSignMap.put("source", genSignStr(apiParams));
//					apiSignMap.put("key", paymentPlatform.getPassword());
//					
//					apiParams.put("paySign", this.encode(apiSignMap));
//					apiParams.put("code", "success");
//					apiParams.put("msg", "生成签名成功");
					
				} else {
					apiParams.put("code", "fail");
					apiParams.put("msgcode", retMap.get("err_code"));
					apiParams.put("msg", retMap.get("err_code_des"));
				}
			}
		} else {
			apiParams.put("code", "fail");
			apiParams.put("msg", retMap.get("return_msg"));
		}
		
		return apiParams;
		
	}
	
	private String genSignStr(Map<String,Object> treeMap){
		Iterator<String> iter = treeMap.keySet().iterator();
		StringBuffer sb = new StringBuffer();
		while (iter.hasNext()) {
			String name = (String) iter.next();
			String value = String.valueOf(treeMap.get(name));
			
			if("sign".equalsIgnoreCase(name)){
				continue;
			}
			
			if(StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)){
				sb.append(name).append("=").append(value).append("&");
			}
		}
		logger.info(sb.toString());
		return sb.toString();
	}
	
	private String mapToXml(Map<String,Object> treeMap){
		Iterator<String> iter = treeMap.keySet().iterator();
		StringBuffer sb = new StringBuffer("<xml>");
		while (iter.hasNext()) {
			String name = (String) iter.next();
			String value = String.valueOf(treeMap.get(name));
			
			if(StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)){
				sb.append("<").append(name).append(">").append(value).append("</").append(name).append(">");
			}
		}
		sb.append("</xml>");
		return sb.toString();
	}

	
	

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException,
			DataAccessException, PaymentRedirectException {
		
		String payResult = StringUtils.trim(request.getParameter("pay_result"));//支付结果：0—成功；其它—失败
		String oppositeOrderNo = StringUtils.trim(request.getParameter("transaction_id"));//微信公众号订单号
		String orderNo = StringUtils.trim(request.getParameter("sp_billno"));//我方订单号
		String paymentMoney = StringUtils.trim(request.getParameter("total_fee"));//总金额，单位分
		
		//校验加密串
		String sign = StringUtils.trim(request.getParameter("sign"));
		String localSign = generateBackParamSign(request, platform);
		if(!localSign.equalsIgnoreCase(sign)){
			if (logger.isInfoEnabled()) {
				logger.info("==============微信公众号Wap后台加密处理失败=================");
				logger.info("我方加密串：" + localSign);
				logger.info("对方加密串：" + sign);
				logger.info("==============微信公众号Wap后台加密处理结束=================\n");
			}
			throw new ValidationException("微信公众号Wap支付平台加密校验失败");
		}
		
		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "微信公众号Wap支付订单查询为空,orderNo:" + orderNo);
		
		Map<String, Object> returned = new HashMap<String, Object>();
		if ("0".equals(payResult)) { // 支付成功
			logger.info("微信公众号Wap返回支付成功,orderNo:"+orderNo);
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else { // 未支付 - 只有2个状态 0 和 非0
			logger.info("微信公众号Wap返回未支付,orderNo:"+orderNo+",pay_info:" + request.getParameter("pay_info"));
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}
		
		// 设置充值类型 - 不传则默认1-网银支付
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);
		returned.put(PaymentConstant.OPPOSITE_MONEY, paymentMoney);
		return returned;
	}
	
	private String generateBackParamSign(HttpServletRequest request,Platform platform){
		LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
		Map<String, Object> requestParams = request.getParameterMap();
		for (Iterator<Entry<String, Object>> keyValuePairs = requestParams.entrySet().iterator(); keyValuePairs
				.hasNext();) {
			Map.Entry<String, Object> entry = keyValuePairs.next();
			if(!"sign".equalsIgnoreCase(entry.getKey())){
				params.put(entry.getKey(),request.getParameter(entry.getKey()));
			}
		}
//		params.put("ver", StringUtils.trim(request.getParameter("ver")));
//		params.put("charset", StringUtils.trim(request.getParameter("charset")));
//		params.put("pay_result", StringUtils.trim(request.getParameter("pay_result")));
//		params.put("pay_info", StringUtils.trim(request.getParameter("pay_info")));
//		params.put("transaction_id", StringUtils.trim(request.getParameter("transaction_id")));
//		params.put("sp_billno", StringUtils.trim(request.getParameter("sp_billno")));
//		params.put("total_fee", StringUtils.trim(request.getParameter("total_fee")));
//		params.put("fee_type", StringUtils.trim(request.getParameter("total_fee")));
//		params.put("attach", StringUtils.trim(request.getParameter("attach")));
//		params.put("bank_type", StringUtils.trim(request.getParameter("bank_type")));
//		params.put("bank_billno", StringUtils.trim(request.getParameter("bank_billno")));
//		params.put("time_end", StringUtils.trim(request.getParameter("time_end")));
//		params.put("purchase_alias", StringUtils.trim(request.getParameter("purchase_alias")));
		//params.put("key", paymentPlatform.getAuthKey());
		
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
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		// 1.组装查询接口参数
		String attach = "";
		try {
			attach = EncryptFactory.getInstance(Md5.NAME).encrypt(paymentOrder.getOrderNo(), platform.getPayKey(),"");
		} catch (Exception e1) {
			logger.error("Md5密钥签名异常,"+e1.getMessage(),e1);
		}
//		String attach = super.encode(paymentOrder.getOrderNo(), paymentPlatform.getAuthKey());
		String bargainorId = platform.getMerchantNo();
		String spBillno = paymentOrder.getOrderNo();
		
		LinkedHashMap<String, Object> signParams = new LinkedHashMap<String, Object>();
		signParams.put("ver", "2.0");
		signParams.put("bargainor_id", bargainorId); // 商户ID
		signParams.put("sp_billno", spBillno);
		signParams.put("attach", attach);
		signParams.put("charset", _charset_encode);

		LinkedHashMap<String, Object> sortSignMap = AlipayHelper.sortMap(signParams);
		sortSignMap.put("key", platform.getPayKey());

		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", EncryptStringUtils.linkedHashMapToStringWithKey(sortSignMap, true));
		String sign = this.encode(encodeParams);
		
		// 2.向微信公众号查询订单信息
		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"utf-8");
		httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("ver", "2.0"));
		formparams.add(new BasicNameValuePair("bargainor_id", bargainorId));
		formparams.add(new BasicNameValuePair("sp_billno", spBillno));
		formparams.add(new BasicNameValuePair("attach", attach));
		formparams.add(new BasicNameValuePair("charset", _charset_encode));
		formparams.add(new BasicNameValuePair("sign", sign));
		
		String response = null;
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(platform.getPayCheckUrl());
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, _charset_encode);
			httpPost.setEntity(entity);
			
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			response = httpclient.execute(httpPost,responseHandler);
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("微信公众号wap订单查询接口出错，请与客服联系", e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("微信公众号wap订单查询接口出错，请与客服联系", e);
		}  finally {
			logger.info("微信公众号wap手机 订单查询接口 url:{}",platform.getPayCheckUrl());
			logger.info("微信公众号wap手机 订单查询接口返回:{}",response);
			abortConnection(httpPost, httpclient);
		}
		
		// 3.解析，校验及分析返回数据
		String retVer = readXmlNode(response,"//root/ver");
		String retCharset = readXmlNode(response,"//root/charset");
		String retPayResult = readXmlNode(response,"//root/pay_result");
		String retPayInfo = readXmlNode(response,"//root/pay_info");
		String retTransactionId = readXmlNode(response,"//root/transaction_id");
		String retSpBillno = readXmlNode(response,"//root/sp_billno");
		String retTotalFee = readXmlNode(response,"//root/total_fee");
		String retFeeType = readXmlNode(response,"//root/fee_type");
		String retBargainorId = readXmlNode(response,"//root/bargainor_id");
		String retAttach = readXmlNode(response,"//root/attach");
		String retBankType = readXmlNode(response,"//root/bank_type");
		String retBankBillno = readXmlNode(response,"//root/bank_billno");
		String retTimeEnd = readXmlNode(response,"//root/time_end");
		String retPurchaseAlias = readXmlNode(response,"//root/purchase_alias");
		String retSign = readXmlNode(response,"//root/sign");
		
		Map<String, Object> outParams = new HashMap<String, Object>();
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		if (!"0".equals(retPayResult)) { // 支付不成功
			payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
			logger.info("微信公众号wap返回未成功支付，pay_info:" + retPayInfo + ",retmsg:" + response);
			
			// 设置充值类型 - 不传则默认1-网银支付
			outParams.put(PaymentConstant.OPPOSITE_ORDERNO, retTransactionId); // 对方订单号
			outParams.put(PaymentConstant.PAYMENT_STATE, payState);
			outParams.put(PaymentConstant.OPPOSITE_MONEY, retTotalFee); // 总金额，对方传回的单位已经是分
			return outParams;
		}
		
		LinkedHashMap<String, Object> returnLinkedParams = new LinkedHashMap<String, Object>();
		returnLinkedParams.put("ver", retVer);
		returnLinkedParams.put("charset", retCharset);
		returnLinkedParams.put("pay_result", retPayResult);
		returnLinkedParams.put("pay_info", retPayInfo);
		returnLinkedParams.put("transaction_id", retTransactionId);
		returnLinkedParams.put("sp_billno", retSpBillno);
		returnLinkedParams.put("total_fee", retTotalFee);
		returnLinkedParams.put("fee_type", retFeeType);
		returnLinkedParams.put("bargainor_id", retBargainorId);
		returnLinkedParams.put("attach", retAttach);
		returnLinkedParams.put("bank_type", retBankType);
		returnLinkedParams.put("bank_billno", retBankBillno);
		returnLinkedParams.put("time_end", retTimeEnd);
		returnLinkedParams.put("purchase_alias", retPurchaseAlias);
		
		LinkedHashMap<String, Object> retSortSignMap = AlipayHelper.sortMap(returnLinkedParams);
		retSortSignMap.put("key", platform.getPayKey());

		Map<String, Object> retSortSignStrMap = new HashMap<String, Object>();
		retSortSignStrMap.put("source", EncryptStringUtils.linkedHashMapToStringWithKey(retSortSignMap, true));
		String localRetSign = this.encode(retSortSignStrMap);
		
		// 对方校验串校验 md5(参数排序 + key)
		if (!localRetSign.equalsIgnoreCase(retSign)) {
			if (logger.isInfoEnabled()) {
				logger.info("==============微信公众号wap订单校验返回加密处理失败=================");
				logger.info("我方加密串：" + localRetSign);
				logger.info("对方加密串：" + retSign);
				logger.info("==============微信公众号wap订单校验返回加密处理结束=================\n");
			}
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return outParams;
		}
		
		// 我方校验串校验 md5(orderNo + key)
		String localAttach = "";
		try {
			localAttach = EncryptFactory.getInstance(Md5.NAME).encrypt(retSpBillno, platform.getPayKey(), "");
		} catch (Exception e) {
			logger.error("Md5密钥签名异常,"+e.getMessage(),e);
		}
//		String localAttach = super.encode(retSpBillno, paymentPlatform.getAuthKey());
		if (!localAttach.equals(attach)) {
			if (logger.isInfoEnabled()) {
				logger.info("==============微信公众号wap订单orderNo + key校验失败=================");
				logger.info("我方原文：" + retSpBillno + platform.getPayKey());
				logger.info("我方扩展信息：" + localAttach);
				logger.info("对方传回扩展信息：" + attach);
				logger.info("==============微信公众号wap订单orderNo + key校验结束=================\n");
			}
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return outParams;
		}
		
		payState = PaymentConstant.PAYMENT_STATE_PAYED;
		// 设置充值类型 - 不传则默认1-网银支付
		outParams.put(PaymentConstant.OPPOSITE_ORDERNO, retTransactionId); // 对方订单号
		outParams.put(PaymentConstant.PAYMENT_STATE, payState);
		outParams.put(PaymentConstant.OPPOSITE_MONEY, retTotalFee); // 总金额，对方传回的单位已经是分
		return outParams;
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("sp_billno");
	}
	
	/**
	 * 支持Https
	 * 
	 * @param httpClient
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	private void supportHttps(HttpClient httpClient) throws NoSuchAlgorithmException, KeyManagementException{
		// First create a trust manager that won't care.
		X509TrustManager trustManager = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
		// Now put the trust manager into an SSLContext.
		SSLContext sslcontext = SSLContext.getInstance("SSL");
		sslcontext.init(null, new TrustManager[] { trustManager }, null);

		// Use the above SSLContext to create your socket factory
		// (I found trying to extend the factory a bit difficult due to a
		// call to createSocket with no arguments, a method which doesn't
		// exist anywhere I can find, but hey-ho).
		SSLSocketFactory sf = new SSLSocketFactory(sslcontext);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		
		httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", sf, 443));
	}
	
	/**
	 * 释放HttpClient连接
	 * 
	 * @param hrb
	 *            请求对象
	 * @param httpclient
	 * 			  client对象
	 */
	private static void abortConnection(final HttpRequestBase hrb, final HttpClient httpclient){
		if (hrb != null) {
			hrb.abort();
		}
		if (httpclient != null) {
			httpclient.getConnectionManager().shutdown();
		}
	}
	
	private String readXmlNode(String resData,String nodePath) {
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(resData);
		} catch (DocumentException e) {
			throw new ValidationException("微信公众号初始化接口返回xml转换异常");
		}
		Node _requestToken = doc.selectSingleNode(nodePath);
		String requestToken = _requestToken.getText();
		return requestToken;
	}

}
