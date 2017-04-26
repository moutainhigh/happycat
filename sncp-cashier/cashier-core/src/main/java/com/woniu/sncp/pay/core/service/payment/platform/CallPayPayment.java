package com.woniu.sncp.pay.core.service.payment.platform;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.rpc.holders.IntHolder;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.common.utils.RefundmentConstant;
import com.woniu.pay.pojo.PayRequestParams;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.cbss.api.imprest.direct.request.DICallRequest;
import com.woniu.sncp.cbss.api.pay.direct.request.DIOrderPayBackGetOrderNoRequest;
import com.woniu.sncp.cbss.api.pay.direct.request.DIOrderPayBackRequest;
import com.woniu.sncp.cbss.api.pay.direct.request.DIOrderPayBackReturnRequest;
import com.woniu.sncp.cbss.api.pay.direct.request.DIOrderPayGetOrderNoRequest;
import com.woniu.sncp.cbss.api.pay.direct.request.DIOrderPayQueryRequest;
import com.woniu.sncp.cbss.api.pay.direct.request.DIOrderPayRequest;
import com.woniu.sncp.cbss.api.pay.direct.response.DIOrderPayBackGetOrderNoResponse;
import com.woniu.sncp.cbss.api.pay.direct.response.DIOrderPayBackResponse;
import com.woniu.sncp.cbss.api.pay.direct.response.DIOrderPayBackResponseData;
import com.woniu.sncp.cbss.api.pay.direct.response.DIOrderPayBackReturnResponse;
import com.woniu.sncp.cbss.api.pay.direct.response.DIOrderPayGetOrderNoResponse;
import com.woniu.sncp.cbss.api.pay.direct.response.DIOrderPayQueryResponse;
import com.woniu.sncp.cbss.api.pay.direct.response.DIOrderPayResponse;
import com.woniu.sncp.cbss.core.errorcode.EchoInfo;
import com.woniu.sncp.cbss.core.model.constant.NameFactory;
import com.woniu.sncp.cbss.core.model.request.RequestClientInfo;
import com.woniu.sncp.cbss.core.model.request.RequestDatas;
import com.woniu.sncp.cbss.core.model.response.MonitorData;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.http.HttpClient;
import com.woniu.sncp.pay.core.filter.AuthenticationCommonFilter;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.web.IpUtils;

/**
 * 支付渠道业务抽象类，主要职责如下：<br />
 * 1.完成各平台独立的基本方法<br />
 * 2.调用各平台基本方法实现我方公共的框架方法，如：生成订单、完成支付等
 * 3.增加支付远程请求,支付远程回调,支付远程查询,支付远程回调响应
 */
@Service("callPayPayment")
public class CallPayPayment extends AbstractPayment {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${refund.accessId}")
	private String ACCESS_ID;
	
	@Value("${refund.accessType}")
	private String ACCESS_TYPE;
	
	@Value("${refund.accessPassword}")
	private String ACCESS_PWD;
	
	@Value("${refund.key}")
	private String ACCESS_KEY;
	
	@Value("${refund.timeout}")
	private String TIMEOUT;
	
