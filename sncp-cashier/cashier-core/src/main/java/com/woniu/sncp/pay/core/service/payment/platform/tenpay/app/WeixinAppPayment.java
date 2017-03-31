package com.woniu.sncp.pay.core.service.payment.platform.tenpay.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
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
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.net.NetServiceException;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptFactory;
import com.woniu.sncp.pay.common.utils.encrypt.Sha1;
import com.woniu.sncp.pay.core.service.MemcachedService;
import com.woniu.sncp.pay.core.service.payment.platform.tenpay.TenpayPayment;
import com.woniu.sncp.pay.core.service.payment.platform.tenpay.tools.ClientResponseHandler;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.web.IpUtils;

/**
 * 微信APP支付
 * 
 * @author luzz
 *
 */
@Service("weixinAppPayment")
public class WeixinAppPayment extends TenpayPayment {
	
	@Resource
	private MemcachedService memcachedService;
	
	@Resource
	private PaymentConstant paymentConstant;
	
	/**
	 * 微信APP支付编码
	 */
	private final String _charset_encode = HTTP.UTF_8;

	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		String source = (String) inParams.get("source");
		String encrypted = StringUtils.upperCase(MD5Encrypt.encrypt(source,_charset_encode));
		if (logger.isInfoEnabled()) {
			logger.info("=========微信APP支付加密开始=========");
			logger.info("source：" + source);
			logger.info("encrypted：" + encrypted);
			logger.info("=========微信APP支付加密结束=========\n");
		}
		return encrypted;
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		if(StringUtils.isEmpty(platform.getPayUrl()) || platform.getPayUrl().split("\\|").length != 2 ){
			throw new ValidationException("微信APP支付Url配置不正确");
		}
		
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(platform.getPrivateUrl()));
		} catch (FileNotFoundException e) {
			logger.error("微信APP支付预支付接口出错,密钥文件未找到",e);
			throw new NetServiceException("微信APP支付预支付接口出错，请与客服联系", e);
		} catch (IOException e) {
			logger.error("微信APP支付预支付接口出错,密钥文件读取异常",e);
			throw new NetServiceException("微信APP支付预支付接口出错，请与客服联系", e);
		}
		
