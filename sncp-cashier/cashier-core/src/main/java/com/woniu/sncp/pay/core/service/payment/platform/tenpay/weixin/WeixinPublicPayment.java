package com.woniu.sncp.pay.core.service.payment.platform.tenpay.weixin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.net.NetServiceException;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.xml.XmlConvertUtil;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.tools.IpUtils;

/**
 * 
 * <p>descrption: 
 * 微信公众号支付平台 
 * 
 * 总金额：以分为单位
 * 特殊：md5加密，有大写有小写，故比对时不区分大小写
 * 支付url：由初始化请求接口url和Wap支付接口通过“|”拼接
 * 生成订单和后台校验都是顺序排列，订单校验是参数按照a-z升序排列
 * </p>
 * 
 * @author fuzl
 * @date   2016年9月2日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@Service("weixinPublicPayment")
public class WeixinPublicPayment extends WeixinPayment {
	
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
		
//		String mchIdAndAppId = platform.getMerchantNo();
//		String mchId = mchIdAndAppId.split("-")[0];//商户号
//		String appId = mchIdAndAppId.split("-")[1];//公众账号ID
//		String tradeType = mchIdAndAppId.split("-")[2];//交易类型
		String ext = ObjectUtils.toString(platform.getExtend());
		String mchId = "";
		String appId = "";
		String tradeType = "";
		if(StringUtils.isNotBlank(ext)){
			JSONObject extend = JSONObject.parseObject(ext);
			mchId = extend.getString("mchId");//商户号
			appId = extend.getString("appId");//公众账号ID
			tradeType = extend.getString("tradeType");//交易类型
		}
		
		
		
		String extend = ObjectUtils.toString(inParams.get("ext"));
		String openid = "";
		if(StringUtils.isNotBlank(extend)){
			JSONObject extJson = JSONObject.parseObject(extend);
			openid = extJson.getString("openid");
		}
		
		Map<String,Object> params = new TreeMap<String,Object>();
		params.put("appid", appId);
		params.put("mch_id", mchId);
		params.put("device_info", "WEB");//PC网页或公众号内支付请传"WEB"
		params.put("nonce_str", MD5Encrypt.encrypt(String.valueOf(RandomUtils.nextLong())));
		params.put("body", StringUtils.trim((String) inParams.get("productName")));//产品名称
		params.put("out_trade_no", paymentOrder.getOrderNo());
		params.put("total_fee", String.valueOf((new BigDecimal(paymentOrder.getMoney().toString())).multiply(new BigDecimal(100)).intValue()));//订单总金额，单位为分
		params.put("spbill_create_ip", IpUtils.longToIp(paymentOrder.getIp()));
		params.put("notify_url", platform.getBehindUrl(paymentOrder.getMerchantId()));
		params.put("trade_type", tradeType);//二维码链接需要配置为Native,取值如下：JSAPI，NATIVE，APP，WAP,
		
		params.put("openid", openid);//接口参数传入
		//openid 用户标识    trade_type=JSAPI时，此参数必传，用户在商户appid下的唯一标识。
		//下单前需要调用【网页授权获取用户信息】接口获取到用户的Openid。
		//企业号请使用【企业号OAuth2.0接口】获取企业号内成员userid，再调用【企业号userid转openid接口】进行转换。
		
		Map<String,Object> signMap = new HashMap<String, Object>();
		signMap.put("source", genSignStr(params)+"key="+platform.getPayKey());
		
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
		
		retSignMap.put("source", genSignStr(retMap)+"key="+platform.getPayKey());
		
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
					
					if(StringUtils.isNotBlank(tradeType)){
						if(tradeType.equals("JSAPI")){
							//生成JSAPI签名
							apiParams.put("appId", appId);
							apiParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
							apiParams.put("nonceStr", MD5Encrypt.encrypt(String.valueOf(RandomUtils.nextLong())));
							apiParams.put("package", "prepay_id="+retMap.get("prepay_id"));
							apiParams.put("signType", "MD5");

							Map<String,Object> apiSignMap = new HashMap<String, Object>();
							apiSignMap.put("source", genSignStr(apiParams)+"key="+platform.getPayKey());
							
							apiParams.put("paySign", this.encode(apiSignMap));
							apiParams.put("code", "success");
							apiParams.put("msg", "生成签名成功");
						}
						if(tradeType.equals("APP")){
							
							apiParams.put("appid", appId);
							apiParams.put("partnerid", mchId);
							apiParams.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
							apiParams.put("noncestr", String.valueOf(System.nanoTime()));
							apiParams.put("prepay_id", retMap.get("prepay_id"));
							apiParams.put("package", "Sign=WXPay");
							apiParams.put("signType", "MD5");
							Map<String,Object> apiSignMap = new HashMap<String, Object>();
							apiSignMap.put("source", genSignStr(apiParams)+"key="+platform.getPayKey());
							apiParams.put("sign", this.encode(apiSignMap));
							apiParams.put("code", "success");
							apiParams.put("msg", "获取prepay_id成功");
						}
					}
					
					//返回二维码链接
//					apiParams.put("code", "success");
//					apiParams.put("msg", "获取prepay_id成功");
//					apiParams.put("trade_type", retMap.get("trade_type"));
//					apiParams.put("prepay_id", retMap.get("prepay_id"));
//					apiParams.put("code_url", retMap.get("code_url"));
					
					
					//生成JSAPI签名
//					apiParams.put("appId", appId);
//					apiParams.put("timeStamp", (new Date()).getTime()/1000);
//					apiParams.put("nonceStr", MD5Encrypt.encrypt(String.valueOf(RandomUtils.nextLong())));
//					apiParams.put("package", "prepay_id="+retMap.get("prepay_id"));
//					apiParams.put("signType", "MD5");
//
//					Map<String,Object> apiSignMap = new HashMap<String, Object>();
//					apiSignMap.put("source", genSignStr(apiParams)+"key="+platform.getPayKey());
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
	
	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException,
			DataAccessException, PaymentRedirectException {
		StringBuilder sb = new StringBuilder();  
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader((ServletInputStream)request.getInputStream()));  
			String line = null;  
			while((line = br.readLine())!=null){  
			    sb.append(line);  
			}
		} catch (IOException e) {
			
		}  
		Map<String, Object> retMap = XmlConvertUtil.XmlNodesToMap(sb.toString(), "//xml/*");
		
		String returnCode = ObjectUtils.toString(retMap.get("return_code"));
		if(!"SUCCESS".equalsIgnoreCase(returnCode)) {
			throw new ValidationException(ObjectUtils.toString(retMap.get("return_msg")));
		}
		
		
		
		String payResult = ObjectUtils.toString(retMap.get("result_code"));//支付结果：SUCCESS—成功；其它—失败
		String oppositeOrderNo = ObjectUtils.toString(retMap.get("transaction_id"));//微信公众号订单号
		String orderNo = ObjectUtils.toString(retMap.get("out_trade_no"));//我方订单号
		String paymentMoney = ObjectUtils.toString(retMap.get("total_fee"));//总金额，单位分
		
		//校验加密串
		Map<String,Object> signMap = new HashMap<String,Object>();
		signMap.put("source", genSignStr(retMap)+"key="+platform.getPayKey());
		String localSign = this.encode(signMap);
		
		String sign = ObjectUtils.toString(retMap.get("sign"));
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
		if ("SUCCESS".equals(payResult)) { // 支付成功
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
		String mchIdAndAppId = platform.getMerchantNo();
		String mchId = mchIdAndAppId.split("-")[0];//商户号
		String appId = mchIdAndAppId.split("-")[1];//公众账号ID
		
		
		Map<String, Object> params = new TreeMap<String, Object>();
		params.put("appid", appId);
		params.put("mch_id", mchId); // 商户ID
		params.put("out_trade_no", paymentOrder.getOrderNo());//我方订单号
		params.put("nonce_str", MD5Encrypt.encrypt(String.valueOf(RandomUtils.nextLong())));

		// 2.签名
		Map<String, Object> encodeMap = new HashMap<String, Object>();
		encodeMap.put("source", genSignStr(params)+"key="+platform.getPayKey());
		String sign = this.encode(encodeMap);
		
		params.put("sign", sign);
		// 3.向微信公众号查询订单信息
		String postContent = mapToXml(params);
		
		String response = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;
		String prePayUrl = null;
		try {
			httpclient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"utf-8");
			httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
			httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
			
			prePayUrl = platform.getPayCheckUrl();
			
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
			throw new NetServiceException("微信公众号支付预支付接口出错，请与客服联系", e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("微信公众号支付预支付接口出错，请与客服联系", e);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("微信公众号支付接口出错，请与客服联系", e);
		}  finally {
			logger.info("微信公众号支付预支付 url:{},返回:{}",prePayUrl,response);
			abortConnection(httppost, httpclient);
		}
		
		
		// 4.解析，校验及分析返回数据
		Map<String, Object> retMap = XmlConvertUtil.XmlNodesToMap(response, "//xml/*");
		
		String returnCode = ObjectUtils.toString(retMap.get("return_code"));
		String resultCode = ObjectUtils.toString(retMap.get("result_code"));
		String retSign = ObjectUtils.toString(retMap.get("sign"));
		String transactionId = ObjectUtils.toString(retMap.get("transaction_id"));
		String totalFee = ObjectUtils.toString(retMap.get("total_fee"));
		String tradeState = ObjectUtils.toString(retMap.get("trade_state"));
		
		Map<String, Object> outParams = new HashMap<String, Object>();
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		
		if("SUCCESS".equalsIgnoreCase(returnCode)){
			Map<String,Object> retSignMap = new HashMap<String, Object>();
			retSignMap.put("source", genSignStr(retMap)+"key="+platform.getPayKey());
			String localSign = this.encode(retSignMap);
			
			// 对方校验串校验 md5(参数排序 + key)
			if (!localSign.equalsIgnoreCase(retSign)) {
				if (logger.isInfoEnabled()) {
					logger.info("==============微信公众号wap订单校验返回加密处理失败=================");
					logger.info("我方加密串：" + localSign);
					logger.info("对方加密串：" + retSign);
					logger.info("==============微信公众号wap订单校验返回加密处理结束=================\n");
				}
				outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
				return outParams;
			}else{
				if (!"SUCCESS".equals(resultCode) || !"SUCCESS".equals(tradeState)) { // 支付不成功
					payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
					logger.info("微信公众号wap返回未成功支付，pay_info:" + params + ",retmsg:" + response);
					// 设置充值类型 - 不传则默认1-网银支付
					outParams.put(PaymentConstant.PAYMENT_STATE, payState);
					return outParams;
				}
			}
		}
		
		payState = PaymentConstant.PAYMENT_STATE_PAYED;
		// 设置充值类型 - 不传则默认1-网银支付
		outParams.put(PaymentConstant.OPPOSITE_ORDERNO, transactionId); // 对方订单号
		outParams.put(PaymentConstant.PAYMENT_STATE, payState);
		outParams.put(PaymentConstant.OPPOSITE_MONEY, totalFee); // 总金额，对方传回的单位已经是分
		return outParams;
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("sp_billno");
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
	
	private String genSignStr(Map<String,Object> treeMap){
		Iterator<String> iter = treeMap.keySet().iterator();
		StringBuffer sb = new StringBuffer();
		while (iter.hasNext()) {
			String name = iter.next();
			String value = String.valueOf(treeMap.get(name));
			
			if("sign".equalsIgnoreCase(name)){
				continue;
			}
			
			if(StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)){
				sb.append(name).append("=").append(value).append("&");
			}
		}
		String result = sb.toString();
		
		logger.info(result);
		return result;
	}
	
	
	
	private String mapToXml(Map<String,Object> treeMap){
		Iterator<String> iter = treeMap.keySet().iterator();
		StringBuffer sb = new StringBuffer("<xml>");
		while (iter.hasNext()) {
			String name = iter.next();
			String value = String.valueOf(treeMap.get(name));
			
			if(StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)){
				sb.append("<").append(name).append(">").append(value).append("</").append(name).append(">");
			}
		}
		sb.append("</xml>");
		return sb.toString();
	}
}