	/**
	 * 循环调用远程服务方法
	 * @param callUrl
	 * @param headers
	 * @param body
	 * @param timeout
	 * @param encode
	 * @param cycle
	 * @param exts
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	private String callRemoteUrl(String callUrl, Map<String, Object> headers, String body, int timeout, String encode, int cycle, Map exts)
			throws Exception {
		String ret = "";
		int j = 0;
		for (int i = 0; i < cycle; i++) {
			String url = "callUrl" + (j == 0 ? "" : j);
			if (!exts.containsKey(url)) {
				break;
			}

			try {
				callUrl = ObjectUtils.toString(exts.get(url));
				String timout = "cutimout" + (j == 0 ? "" : j);
				ret = HttpClient.post(callUrl, headers, URLEncoder.encode(body, "utf-8"),
						exts.containsKey(timout) ? Integer.parseInt(ObjectUtils.toString(exts.get(timout))) : timeout, "utf-8");
				break;
			} catch (Exception e) {
				if (i + 1 >= cycle) {
					i = 0;
					j++;

					url = "callUrl" + (j == 0 ? "" : j);
					if (!exts.containsKey(url)) {
						throw e;
					}

					continue;
				}
				if (e instanceof IOException || e instanceof ClientProtocolException || e instanceof EOFException || e instanceof ConnectException
						|| e instanceof SocketException) {
					logger.error("[" + callUrl + "][" + i + "]", e);
					continue;
				}
			}
		}
		return ret;
	}
	
	
	/**********************************************************/
	/******增加统一付款流程,接入cbss-api方式,渠道对接由cbss-api完成******/
	/***********************************************************/
	/**
	 * 循环调用远程服务方法
	 * @param callUrl
	 * @param headers
	 * @param body
	 * @param timeout
	 * @param encode
	 * @param cycle
	 * @param exts
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	private String callPayRemoteUrl(String callUrl, Map<String, Object> headers, String body, int timeout, String encode, int cycle, Map exts,IntHolder d)
			throws Exception {
		String ret = "";
		int j = 0;
		for (int i = 0; i < cycle; i++) {
			String url = "callPayUrl" + (j == 0 ? "" : j);
			if (!exts.containsKey(url)) {
				break;
			}

			try {
				callUrl = ObjectUtils.toString(exts.get(url));
				String timout = "cutimout" + (j == 0 ? "" : j);
				if(d != null){
					ret = HttpClient.post(callUrl, headers, URLEncoder.encode(body, "utf-8"),
							exts.containsKey(timout) ? Integer.parseInt(ObjectUtils.toString(exts.get(timout))) : timeout, "utf-8",d);
				}else{
					ret = HttpClient.post(callUrl, headers, URLEncoder.encode(body, "utf-8"),
							exts.containsKey(timout) ? Integer.parseInt(ObjectUtils.toString(exts.get(timout))) : timeout, "utf-8");
				}
				
				break;
			} catch (Exception e) {
				if (i + 1 >= cycle) {
					i = 0;
					j++;

					url = "callPayUrl" + (j == 0 ? "" : j);
					if (!exts.containsKey(url)) {
						throw e;
					}

					continue;
				}
				if (e instanceof IOException || e instanceof ClientProtocolException || e instanceof EOFException || e instanceof ConnectException
						|| e instanceof SocketException) {
					logger.error("[" + callUrl + "][" + i + "]", e);
					continue;
				}
			}
		}
		return ret;
	}
	/**
	 * 统一实现第三方支付
	 * 分预支付和直接支付
	 * @param inParams
	 * @return
	 * @throws ValidationException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> callPay(Map<String, Object> inParams) throws ValidationException{
		// 1.拼装支付请求参数
		DIOrderPayRequest orderPayRequest = new DIOrderPayRequest();
		
		// 请求支付响应
		DIOrderPayResponse response = null;
		
		Map<String, Object> outParams = new HashMap<String, Object>();
		PayRequestParams payRequestParams = new PayRequestParams();
		try {
			// 封装请求数据
			PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
			Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
			String productName = ObjectUtils.toString(inParams.get("productName"));
			String defaultbank = ObjectUtils.toString(inParams.get("defaultbank"));
			Map<String, Object> extendParams = (Map<String, Object>) inParams.get("extendParams");
			String clientIp = ObjectUtils.toString(inParams.get(PaymentConstant.CLIENT_IP));
					
			
			String callUrl = "";
			String timeout = "30000";//默认30秒
			String extend = platform.getExtend();//渠道-商户扩展
			Integer cyle = 3;//默认失败重试次数3次
			String payType = "directPay";//默认是直接支付:directPay;预支付prePay
			
			// 请求支付相关配置
			if(StringUtils.isNotEmpty(extend)){
				JSONObject extJson = JSONObject.parseObject(extend);
				if(extJson.containsKey("callPayUrl") && StringUtils.isNotBlank(extJson.getString("callPayUrl"))){
					callUrl = extJson.getString("callPayUrl");
				}else{
					// 未配置,callPayUrl
					logger.error("$_$第三方服务callPay$_$调用出现异常,未正确配置callPayUrl");
					throw new ValidationException("$_$第三方服务callPay$_$调用出现异常,未正确配置callPayUrl");
				}
				if(extJson.containsKey("timeout") && StringUtils.isNotBlank(extJson.getString("timeout"))){
					timeout = extJson.getString("timeout");
				}
				if(extJson.containsKey("cyle") && StringUtils.isNotBlank(extJson.getString("cyle"))){
					cyle = Integer.parseInt(extJson.getString("cyle"));
				}
				if(extJson.containsKey("payType") && StringUtils.isNotBlank(extJson.getString("payType"))){
					payType = extJson.getString("payType");
				}
			}
			
			payRequestParams.setClientIp(clientIp);
			payRequestParams.setDefaultbank(defaultbank);
			payRequestParams.setExtendParams(extendParams);
			payRequestParams.setPaymentOrder(paymentOrder);
			payRequestParams.setPlatform(platform);
			payRequestParams.setProductName(productName);
			
			orderPayRequest.setPayRequestParams(payRequestParams);//设置请求参数
			
			DICallRequest paramData = new DICallRequest();
			paramData.setMethod("OrderPayRequest");//付款
			paramData.setOrderPayRequest(orderPayRequest);

			RequestDatas<DICallRequest> request = new RequestDatas<DICallRequest>();

			request.setAccessId(Long.parseLong(ACCESS_ID));
			request.setAccessPasswd(ACCESS_PWD);
			request.setAccessType(Long.parseLong(ACCESS_TYPE));
			request.setParamdata(paramData);
			request.setVersion("1");
			
			RequestClientInfo requestClientInfo = new RequestClientInfo();
			requestClientInfo.setClientUserIp(ObjectUtils.toString(inParams.get(PaymentConstant.CLIENT_IP)));
			requestClientInfo.setLocalReqIp(IpUtils.getLoaclAddr());
			requestClientInfo.setStartReqTime(new Date().getTime());
			request.setClientInfo(requestClientInfo);
			
			String body = JSON.toJSONString(request);
			String vString = body + request.getAccessId() + request.getAccessType() + request.getAccessPasswd()
					+ ACCESS_KEY;
			String verfiy = MD5Encrypt.encrypt(vString, "utf-8");

			Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("Content-Type", "application/json");
			headers.put("bodyEncode", "urlencode=utf-8");
			headers.put(NameFactory.request_head.accessId.name(), request.getAccessId());
			headers.put(NameFactory.request_head.accessType.name(), request.getAccessType());
			headers.put(NameFactory.request_head.accessPasswd.name(), request.getAccessPasswd());
			headers.put(NameFactory.request_head.accessVerify.name(), verfiy);
			
			// 发起调用
			logger.info(this.getClass().getSimpleName()+"header{},body:{}",new Object[]{headers,body});
			String ret = callPayRemoteUrl(callUrl,headers, body, Integer.parseInt(timeout),"utf-8", cyle, JSONObject.parseObject(platform.getExtend()),null);
			
			EchoInfo<DIOrderPayResponse> echoInfo = JSON.parseObject(ret, new TypeReference<EchoInfo<DIOrderPayResponse>>() {});
			
			// 支付响应
			response = echoInfo.getData();
			
			if(null!=response){
				/**
				 * 判断是什么支付类型
				 */
				if(payType.equals(PaymentConstant.PAYMENT_TYPE_DIRECT_PAY)){
					// 直接支付
					// 有支付请求响应,支付请求成功,处理中或初始化
					if( null != response.getOrderPayResponseData() && 
							PaymentConstant.PAYMENT_STATE_PAYED.equals(response.getOrderPayResponseData().getStatusCode() )){
						// 直接支付参数
						logger.info(this.getClass().getSimpleName()+" 直接支付参数:{} ",response.getOrderPayResponseData().getPrePayParams());
						outParams = ErrorCode.getErrorCode(1);
						outParams.putAll(response.getOrderPayResponseData().getDirectPayParams());
					}else{
						MonitorData monitorData = response.getMonitorData();
						throw new ValidationException(monitorData.toString());
					}
				}
				if(payType.equals(PaymentConstant.PAYMENT_TYPE_PRE_PAY)){
					// 预支付
					if( null != response.getOrderPayResponseData() && 
							PaymentConstant.PAYMENT_STATE_PAYED.equals(response.getOrderPayResponseData().getStatusCode() )){
						// 预支付参数
						logger.info(this.getClass().getSimpleName()+" 预支付参数:{} ",response.getOrderPayResponseData().getPrePayParams());
						outParams.putAll(response.getOrderPayResponseData().getPrePayParams());
					}else{
						MonitorData monitorData = response.getMonitorData();
						throw new ValidationException(monitorData.toString());
					}
				}
				
			}else{
				throw new ValidationException("$_$第三方服务callPay$_$无响应" + orderPayRequest.toString());
			}
		}catch(Exception e){
			logger.error("$_$第三方服务callPay$_$调用出现异常", e);
			throw new ValidationException("$_$第三方服务callPay$_$调用出现异常");
		}
		return outParams;
	}
	
	
	/**
	 * 第三方支付异步回调处理
	 * 分预支付和直接支付
	 * @param inParams
	 * @return
	 * @throws ValidationException
	 */
	public Map<String, Object> callPayBack(HttpServletRequest origiRequest,Platform platform,PaymentOrder payOrder) throws ValidationException{
		// 1.拼装支付异步回调参数
		DIOrderPayBackRequest orderPayBackRequest = new DIOrderPayBackRequest();
		
		// 异步回调响应
		DIOrderPayBackResponse response = null;
		
		Map<String, Object> outParams = new HashMap<String, Object>();
		
		// 原始请求参数
		Map<String, Object> origiRequestMap = new HashMap<String, Object>();
		
		try {
			String callPayUrl = "";
			String timeout = "30000";//默认30秒
			String extend = platform.getExtend();//渠道-商户扩展
			Integer cyle = 3;//默认失败重试次数3次
			String payType = "directPay";//默认是直接支付:directPay;预支付prePay
			String readType = "0";// 默认是:0 reuqest.getParameter读取, 1 inputStream读取
			// 请求支付相关配置
			if(StringUtils.isNotEmpty(extend)){
				JSONObject extJson = JSONObject.parseObject(extend);
				if(extJson.containsKey("callPayUrl") && StringUtils.isNotBlank(extJson.getString("callPayUrl"))){
					callPayUrl = extJson.getString("callPayUrl");
				}else{
					// 未配置,callPayUrl
					logger.error("$_$第三方服务callPayBack$_$调用出现异常,未正确配置callPayUrl");
					throw new ValidationException("$_$第三方服务callPayBack$_$调用出现异常,未正确配置callPayUrl");
				}
				if(extJson.containsKey("timeout") && StringUtils.isNotBlank(extJson.getString("timeout"))){
					timeout = extJson.getString("timeout");
				}
				if(extJson.containsKey("cyle") && StringUtils.isNotBlank(extJson.getString("cyle"))){
					cyle = Integer.parseInt(extJson.getString("cyle"));
				}
				if(extJson.containsKey("payType") && StringUtils.isNotBlank(extJson.getString("payType"))){
					payType = extJson.getString("payType");
				}
				if(extJson.containsKey("readType") && StringUtils.isNotBlank(extJson.getString("readType"))){
					readType = extJson.getString("readType");
				}
			}
			
			// request转换成map传递
			origiRequest.setCharacterEncoding("UTF-8");
			StringBuffer sb = new StringBuffer();
			if (StringUtils.isNotBlank(readType) && readType.equals("0")) {
				Set keys = origiRequest.getParameterMap().keySet();
				for (Object key : keys) {
					String keyString = ObjectUtils.toString(key);
					sb.append(key+"="+origiRequest.getParameter(keyString)).append("&");
					origiRequestMap.put(keyString, origiRequest.getParameter(keyString));
				}
			} else {
				getRequestBody(origiRequestMap, origiRequest, "utf-8");
				sb.append(origiRequestMap.get("RequestBody"));
			}
			logger.info("The original data is : " + sb.toString());
			
			
			orderPayBackRequest.setBackRequestMap(origiRequestMap);// 支付渠道原始参数
			orderPayBackRequest.setPlatform(platform);// 收银台-支付渠道信息
			orderPayBackRequest.setPaymentOrder(payOrder);// 收银台-订单
			
			
			DICallRequest paramData = new DICallRequest();
			paramData.setMethod("OrderPayBackRequest");// 付款回调
			paramData.setOrderPayBackRequest(orderPayBackRequest);

			RequestDatas<DICallRequest> request = new RequestDatas<DICallRequest>();

			request.setAccessId(Long.parseLong(ACCESS_ID));
			request.setAccessPasswd(ACCESS_PWD);
			request.setAccessType(Long.parseLong(ACCESS_TYPE));
			request.setParamdata(paramData);
			request.setVersion("1");
			
			RequestClientInfo requestClientInfo = new RequestClientInfo();
			requestClientInfo.setClientUserIp(IpUtils.getRemoteAddr(origiRequest));
			requestClientInfo.setLocalReqIp(IpUtils.getLoaclAddr());
			requestClientInfo.setStartReqTime(new Date().getTime());
			request.setClientInfo(requestClientInfo);
			
			String body = JSON.toJSONString(request);
			String vString = body + request.getAccessId() + request.getAccessType() + request.getAccessPasswd()
					+ ACCESS_KEY;
			String verfiy = MD5Encrypt.encrypt(vString, "utf-8");

			Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("Content-Type", "application/json");
			headers.put("bodyEncode", "urlencode=utf-8");
			headers.put(NameFactory.request_head.accessId.name(), request.getAccessId());
			headers.put(NameFactory.request_head.accessType.name(), request.getAccessType());
			headers.put(NameFactory.request_head.accessPasswd.name(), request.getAccessPasswd());
			headers.put(NameFactory.request_head.accessVerify.name(), verfiy);
			
			// 发起调用
			logger.info(this.getClass().getSimpleName()+"header{},body:{}",new Object[]{headers,body});
			String ret = callPayRemoteUrl(callPayUrl,headers, body, Integer.parseInt(timeout),"utf-8", cyle, JSONObject.parseObject(platform.getExtend()),null);
			
			EchoInfo<DIOrderPayBackResponse> echoInfo = JSON.parseObject(ret, new TypeReference<EchoInfo<DIOrderPayBackResponse>>() {});
			
			// 支付响应
			response = echoInfo.getData();
			
			if(null!=response){
				/**
				 * 判断是什么支付类型
				 */
				if(payType.equals(PaymentConstant.PAYMENT_TYPE_DIRECT_PAY)){
					// 直接支付
					// 有支付回调响应,
					if( null != response.getOrderPayBackResponseData() && 
							StringUtils.isNotEmpty(response.getOrderPayBackResponseData().getStatusCode()) && 
							DIOrderPayBackResponseData.PAYMENT_STATE_PAYED.equals(response.getOrderPayBackResponseData().getStatusCode())){
						// 直接支付参数,回调支付成功
						logger.info(this.getClass().getSimpleName()+" 直接支付回调参数:{} ",response.getOrderPayBackResponseData().toString());
						outParams.put(PaymentConstant.PAYMENT_STATE, response.getOrderPayBackResponseData().getStatusCode()); // 支付状态
						outParams.put(PaymentConstant.OPPOSITE_MONEY, response.getOrderPayBackResponseData().getMoney()); // 支付金额,分
						outParams.put(PaymentConstant.PAYMENT_ORDER, response.getOrderPayBackResponseData().getPaymentOrder());
						outParams.put(PaymentConstant.OPPOSITE_ORDERNO, response.getOrderPayBackResponseData().getPayplatformOrderNo());// 支付订单
					}else{
						MonitorData monitorData = response.getMonitorData();
						throw new ValidationException(monitorData.toString());
					}
				}
				if(payType.equals(PaymentConstant.PAYMENT_TYPE_PRE_PAY)){
					// 预支付
					if( null != response.getOrderPayBackResponseData() && 
							StringUtils.isNotEmpty(response.getOrderPayBackResponseData().getStatusCode()) && 
							DIOrderPayBackResponseData.PAYMENT_STATE_PAYED.equals(response.getOrderPayBackResponseData().getStatusCode())){
						// 预支付参数
						logger.info(this.getClass().getSimpleName()+" 预支付参数:{} ",response.getOrderPayBackResponseData().toString());
						outParams.put(PaymentConstant.PAYMENT_STATE, response.getOrderPayBackResponseData().getStatusCode()); // 支付状态
						outParams.put(PaymentConstant.OPPOSITE_CURRENCY, response.getOrderPayBackResponseData().getCurrency()); // 对方传来的交易币种
						outParams.put(PaymentConstant.OPPOSITE_MONEY, response.getOrderPayBackResponseData().getMoney()); // 支付金额,分
						outParams.put(PaymentConstant.PAYMENT_ORDER, response.getOrderPayBackResponseData().getPaymentOrder());
						outParams.put(PaymentConstant.OPPOSITE_ORDERNO, response.getOrderPayBackResponseData().getPayplatformOrderNo());// 支付订单

					}else{
						MonitorData monitorData = response.getMonitorData();
						throw new ValidationException(monitorData.toString());
					}
				}
				
			}else{
				throw new ValidationException("$_$第三方服务callPayBack$_$无响应" + orderPayBackRequest.toString());
			}
		}catch(Exception e){
			logger.error("$_$第三方服务callPayBack$_$调用出现异常", e);
			throw new ValidationException("$_$第三方服务callPayBack$_$调用出现异常");
		}
		return outParams;
	}
	
	/**
	 * 统一实现第三方支付回调,响应结果
	 * @param inParams
	 * @param response
	 * @param isImprestedSuccess
	 */
	public void callPayBackReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		// 请求远程服务返回处理处理
		DIOrderPayBackReturnRequest orderPayBackReturnRequest = new DIOrderPayBackReturnRequest();
		// 响应给第三方平台的处理结果
		DIOrderPayBackReturnResponse orderPayBackReturnResponse = new DIOrderPayBackReturnResponse();
		// 原始请求request
		HttpServletRequest origiRequest = (HttpServletRequest) inParams.get("request");
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		try {
			String result = "";
			String contentType = "";
			
			String callUrl = "";
			String timeout = "30000";
			String extend = platform.getExtend();
			Integer cyle = 3;//默认失败重试次数3次
			// 请求退款回调响应url
			if(StringUtils.isNotEmpty(extend)){
				JSONObject extJson = JSONObject.parseObject(extend);
				if(extJson.containsKey("callPayUrl") && StringUtils.isNotBlank(extJson.getString("callPayUrl"))){
					callUrl = extJson.getString("callPayUrl");
				}else{
					// 未配置,callPayUrl
					logger.error("$_$第三方服务callPayBackReturn$_$调用出现异常,未正确配置callPayUrl");
					throw new ValidationException("$_$第三方服务callPayBackReturn$_$调用出现异常,未正确配置callPayUrl");
				}
				if(extJson.containsKey("timeout") && StringUtils.isNotBlank(extJson.getString("timeout"))){
					timeout = extJson.getString("timeout");
				}
				if(extJson.containsKey("cyle") && StringUtils.isNotBlank(extJson.getString("cyle"))){
					cyle = Integer.parseInt(extJson.getString("cyle"));
				}
			}
			
			DICallRequest paramData = new DICallRequest();
			orderPayBackReturnRequest.setImprestedSuccess(isImprestedSuccess);
			paramData.setMethod("OrderPayBackReturnRequest");// 支付回调响应获取
			paramData.setOrderPayBackReturnRequest(orderPayBackReturnRequest);
			
			
			RequestDatas<DICallRequest> request = new RequestDatas<DICallRequest>();

			request.setAccessId(Long.parseLong(ACCESS_ID));
			request.setAccessPasswd(ACCESS_PWD);
			request.setAccessType(Long.parseLong(ACCESS_TYPE));
			request.setParamdata(paramData);
			request.setVersion("1");
			RequestClientInfo requestClientInfo = new RequestClientInfo();
			requestClientInfo.setClientUserIp(IpUtils.getRemoteAddr(origiRequest));
			requestClientInfo.setLocalReqIp(IpUtils.getLoaclAddr());
			requestClientInfo.setStartReqTime(new Date().getTime());
			request.setClientInfo(requestClientInfo);
			String body = JSON.toJSONString(request);
			String vString = body + request.getAccessId() + request.getAccessType() + request.getAccessPasswd()
					+ ACCESS_KEY;
			String verfiy = MD5Encrypt.encrypt(vString, "utf-8");

			Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("Content-Type", "application/json");
			headers.put("bodyEncode", "urlencode=utf-8");
			headers.put(NameFactory.request_head.accessId.name(), request.getAccessId());
			headers.put(NameFactory.request_head.accessType.name(), request.getAccessType());
			headers.put(NameFactory.request_head.accessPasswd.name(), request.getAccessPasswd());
			headers.put(NameFactory.request_head.accessVerify.name(), verfiy);
			
			// 发起调用
			IntHolder d  = new IntHolder();
//			String ret = HttpClient.post(callUrl, headers, URLEncoder.encode(body, "utf-8"),Integer.parseInt(timeout), "utf-8",d);
			String ret = callPayRemoteUrl(callUrl,headers, body, Integer.parseInt(timeout),"utf-8", cyle, JSONObject.parseObject(platform.getExtend()),d);
			
			EchoInfo<DIOrderPayBackReturnResponse> echoInfo = JSON.parseObject(ret, new TypeReference<EchoInfo<DIOrderPayBackReturnResponse>>() {});
			
			// 支付回调响应
			orderPayBackReturnResponse = echoInfo.getData();
			
			if(null!=orderPayBackReturnResponse){
				// 有支付回调,支付回调处理完成,响应给第三方平台结果
				if(StringUtils.isNotEmpty(orderPayBackReturnResponse.getSuccFail())){
					contentType = orderPayBackReturnResponse.getContentType();
					result = orderPayBackReturnResponse.getSuccFail();
				}else{
					MonitorData monitorData = orderPayBackReturnResponse.getMonitorData();
					throw new ValidationException(monitorData.toString());
				}
			}else{
				throw new ValidationException("$_$第三方服务payBackReturn$_$无响应," + orderPayBackReturnRequest.toString());
			}
			response.setStatus(d.value);
			response.setContentType(contentType);
			responseAndWrite(response, result);
		} catch (Exception e) {
			throw new ValidationException("$_$第三方服务payBackReturn$_$调用出现异常");
		}
	}
	
	/**
	 * 统一处理支付查询验证
	 * @param request
	 * @param platform
	 * @return
	 */
	public Map<String, Object> callPayQuery(Map<String, Object> queryParams){
		Map<String, Object> outParams = ErrorCode.getErrorCode(1);
		
		// 请求
		DIOrderPayQueryRequest orderPayQueryRequest = new DIOrderPayQueryRequest();
		PayRequestParams payRequestParams = new PayRequestParams();
		
		// 响应
		DIOrderPayQueryResponse response = new DIOrderPayQueryResponse();
		try {
			// 封装请求数据
			PaymentOrder paymentOrder = (PaymentOrder) queryParams.get(PaymentConstant.PAYMENT_ORDER);
			Platform platform = (Platform) queryParams.get(PaymentConstant.PAYMENT_PLATFORM);

			HttpServletRequest req = (HttpServletRequest) queryParams.get(RefundmentConstant.HTTP_REQUEST);
			
			String callUrl = "";
			String extend = platform.getExtend();
			String timeout = "30000";
			Integer cyle = 3;//默认失败重试次数3次
			String payType = "directPay";//默认是直接支付:directPay;预支付prePay
			
			// 请求查询退款url
			if(StringUtils.isNotEmpty(extend)){
				JSONObject extJson = JSONObject.parseObject(extend);
				if(extJson.containsKey("callPayUrl") && StringUtils.isNotBlank(extJson.getString("callPayUrl"))){
					callUrl = extJson.getString("callPayUrl");
				}else{
					// 未配置,callPayUrl
					logger.error("$_$第三方服务callPayQuery$_$调用出现异常,未正确配置callPayQueryUrl");
					throw new ValidationException("$_$第三方服务callPayQuery$_$调用出现异常,未正确配置callPayQueryUrl");
				}
				if(extJson.containsKey("timeout") && StringUtils.isNotBlank(extJson.getString("timeout"))){
					timeout = extJson.getString("timeout");
				}
				if(extJson.containsKey("cyle") && StringUtils.isNotBlank(extJson.getString("cyle"))){
					cyle = Integer.parseInt(extJson.getString("cyle"));
				}
				if(extJson.containsKey("payType") && StringUtils.isNotBlank(extJson.getString("payType"))){
					payType = extJson.getString("payType");
				}
			}
			// 设置请求参数
			payRequestParams.setPaymentOrder(paymentOrder);
			payRequestParams.setPlatform(platform);
			
			orderPayQueryRequest.setPayRequestParams(payRequestParams);
			
			DICallRequest paramData = new DICallRequest();
			paramData.setMethod("OrderPayQueryRequest");// 支付查询验证
			paramData.setOrderPayQueryRequest(orderPayQueryRequest);
			
			RequestDatas<DICallRequest> request = new RequestDatas<DICallRequest>();
			request.setAccessId(Long.parseLong(ACCESS_ID));
			request.setAccessPasswd(ACCESS_PWD);
			request.setAccessType(Long.parseLong(ACCESS_TYPE));
			request.setParamdata(paramData);
			request.setVersion("1");
			
			RequestClientInfo requestClientInfo = new RequestClientInfo();
			requestClientInfo.setClientUserIp(IpUtils.getLoaclAddr());
			requestClientInfo.setLocalReqIp(IpUtils.getLoaclAddr());
			requestClientInfo.setStartReqTime(new Date().getTime());
			request.setClientInfo(requestClientInfo);
			
			String body = JSON.toJSONString(request);
			String vString = body + request.getAccessId() + request.getAccessType() + request.getAccessPasswd()
					+ ACCESS_KEY;
			String verfiy = MD5Encrypt.encrypt(vString, "utf-8");

			Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("Content-Type", "application/json");
			headers.put("bodyEncode", "urlencode=utf-8");
			headers.put(NameFactory.request_head.accessId.name(), request.getAccessId());
			headers.put(NameFactory.request_head.accessType.name(), request.getAccessType());
			headers.put(NameFactory.request_head.accessPasswd.name(), request.getAccessPasswd());
			headers.put(NameFactory.request_head.accessVerify.name(), verfiy);
			
			// 发起调用
//			String ret = HttpClient.post(callUrl, headers, URLEncoder.encode(body, "utf-8"),Integer.parseInt(timeout), "utf-8");
			String ret = callPayRemoteUrl(callUrl,headers, body, Integer.parseInt(timeout),"utf-8", cyle, JSONObject.parseObject(platform.getExtend()),null);
			
			EchoInfo<DIOrderPayQueryResponse> echoInfo = JSON.parseObject(ret, new TypeReference<EchoInfo<DIOrderPayQueryResponse>>() {});
			
			// 退款响应
			response = echoInfo.getData();
			if(null!=response){
				/**
				 * 判断是什么支付类型
				 */
				if(payType.equals(PaymentConstant.PAYMENT_TYPE_DIRECT_PAY)){
					// 直接支付
					// 有支付回调响应,
					if( null != response.getOrderPayQueryData() && 
							StringUtils.isNotEmpty(response.getOrderPayQueryData().getStatusCode()) && 
							DIOrderPayBackResponseData.PAYMENT_STATE_PAYED.equals(response.getOrderPayQueryData().getStatusCode())){
						// 直接支付参数,回调支付成功
						logger.info(this.getClass().getSimpleName()+" 直接支付回调参数:{} ",response.getOrderPayQueryData().toString());
						outParams.put(PaymentConstant.PAYMENT_STATE, response.getOrderPayQueryData().getStatusCode()); // 支付状态
						outParams.put(PaymentConstant.OPPOSITE_MONEY, response.getOrderPayQueryData().getMoney()); // 支付金额,分
						outParams.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
						outParams.put(PaymentConstant.OPPOSITE_ORDERNO, response.getOrderPayQueryData().getPayplatformOrderNo());// 支付订单
					}else{
						MonitorData monitorData = response.getMonitorData();
						throw new ValidationException(monitorData.toString());
					}
				}
				if(payType.equals(PaymentConstant.PAYMENT_TYPE_PRE_PAY)){
					// 预支付
					if( null != response.getOrderPayQueryData() && 
							StringUtils.isNotEmpty(response.getOrderPayQueryData().getStatusCode()) && 
							DIOrderPayBackResponseData.PAYMENT_STATE_PAYED.equals(response.getOrderPayQueryData().getStatusCode())){
						// 预支付参数
						logger.info(this.getClass().getSimpleName()+" 预支付参数:{} ",response.getOrderPayQueryData().toString());
						outParams.put(PaymentConstant.PAYMENT_STATE, response.getOrderPayQueryData().getStatusCode()); // 支付状态
						outParams.put(PaymentConstant.OPPOSITE_MONEY, response.getOrderPayQueryData().getMoney()); // 支付金额,分
						outParams.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
						outParams.put(PaymentConstant.OPPOSITE_ORDERNO, response.getOrderPayQueryData().getPayplatformOrderNo());// 支付订单

					}else{
						MonitorData monitorData = response.getMonitorData();
						throw new ValidationException(monitorData.toString());
					}
				}
				
			}else{
				throw new ValidationException("$_$第三方服务callPayQuery$_$无响应" + orderPayQueryRequest.toString());
			}
			
		} catch (Exception e) {
			logger.error("$_$第三方服务callPayQuery$_$调用出现异常", e);
			throw new ValidationException("$_$第三方服务callPayQuery$_$调用出现异常");
		}
		return outParams;
	}
	
	/**
	 *  统一处理前端跳转获取订单号
	 * @param request
	 * @return
	 */
	public String callPayGetOrderNoFromRequest(Platform platform,HttpServletRequest origRequest) {
		
		String orderNo = "";
		// 请求
		DIOrderPayGetOrderNoRequest orderPayGetOrderNoRequest = new DIOrderPayGetOrderNoRequest();
		
		// 响应
		DIOrderPayGetOrderNoResponse response = new DIOrderPayGetOrderNoResponse();
		
		// 请求map
		Map<String, Object> reqMap = new HashMap<String, Object>();
		try {
			// 封装请求数据
			
			String callUrl = "";
			String extend = platform.getExtend();
			String timeout = "30000";
			Integer cyle = 3;//默认失败重试次数3次
			String payType = "directPay";//默认是直接支付:directPay;预支付prePay
			String readType = "0";// 默认是:0 reuqest.getParameter读取, 1 inputStream读取
			// 请求查询退款url
			if(StringUtils.isNotEmpty(extend)){
				JSONObject extJson = JSONObject.parseObject(extend);
				if(extJson.containsKey("callPayUrl") && StringUtils.isNotBlank(extJson.getString("callPayUrl"))){
					callUrl = extJson.getString("callPayUrl");
				}else{
					// 未配置,callPayUrl
					logger.error("$_$第三方服务callPayGetOrderNo$_$调用出现异常,未正确配置callPayUrl");
					throw new ValidationException("$_$第三方服务callPayGetOrderNo$_$调用出现异常,未正确配置callPayUrl");
				}
				if(extJson.containsKey("timeout") && StringUtils.isNotBlank(extJson.getString("timeout"))){
					timeout = extJson.getString("timeout");
				}
				if(extJson.containsKey("cyle") && StringUtils.isNotBlank(extJson.getString("cyle"))){
					cyle = Integer.parseInt(extJson.getString("cyle"));
				}
				if(extJson.containsKey("payType") && StringUtils.isNotBlank(extJson.getString("payType"))){
					payType = extJson.getString("payType");
				}
				if(extJson.containsKey("readType") && StringUtils.isNotBlank(extJson.getString("readType"))){
					readType = extJson.getString("readType");
				}
			}
			
			if (StringUtils.isNotBlank(readType) && readType.equals("0")) {
				Set keys = origRequest.getParameterMap().keySet();
				for (Object key : keys) {
					String keyString = ObjectUtils.toString(key);
					reqMap.put(keyString, origRequest.getParameter(keyString));
				}
			} else {
				getRequestBody(reqMap, origRequest, "utf-8");
			}
			logger.info("The original data is : " + reqMap.toString());
			
			// 设置请求参数
			orderPayGetOrderNoRequest.setReqMap(reqMap);
			
			DICallRequest paramData = new DICallRequest();
			paramData.setMethod("OrderPayGetOrderNoRequest");// 支付查询验证
			paramData.setOrderPayGetOrderNoRequest(orderPayGetOrderNoRequest);
			
			RequestDatas<DICallRequest> request = new RequestDatas<DICallRequest>();
			request.setAccessId(Long.parseLong(ACCESS_ID));
			request.setAccessPasswd(ACCESS_PWD);
			request.setAccessType(Long.parseLong(ACCESS_TYPE));
			request.setParamdata(paramData);
			request.setVersion("1");
			
			RequestClientInfo requestClientInfo = new RequestClientInfo();
			requestClientInfo.setClientUserIp(IpUtils.getRemoteAddr(origRequest));
			requestClientInfo.setLocalReqIp(IpUtils.getLoaclAddr());
			requestClientInfo.setStartReqTime(new Date().getTime());
			request.setClientInfo(requestClientInfo);
			
			String body = JSON.toJSONString(request);
			String vString = body + request.getAccessId() + request.getAccessType() + request.getAccessPasswd()
					+ ACCESS_KEY;
			String verfiy = MD5Encrypt.encrypt(vString, "utf-8");

			Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("Content-Type", "application/json");
			headers.put("bodyEncode", "urlencode=utf-8");
			headers.put(NameFactory.request_head.accessId.name(), request.getAccessId());
			headers.put(NameFactory.request_head.accessType.name(), request.getAccessType());
			headers.put(NameFactory.request_head.accessPasswd.name(), request.getAccessPasswd());
			headers.put(NameFactory.request_head.accessVerify.name(), verfiy);
			
			// 发起调用
//			String ret = HttpClient.post(callUrl, headers, URLEncoder.encode(body, "utf-8"),Integer.parseInt(timeout), "utf-8");
			String ret = callPayRemoteUrl(callUrl,headers, body, Integer.parseInt(timeout),"utf-8", cyle, JSONObject.parseObject(platform.getExtend()),null);
			
			EchoInfo<DIOrderPayGetOrderNoResponse> echoInfo = JSON.parseObject(ret, new TypeReference<EchoInfo<DIOrderPayGetOrderNoResponse>>() {});
			
			// 退款响应
			response = echoInfo.getData();
			if(null != response){
				if( null != response.getOrderNo() ){
					orderNo = response.getOrderNo();
				}else{
					MonitorData monitorData = response.getMonitorData();
					throw new ValidationException(monitorData.toString());
				}
			}else{
				throw new ValidationException("$_$第三方服务callPayGetOrderNo$_$无响应" + orderPayGetOrderNoRequest.toString());
			}
		} catch (Exception e) {
			logger.error("$_$第三方服务callPayGetOrderNo$_$调用出现异常", e);
			throw new ValidationException("$_$第三方服务callPayGetOrderNo$_$调用出现异常");
		}
		return StringUtils.trim(orderNo);
		
	}
	
	/**
	 *  统一处理异步回调获取订单号
	 * @param request
	 * @return
	 */
	public String callPayBackGetOrderNoFromRequest(Platform platform,HttpServletRequest origRequest) {
		
		String orderNo = "";
		// 请求
		DIOrderPayBackGetOrderNoRequest orderPayBackGetOrderNoRequest = new DIOrderPayBackGetOrderNoRequest();
		
		// 响应
		DIOrderPayBackGetOrderNoResponse response = new DIOrderPayBackGetOrderNoResponse();
		
		// 请求map
		Map<String, Object> reqMap = new HashMap<String, Object>();
		try {
			// 封装请求数据
			
			String callUrl = "";
			String extend = platform.getExtend();
			String timeout = "30000";
			Integer cyle = 3;//默认失败重试次数3次
			String payType = "directPay";//默认是直接支付:directPay;预支付prePay
			String readType = "0";// 默认是:0 reuqest.getParameter读取, 1 inputStream读取
			// 请求查询退款url
			if(StringUtils.isNotEmpty(extend)){
				JSONObject extJson = JSONObject.parseObject(extend);
				if(extJson.containsKey("callPayUrl") && StringUtils.isNotBlank(extJson.getString("callPayUrl"))){
					callUrl = extJson.getString("callPayUrl");
				}else{
					// 未配置,callPayUrl
					logger.error("$_$第三方服务callPayGetOrderNo$_$调用出现异常,未正确配置callPayUrl");
					throw new ValidationException("$_$第三方服务callPayGetOrderNo$_$调用出现异常,未正确配置callPayUrl");
				}
				if(extJson.containsKey("timeout") && StringUtils.isNotBlank(extJson.getString("timeout"))){
					timeout = extJson.getString("timeout");
				}
				if(extJson.containsKey("cyle") && StringUtils.isNotBlank(extJson.getString("cyle"))){
					cyle = Integer.parseInt(extJson.getString("cyle"));
				}
				if(extJson.containsKey("payType") && StringUtils.isNotBlank(extJson.getString("payType"))){
					payType = extJson.getString("payType");
				}
				if(extJson.containsKey("readType") && StringUtils.isNotBlank(extJson.getString("readType"))){
					readType = extJson.getString("readType");
				}
			}
			
			if (StringUtils.isNotBlank(readType) && readType.equals("0")) {
				Set keys = origRequest.getParameterMap().keySet();
				for (Object key : keys) {
					String keyString = ObjectUtils.toString(key);
					reqMap.put(keyString, origRequest.getParameter(keyString));
				}
			} else {
				getRequestBody(reqMap, origRequest, "utf-8");
			}
			logger.info("The original data is : " + reqMap.toString());
			
			// 设置请求参数
			orderPayBackGetOrderNoRequest.setReqMap(reqMap);
			
			DICallRequest paramData = new DICallRequest();
			paramData.setMethod("OrderPayBackGetOrderNoRequest");// 异步回调订单号查询
			paramData.setOrderPayBackGetOrderNoRequest(orderPayBackGetOrderNoRequest);
			
			RequestDatas<DICallRequest> request = new RequestDatas<DICallRequest>();
			request.setAccessId(Long.parseLong(ACCESS_ID));
			request.setAccessPasswd(ACCESS_PWD);
			request.setAccessType(Long.parseLong(ACCESS_TYPE));
			request.setParamdata(paramData);
			request.setVersion("1");
			
			RequestClientInfo requestClientInfo = new RequestClientInfo();
			requestClientInfo.setClientUserIp(IpUtils.getRemoteAddr(origRequest));
			requestClientInfo.setLocalReqIp(IpUtils.getLoaclAddr());
			requestClientInfo.setStartReqTime(new Date().getTime());
			request.setClientInfo(requestClientInfo);
			
			String body = JSON.toJSONString(request);
			String vString = body + request.getAccessId() + request.getAccessType() + request.getAccessPasswd()
					+ ACCESS_KEY;
			String verfiy = MD5Encrypt.encrypt(vString, "utf-8");

			Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("Content-Type", "application/json");
			headers.put("bodyEncode", "urlencode=utf-8");
			headers.put(NameFactory.request_head.accessId.name(), request.getAccessId());
			headers.put(NameFactory.request_head.accessType.name(), request.getAccessType());
			headers.put(NameFactory.request_head.accessPasswd.name(), request.getAccessPasswd());
			headers.put(NameFactory.request_head.accessVerify.name(), verfiy);
			
			// 发起调用
//			String ret = HttpClient.post(callUrl, headers, URLEncoder.encode(body, "utf-8"),Integer.parseInt(timeout), "utf-8");
			String ret = callPayRemoteUrl(callUrl,headers, body, Integer.parseInt(timeout),"utf-8", cyle, JSONObject.parseObject(platform.getExtend()),null);
			
			EchoInfo<DIOrderPayBackGetOrderNoResponse> echoInfo = JSON.parseObject(ret, new TypeReference<EchoInfo<DIOrderPayBackGetOrderNoResponse>>() {});
			
			// 退款响应
			response = echoInfo.getData();
			if(null != response){
				if( null != response.getOrderNo() ){
					orderNo = response.getOrderNo();
				}else{
					MonitorData monitorData = response.getMonitorData();
					throw new ValidationException(monitorData.toString());
				}
			}else{
				throw new ValidationException("$_$第三方服务callPayBackGetOrderNo$_$无响应" + orderPayBackGetOrderNoRequest.toString());
			}
		} catch (Exception e) {
			logger.error("$_$第三方服务callPayBackGetOrderNo$_$调用出现异常", e);
			throw new ValidationException("$_$第三方服务callPayBackGetOrderNo$_$调用出现异常");
		}
		return StringUtils.trim(orderNo);
		
	}
	
	/**
	 * 获取请求参数
	 * @param reqMap
	 * @param request
	 * @param charset
	 */
	private void getRequestBody(Map<String, Object> reqMap, HttpServletRequest request, String charset) {
		if (StringUtils.isBlank(AuthenticationCommonFilter.getRequestBody().get())) {
			InputStream input = null;
			InputStreamReader reader = null;
			BufferedReader bread = null;
			try {
				request.setCharacterEncoding(charset);

				input = request.getInputStream();
				reader = new InputStreamReader(input);
				bread = new BufferedReader(reader);
				StringBuffer _sb = new StringBuffer();
				String _b = "";
				while ((_b = bread.readLine()) != null) {
					_sb.append(_b).append(System.getProperty("line.separator"));
				}
				AuthenticationCommonFilter.getRequestBody().set(_sb.toString());
				reqMap.put("RequestBody", _sb);
			} catch (IOException e) {
				logger.error("", e);
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
				if (bread != null) {
					try {
						bread.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			}
		} else {
			reqMap.put("RequestBody", AuthenticationCommonFilter.getRequestBody().get());
		}

	}


	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException, DataAccessException,
			PaymentRedirectException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}