//		String appId = StringUtils.trim(prop.getProperty("appid"));
		String appKey = StringUtils.trim(prop.getProperty("key"));
		String appSecret = StringUtils.trim(prop.getProperty("appsecret"));
		
		String partnerId = platform.getMerchantNo().split("-")[0];
		String appId = platform.getMerchantNo().split("-")[1];
		
		Map<String,Object> retMap = preOrder(inParams,true,appId,appKey,appSecret);
		
		if("40001".equals(String.valueOf(retMap.get("errcode")))){
			retMap = preOrder(inParams,false,appId,appKey,appSecret);
		}
		
		retMap.put("appid", appId);
		retMap.put("partnerid", partnerId);
		orderSign(retMap);
		
		logger.info("retMap:"+retMap);
		return retMap;
	}
	
	/**
	 * 向微信获取access token，有效期为 7200 秒，需要缓存，每天只能获取200次
	 * @param inParams
	 * @return
	 */
	private String getAccessToken(Map<String, Object> inParams, boolean getFromCache,String appId,String secret){
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
//		String appId = paymentPlatform.getManageUser();
//		String secret = paymentPlatform.getManagePwd();
		
		String memKey = "IM_AT_"+appId;
		String accessToken = (String) memcachedService.get(memKey);
		logger.info("accessToken:"+accessToken);
		if(StringUtils.isNotEmpty(accessToken) && accessToken != null && getFromCache){
			return accessToken;
		}
		
		String response = null;
		HttpGet httpGet = null;
		String reqTokenUrl = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		
		try {
			httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
			httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
			
			reqTokenUrl = platform.getPayUrl().split("\\|")[0];
			reqTokenUrl = reqTokenUrl + "?grant_type=client_credential&appid="+appId+"&secret="+secret;
			
			httpGet = new HttpGet(reqTokenUrl);
			
			supportHttps(httpclient);
			HttpResponse res = httpclient.execute(httpGet);
			HttpEntity ent = res.getEntity();
			response = EntityUtils.toString(ent , "UTF-8").trim();
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("微信APP支付接口出错，请与客服联系", e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("微信APP支付接口出错，请与客服联系", e);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("微信APP支付接口出错，请与客服联系", e);
		}  finally {
			logger.info("微信APP支付获取token url:{},返回:{}",reqTokenUrl,response);
			abortConnection(httpGet, httpclient);
		}
		Map<String, Object> retMap = JsonUtils.jsonToMap(response);
		accessToken = String.valueOf(retMap.get("access_token"));
		memcachedService.set(memKey,7000,accessToken);
		
		return accessToken;
	}
	
	private Date getAfterSeconds(int second){
    	Calendar calendar = Calendar.getInstance();
    	calendar.add(Calendar.SECOND, second);
    	return calendar.getTime();
    }
	
	private String getSignData(Map<String, String> signParams){
		StringBuffer sb = new StringBuffer();
		Set es = signParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			sb.append(k + "=" + v + "&");
			// 要采用URLENCODER的原始值！
		}
		String params = sb.substring(0, sb.lastIndexOf("&"));
		logger.info("sha1 原串:" + params);
		
		return params;
	}
	
	/**
	 * 向微信请求预支付信息
	 * 
	 * 成功示例:
	 * {"prepayid":"PREPAY_ID","errcode":0,"errmsg":"Success"}
	 * 
	 * 错误示例：
	 * {"errcode":48001,"errmsg":"api unauthorized"}
	 * 
	 * @param inParams
	 * @return
	 */
	private Map<String,Object> preOrder(Map<String, Object> inParams,boolean getTokenFromCache,String appId,String appKey,String appSecret){
		// 1.拼装参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		String accessToken = getAccessToken(inParams,getTokenFromCache,appId,appSecret);
		String key = platform.getPayKey();
		
		String merchantNo = platform.getMerchantNo().split("-")[0];

		//设置package订单参数
		Map<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("bank_type", "WX"); //支付类型，微信   
		packageParams.put("body", (String)inParams.get("productName")); //商品描述   
		packageParams.put("notify_url", platform.getBehindUrl(paymentOrder.getMerchantId())); //接收财付通通知的URL  
		packageParams.put("partner", merchantNo); //商户号    
		packageParams.put("out_trade_no", paymentOrder.getOrderNo()); //商家订单号  
//		packageParams.put("total_fee", String.valueOf((int)(paymentOrder.getMoney()*100))); //商品金额,以分为单位  
		packageParams.put("total_fee", String.valueOf((new BigDecimal(paymentOrder.getMoney().toString())).multiply(new BigDecimal(100)).intValue())); //商品金额,以分为单位  
		packageParams.put("spbill_create_ip", IpUtils.longToIp(paymentOrder.getIp())); //订单生成的机器IP，指用户浏览器端IP  
		packageParams.put("fee_type", "1"); //币种，1人民币   66
		packageParams.put("input_charset", this._charset_encode); //字符编码
		
		Map<String, String> params = new TreeMap<String, String>();
		params.put("appid", appId);
		params.put("appkey", appKey);
		params.put("noncestr", String.valueOf(System.nanoTime()));
		params.put("package", genPackage(packageParams, key));
		params.put("traceid",String.valueOf(paymentOrder.getAid()));
		params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
		
		try {
			params.put("app_signature", EncryptFactory.getInstance(Sha1.NAME).sign(getSignData(params), "", ""));
		} catch (Exception e1) {
		}
		params.remove("appkey");
		params.put("sign_method", "sha1");

		// 3.请求获取prepayid
		String response = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;
		String prePayUrl = null;
		try {
			//httpclient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,_charset_encode);
			httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
			httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
			
			prePayUrl = platform.getPayUrl().split("\\|")[1]+"?access_token="+accessToken;
			
			httppost = new HttpPost(prePayUrl);
			logger.info("PostData:"+JsonUtils.toJson(params));
			StringEntity entity = new StringEntity(JsonUtils.toJson(params), _charset_encode);
			httppost.setEntity(entity);
			
			supportHttps(httpclient);
			HttpResponse res = httpclient.execute(httppost);
			HttpEntity ent = res.getEntity();
			response = EntityUtils.toString(ent , "UTF-8").trim();
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("微信APP支付预支付接口出错，请与客服联系", e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("微信APP支付预支付接口出错，请与客服联系", e);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("微信APP支付接口出错，请与客服联系", e);
		}  finally {
			logger.info("微信APP支付预支付 url:{},返回:{}",prePayUrl,response);
			abortConnection(httppost, httpclient);
		}

		Map<String,Object> result = JsonUtils.jsonToMap(response);
		result.put("noncestr", String.valueOf(System.nanoTime()));
		result.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
		result.put("appkey", appKey);
//		result.put("timestamp", params.get("timestamp"));
//		result.put("noncestr", params.get("noncestr"));
		
		return result;
	}
	
	private void orderSign(Map<String,Object> inParams){
		Map<String, String> params = new TreeMap<String, String>();
		for (Entry<String, Object> entry : inParams.entrySet()) {
			String k = entry.getKey();
			String v = String.valueOf(entry.getValue());
			
			if("errcode".equals(k) || "errmsg".equals(k)){
				continue;
			}
			
			params.put(k, v);
		}
		
		params.put("package", "Sign=WXPay");
		try {
			inParams.put("sign", EncryptFactory.getInstance(Sha1.NAME).sign(getSignData(params), "", ""));
		} catch (Exception e1) {
		}
		inParams.remove("appkey");
	}
	
	private String genPackage(Map<String, String> packageParams,String key){
		String sign = createSign(packageParams,key);

		StringBuffer sb = new StringBuffer();
		for (Entry<String, String> entry : packageParams.entrySet()) {
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			try {
				sb.append(k + "=" + URLEncoder.encode(v,this._charset_encode).replace("+", "%20") + "&");
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage(),e);
			}
		}

		// 去掉最后一个&
		String packageValue = sb.append("sign=" + sign).toString();
		logger.debug("packageValue=" + packageValue);
		return packageValue;
	}
	
	/**
	 * 创建md5摘要,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
	 */
	private String createSign(Map<String, String> packageParams,String key) {
		StringBuffer sb = new StringBuffer();
		
		for (Entry<String, String> entry : packageParams.entrySet()) {
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (null != v && !"".equals(v) && !"sign".equals(k)
					&& !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		
		sb.append("key=" + key);
		logger.debug("md5 sb:" + sb);
		
		Map<String,Object> signMap = new HashMap<String, Object>();
		signMap.put("source", sb.toString());
		String sign = this.encode(signMap).toUpperCase();

		return sign;
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

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException,
			DataAccessException, PaymentRedirectException {

		Map<String,String> sortMap = new TreeMap<String, String>();
		Map<String,String> m = request.getParameterMap();
		for (Entry<String, String> entry : m.entrySet()) {
			sortMap.put(entry.getKey(), request.getParameter(entry.getKey()));
		}
		
		String tradeState = request.getParameter("trade_state");//支付结果：0—成功,其他保留
		String totalFee = request.getParameter("total_fee");//订单金额，单位为分
		String orderNo = request.getParameter("out_trade_no");//我方订单号
		String oppositeOrderNo = StringUtils.trim(request.getParameter("transaction_id"));//财付通订单号
		
		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "微信APP支付订单查询为空,orderNo:" + orderNo);
		
		String partnerkey = platform.getPayKey();
		if(!isTenpaySign(sortMap, partnerkey)){
			throw new ValidationException("微信APP支付平台加密校验失败");
		}
		
		Map<String, Object> returned = new HashMap<String, Object>();
		if ("0".equals(tradeState)) { // 支付成功
			logger.info("微信APP支付返回支付成功,orderNo:"+orderNo);
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else { // 未支付 - 只有2个状态 0 和 非0
			logger.info("微信APP支付返回未支付,orderNo:"+orderNo+",pay_info:" + request.getParameter("pay_info"));
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}
		
		// 设置充值类型 - 不传则默认1-网银支付
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);
		returned.put(PaymentConstant.OPPOSITE_MONEY, totalFee);
		
		return returned;
	}
	
	/**
	 * 财付通签名是否通过,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
	 * 
	 * @return boolean
	 */
	public boolean isTenpaySign(Map<String,String> sortMap,String key) {
		StringBuffer sb = new StringBuffer();
		
		for (Entry<String, String> entry : sortMap.entrySet()) {
			String k = String.valueOf(entry.getKey());
			String v = String.valueOf(sortMap.get(k));
			if (!"sign".equals(k) && null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
			}
		}

		sb.append("key=" + key);

		Map<String,Object> signMap = new HashMap<String, Object>();
		signMap.put("source", sb.toString());
		String sign = this.encode(signMap);

		String tenpaySign = String.valueOf(sortMap.get("sign"));
		boolean isTenpaySign = tenpaySign.equalsIgnoreCase(sign);
		if(!isTenpaySign){
			logger.info("==============微信APP后台加密处理失败=================");
			logger.info("我方加密串：" + sign);
			logger.info("对方加密串：" + tenpaySign);
			logger.info("==============微信APP后台加密处理结束=================\n");
		}
		return isTenpaySign;
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

	//没有页面跳转
	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return null;
	}
	
	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		// 1.加密，是按照 a-z 升序排列
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		String sp_billno = paymentOrder.getOrderNo();
		
		String merchantNo = platform.getMerchantNo().split("-")[0];

		Map<String, String> sortedParams = new TreeMap<String, String>();
		sortedParams.put("sign_type", "MD5");
		sortedParams.put("service_version", "1.0"); // 商户ID
		sortedParams.put("input_charset", _charset_encode);
		sortedParams.put("partner", merchantNo);
		sortedParams.put("out_trade_no", sp_billno);
		
		String encrypted = createSign(sortedParams, platform.getPayKey());

		// 2.向财付通请求参数
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(platform.getPayCheckUrl());

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("sign_type", "MD5"));
		params.add(new BasicNameValuePair("service_version", "1.0"));
		params.add(new BasicNameValuePair("input_charset", _charset_encode));
		params.add(new BasicNameValuePair("partner", merchantNo));
		params.add(new BasicNameValuePair("out_trade_no", sp_billno));
		params.add(new BasicNameValuePair("sign", encrypted));

		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(params, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			logger.error(e1.getMessage(),e1);
		}
		httpPost.setEntity(entity);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		try {
			responseBody = httpclient.execute(httpPost, responseHandler);
		} catch (Exception e) {
			throw new ValidationException("微信APP订单校验返回失败", e);
		}
		httpclient.getConnectionManager().shutdown();

		if (logger.isInfoEnabled())
			logger.info("微信APP订单校验返回：" + responseBody);

		if (StringUtils.isBlank(responseBody)) {
			throw new ValidationException("微信APP订单验证返回responseBody为空");
		}

		//应答对象
	    ClientResponseHandler resHandler = new ClientResponseHandler();
	    try {
			resHandler.setContent(responseBody);
		} catch (Exception e) {
			throw new ValidationException("微信APP订单验证返回解析异常,responseBody:"+responseBody);
		}
	    resHandler.setKey(platform.getPayKey());

		Map<String, Object> outParams = new HashMap<String, Object>();

		// 5.pay_result是有记录的时候存在，即retcode=0才会有的
		//获取返回参数
    	String retcode = resHandler.getParameter("retcode");
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		if ("0".equals(retcode)) { // 支付成功
			// 4.返回信息校验
			if (!resHandler.isTenpaySign()) {
				if (logger.isInfoEnabled()) {
					logger.info("==============微信APP订单校验返回加密处理失败=================");
					logger.info(resHandler.getDebugInfo());
					logger.info("==============微信APP订单校验返回加密处理结束=================\n");
				}
				outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
				return outParams;
			}

			payState = PaymentConstant.PAYMENT_STATE_PAYED;
		} else {
			payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
			logger.info("微信APP返回未成功支付，" + responseBody);
		}

		// 设置充值类型 - 不传则默认1-网银支付
		outParams.put(PaymentConstant.OPPOSITE_ORDERNO, resHandler.getParameter("transaction_id")); // 对方订单号
		outParams.put(PaymentConstant.PAYMENT_STATE, payState);
		outParams.put(PaymentConstant.OPPOSITE_MONEY, resHandler.getParameter("total_fee")); // 总金额，对方传回的单位已经是分
		return outParams;
	}

}
