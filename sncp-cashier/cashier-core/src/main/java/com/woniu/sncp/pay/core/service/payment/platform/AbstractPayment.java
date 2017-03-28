package com.woniu.sncp.pay.core.service.payment.platform;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.woniu.pay.common.utils.RefundmentConstant;
import com.woniu.pay.pojo.ApplyPayRefundBatchDetail;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.cbss.api.imprest.direct.request.DICallRequest;
import com.woniu.sncp.cbss.api.imprest.direct.request.DIOrderRefundBackGetOrderNoRequest;
import com.woniu.sncp.cbss.api.imprest.direct.request.DIOrderRefundBackRequest;
import com.woniu.sncp.cbss.api.imprest.direct.request.DIOrderRefundBackReturnRequest;
import com.woniu.sncp.cbss.api.imprest.direct.request.DIOrderRefundQueryRequest;
import com.woniu.sncp.cbss.api.imprest.direct.request.DIOrderRefundRequest;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundBackCallResponse;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundBackCallReturnResponse;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundBackGetOrderNoResponse;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundData;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundQueryResponse;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundResponse;
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
import com.woniu.sncp.pay.common.utils.http.PayCheckUtils;
import com.woniu.sncp.pay.core.filter.AuthenticationCommonFilter;
import com.woniu.sncp.pay.core.service.PaymentOrderService;
import com.woniu.sncp.pay.core.service.PaymentService;
import com.woniu.sncp.pay.core.service.PlatformService;
import com.woniu.sncp.pay.core.service.RefundmentOrderService;
import com.woniu.sncp.pay.core.transfer.model.TransferModel;
import com.woniu.sncp.pay.core.transfer.platform.Transfer;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.pojo.payment.TransferOrder;
import com.woniu.sncp.pojo.refund.PayRefundBatch;
import com.woniu.sncp.web.IpUtils;

/**
 * 支付渠道业务抽象类，主要职责如下：<br />
 * 1.完成各平台独立的基本方法<br />
 * 2.调用各平台基本方法实现我方公共的框架方法，如：生成订单、完成支付等
 * 
 * 3.增加退款请求,退款回调,退款查询,退款回调响应
 */
public abstract class AbstractPayment implements Payment,Transfer {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	protected PaymentOrderService paymentOrderService;
//	@Resource
	protected RefundmentOrderService refundmentOrderService;
	@Resource
	protected PaymentService paymentService;
	@Resource
	protected PlatformService platformService;
//	@Resource
//	protected TransferOrderService transferOrderService;
	
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
	 * 是否有前台返回，易宝的前后台返回一起 - 默认是有
	 */
	protected boolean foregroundReturn = true;

	/**
	 * 写出信息到response
	 * 
	 * @param response
	 * @param message
	 */
	public void responseAndWrite(HttpServletResponse response, String message) {
		PrintWriter writer = null;
		try {
			if (logger.isInfoEnabled())
				logger.info("返回给支付平台的信息：" + message);
			
			writer = response.getWriter();
			writer.print(message);
			writer.flush();
		} catch (IOException ex) {
			logger.error("返回给对方跳转的地址失败", ex);
		} finally {
			try {
				writer.close();
				writer = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return;
	}

	/**
	 * 各平台的浏览器调整，如果平台需要浏览器调转，则复写改方法
	 * 
	 * @param response
	 * @param isImprestedSuccess
	 * @param message
	 */
	public void imprestedRedirect(HttpServletRequest request, HttpServletResponse response) {
	}

	/**
	 * 浏览器跳转
	 * 
	 * @param response
	 * @param location
	 */
	public void responseRedirect(HttpServletResponse response, String location) {
		try {
			if (logger.isDebugEnabled())
				logger.debug("response跳转：" + location);

			response.sendRedirect(location);
		} catch (IOException e) {
			logger.error("response跳转异常", e);
		}
	}

	/**
	 * 查询该平台是否有前台返回 - 默认是有前台返回
	 */
	public boolean isForegroundReturn() {
		return foregroundReturn;
	}

	/**
	 * 获取平台实际支付币种 - 默认为人民币 - "R"<br />
	 * 如果有其他币种，则具体平台需要覆写该方法
	 * 
	 * @return
	 */
	@Override
	public String getMoneyCurrency() {
		return MONEY_CURRENCY;
	}
	
	/**
	 * 将参数以 key1=value1&key2=value2的形式按照放入顺序拼装
	 * 
	 * @param inParams
	 * @param blankIsNotIn
	 *            true - 空白字段不加入 false - 空白字段也加入
	 * @return
	 */
	public String linkedHashMapToStringWithKey(LinkedHashMap<String, Object> inParams, boolean blankIsNotIn) {
		return linkedHashMapToStringWithKey(inParams, blankIsNotIn, null);
	}

	/**
	 * 将参数以 key1=value1&key2=value2的形式按照放入顺序拼装
	 * 
	 * @param inParams
	 * @param blankIsNotIn
	 *            true - 空白字段不加入 false - 空白字段也加入
	 * @param replaceString
	 *            blankIsNotIn=ture且该值不为空-则将inParams中不为空的字段替换为replaceString
	 * @return
	 */
	public String linkedHashMapToStringWithKey(LinkedHashMap<String, Object> inParams, boolean blankIsNotIn,
			String replaceString) {
		StringBuffer buffer = new StringBuffer();
		for (Iterator<Entry<String, Object>> keyValuePairs = inParams.entrySet().iterator(); keyValuePairs.hasNext();) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) keyValuePairs.next();
			String key = entry.getKey();
			String value = ObjectUtils.toString(entry.getValue());
			if (!blankIsNotIn) {
				if (StringUtils.isNotBlank(replaceString) && StringUtils.isBlank(value))
					buffer.append(key).append("=").append(replaceString).append("&");
				else
					buffer.append(key).append("=").append(value).append("&");
			} else if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value))
				buffer.append(key).append("=").append(value).append("&");
		}
		return StringUtils.substringBeforeLast(buffer.toString(), "&");
	}
	
	public Map<String, Object> cancelOrder(Map<String, Object> inParams){
		return null;
	}
	/**
	 * 退款参数构造
	 * @param inParams
	 * @return
	 * @throws ValidationException
	 */
	public Map<String, Object> refundedParams(Map<String, Object> inParams) throws ValidationException{
		return null;
	}
	/**
	 * 执行退款
	 * @param inParams
	 * @return
	 */
	public Map<String, Object> executeRefund(Map<String, Object> inParams) {
		return null;
	}
	/**
	 * 退款查询验证
	 * @param orderRefundQueryRequest
	 * @return
	 */
	public Map<String, Object> orderRefundQuery(DIOrderRefundQueryRequest orderRefundQueryRequest){
		return null;
	}
	/**
	 * 退款后台回调参数校验
	 * @param request
	 * @param platform
	 * @return
	 * @throws ValidationException
	 * @throws DataAccessException
	 * @throws PaymentRedirectException
	 */
	public Map<String, Object> validateRefundBackParams(HttpServletRequest request, Platform platform)
			throws ValidationException, DataAccessException, PaymentRedirectException{
		return null;
	};
	/**
	 * 获取我方批次号
	 * @param request
	 * @return
	 */
	public String getBatchNoFromRequest(HttpServletRequest request) {
		return request.getParameter("batch_no");
	}
	/**
	 * 统一实现第三方退款,处理结果
	 * @param inParams
	 * @param response
	 * @param isImprestedSuccess
	 */
	public void refundmentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		// 请求远程服务返回处理处理
		DIOrderRefundBackReturnRequest backReturnrequest = new DIOrderRefundBackReturnRequest();
		// 响应给第三方平台的处理结果
		DIOrderNoRefundBackCallReturnResponse returnResponse = new DIOrderNoRefundBackCallReturnResponse();
		// 原始请求request
		HttpServletRequest origiRequest = (HttpServletRequest) inParams.get("request");
		Platform platform = (Platform) inParams.get(RefundmentConstant.PAYMENT_PLATFORM);
		try {
			String result = "";
			
			String callUrl = "";
			String timeout = "30000";
			String extend = platform.getExtend();
			Integer cyle = 3;//默认失败重试次数3次
			// 请求退款回调响应url
			if(StringUtils.isNotEmpty(extend)){
				JSONObject extJson = JSONObject.parseObject(extend);
				if(StringUtils.isNotBlank(extJson.getString("callUrl"))){
					callUrl = extJson.getString("callUrl");
				}
				if(StringUtils.isNotBlank(extJson.getString("timeout"))){
					timeout = extJson.getString("timeout");
				}
				if(StringUtils.isNotBlank(extJson.getString("cyle"))){
					cyle = Integer.parseInt(extJson.getString("cyle"));
				}
			}
			
			DICallRequest paramData = new DICallRequest();
			backReturnrequest.setImprestedSuccess(isImprestedSuccess);
			paramData.setMethod("OrderRefundBackReturnSign");// 退款回调响应获取
			paramData.setOrderRefundBackReturnRequest(backReturnrequest);
			
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
			String ret = callRemoteUrl(callUrl,headers, body, Integer.parseInt(timeout),"utf-8", cyle, JSONObject.parseObject(platform.getExtend()),d);
			
			EchoInfo<DIOrderNoRefundBackCallReturnResponse> echoInfo = JSON.parseObject(ret, new TypeReference<EchoInfo<DIOrderNoRefundBackCallReturnResponse>>() {});
			
			// 退款响应
			returnResponse = echoInfo.getData();
			
			if(null!=returnResponse){
				response.setContentType("text/html");
				// 有退款响应,退款请求成功,处理中
				if(StringUtils.isNotEmpty(returnResponse.getSuccFail())){
					result = returnResponse.getSuccFail();
					response.setContentType(returnResponse.getContentType());
				}else{
					MonitorData monitorData = returnResponse.getMonitorData();
					throw new ValidationException(monitorData.toString());
				}
			}else{
				throw new ValidationException("$_$第三方服务refundmentReturn$_$无响应," + request.toString());
			}
			response.setStatus(d.value);
			responseAndWrite(response, result);
		} catch (Exception e) {
			throw new ValidationException("$_$第三方服务refundmentReturn$_$调用出现异常");
		}
	}
	
		
	public boolean validateRequestSource(Platform platform,TransferModel transferModel,
			Map<String, Object> extParams) {
		if(StringUtils.isEmpty(platform.getTransferValidateUrl())){
			throw new ValidationException("请求验证地址不可以为空");
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		params.put("orderno", transferModel.getReceiveOrderNo());
		params.put("money", transferModel.getMoney());
		params.put("account", transferModel.getAccount());
		params.put("merchantid", transferModel.getMerchantId());
		params.put("platformid", transferModel.getPlatformId());
		
		String postResponse = PayCheckUtils.postRequst(platform.getTransferValidateUrl(), params, 3000, _charset_encode, "validateRequestSource");
		return new Boolean(postResponse);
	}
	
	public boolean transferRequest(Platform platform,TransferModel transferModel, Map<String, Object> extParams) {
		return false;
	}
	
	public String requestParamsSign(Platform platform,Map<String, Object> inParams)
			throws ValidationException {
		throw new ValidationException("未实现转账功能");
	}

	public Map<String, Object> backendParamsValidate(
			HttpServletRequest request, Platform platform)
			throws ValidationException, DataAccessException{
		throw new ValidationException("未实现转账功能");
	}

	public void backendResponse(Map<String, Object> params,
			HttpServletResponse response, boolean isSccess) {
		responseAndWrite(response, "fail");
	}

	public boolean transferQuery(Platform platform,
			TransferOrder order, Map<String, Object> extParams) {
		return false;
	}
	
	/**
	 * 统一实现第三方退款
	 * @param inParams
	 * @return
	 * @throws ValidationException
	 */
	public Map<String, Object> callRefund(Map<String, Object> inParams) throws ValidationException{
		// 1.拼装参数
		DIOrderRefundRequest orderRefundRequest = new DIOrderRefundRequest();
		// 退款响应
		DIOrderNoRefundResponse response = null;
		Map<String, Object> outParams = new HashMap<String, Object>();
		Map<String, Object> requestMap = new HashMap<String, Object>();
		
		try {
			
			PayRefundBatch payRefundBatch = (PayRefundBatch) inParams.get(RefundmentConstant.REFUNDMENT_BATCH);
			Platform platform = (Platform) inParams.get(RefundmentConstant.PAYMENT_PLATFORM);
			HttpServletRequest req = (HttpServletRequest) inParams.get(RefundmentConstant.HTTP_REQUEST);
			
			// request转换成map传递
			Enumeration<String> reqBody = req.getParameterNames();
			while (reqBody.hasMoreElements()) {
				String paraName = reqBody.nextElement();
				requestMap.put(paraName, req.getParameter(paraName));
			}
			
			String callUrl = "";
			String timeout = "30000";
			String extend = platform.getExtend();
			Integer cyle = 3;//默认失败重试次数3次
			// 请求退款url
			if(StringUtils.isNotEmpty(extend)){
				JSONObject extJson = JSONObject.parseObject(extend);
				if(StringUtils.isNotBlank(extJson.getString("callUrl"))){
					callUrl = extJson.getString("callUrl");
				}
				if(StringUtils.isNotBlank(extJson.getString("timeout"))){
					timeout = extJson.getString("timeout");
				}
				if(StringUtils.isNotBlank(extJson.getString("cyle"))){
					cyle = Integer.parseInt(extJson.getString("cyle"));
				}
			}
			
			orderRefundRequest.setPlatform(platform);
			orderRefundRequest.setPayRefundBatch(payRefundBatch);
			orderRefundRequest.setRequestMap(requestMap);
			
			/**退款数据集*/
			List<ApplyPayRefundBatchDetail> dataDetailList = null;
			//请求支付系统的退款参数
			if(! StringUtils.isBlank(ObjectUtils.toString(payRefundBatch.getDetails()))){
				dataDetailList = JSONArray.parseArray(ObjectUtils.toString(payRefundBatch.getDetails()),ApplyPayRefundBatchDetail.class);
				for(ApplyPayRefundBatchDetail applyPayRefundBatchDetail :dataDetailList){
//	    			PaymentOrder paymentOrder = refundmentOrderService.queryOrderByPartnerOrderNo(applyPayRefundBatchDetail.getOrderno());
//	    			//退款交易结果集
//	    			applyPayRefundBatchDetail.setPaymentOrder(paymentOrder);
	    		}
	    	}
			if((null!=dataDetailList) && (dataDetailList.size()>0)){
				orderRefundRequest.setDataDetailList(dataDetailList);
			}
			
			

			DICallRequest paramData = new DICallRequest();
			paramData.setMethod("OrderRefundRequest");//退款
			paramData.setOrderRefundRequest(orderRefundRequest);

			RequestDatas<DICallRequest> request = new RequestDatas<DICallRequest>();

			request.setAccessId(Long.parseLong(ACCESS_ID));
			request.setAccessPasswd(ACCESS_PWD);
			request.setAccessType(Long.parseLong(ACCESS_TYPE));
			request.setParamdata(paramData);
			request.setVersion("1");
			
			RequestClientInfo requestClientInfo = new RequestClientInfo();
			requestClientInfo.setClientUserIp(IpUtils.getRemoteAddr(req));
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
//			String ret = HttpClient.post(callUrl, headers, URLEncoder.encode(body, "utf-8"),Integer.parseInt(timeout), "utf-8");
			String ret = callRemoteUrl(callUrl,headers, body, Integer.parseInt(timeout),"utf-8", cyle, JSONObject.parseObject(platform.getExtend()),d);
			
			EchoInfo<DIOrderNoRefundResponse> echoInfo = JSON.parseObject(ret, new TypeReference<EchoInfo<DIOrderNoRefundResponse>>() {});
			
			// 退款响应
			response = echoInfo.getData();
			
			if(null!=response){
				// 有退款响应,退款请求成功,处理中或初始化
				if( null != response.getOrderNoRefundData() && StringUtils.isNotEmpty(response.getOrderNoRefundData().getStatusCode()) 
						&& (DIOrderNoRefundData.PAYMENT_STATE_REFUND_PRO.equals(response.getOrderNoRefundData().getStatusCode()) || 
								DIOrderNoRefundData.PAYMENT_STATE_REFUND_INIT.equals(response.getOrderNoRefundData().getStatusCode()) || 
								DIOrderNoRefundData.PAYMENT_STATE_REFUNDED.equals(response.getOrderNoRefundData().getStatusCode()) )){
					
					outParams.put(RefundmentConstant.OPPOSITE_BATCHNO, response.getOrderNoRefundData().getPayplatformBatchNo()); // 对方退款订单号
					outParams.put(RefundmentConstant.REFUNDMENT_STATE, response.getOrderNoRefundData().getStatusCode()); // 退款状态
					outParams.put(ErrorCode.TIP_INFO,response.getOrderNoRefundData().getResult());//退款返回结果
				}else{
					MonitorData monitorData = response.getMonitorData();
					throw new ValidationException(monitorData.toString());
				}
			}else{
				throw new ValidationException("$_$第三方服务callRefund$_$无响应"+orderRefundRequest.toString());
			}
		}catch(Exception e){
			logger.error("$_$第三方服务callRefund$_$调用出现异常", e);
			throw new ValidationException("$_$第三方服务callRefund$_$调用出现异常");
		}
		return outParams;
	}
	
	/**
	 * 统一处理回调
	 * @param request
	 * @param platform
	 * @return
	 */
	public Map<String, Object> callRefundBack(HttpServletRequest origiRequest,
			Platform platform,PayRefundBatch payRefundBatch) {
		// 返回结果
		Map<String, Object> returned = new HashMap<String, Object>();
		
		// 原始请求参数
		Map<String, Object> origiRequestMap = new HashMap<String, Object>();
		
		// 远程调用请求对象
		DIOrderRefundBackRequest orderRefundBackRequest = new DIOrderRefundBackRequest();
		
		// 远程调用退款响应
		DIOrderNoRefundBackCallResponse response = null;
		try {
			String callUrl = "";
			String timeout = "30000";
			String extend = platform.getExtend();
			Integer cyle = 3;//默认失败重试次数3次
			String refundReadType = "0";// 默认是:0 reuqest.getParameter读取, 1 inputStream读取
			// 请求退款回调url
			if(StringUtils.isNotEmpty(extend)){
				JSONObject extJson = JSONObject.parseObject(extend);
				if(extJson.containsKey("callUrl") && StringUtils.isNotBlank(extJson.getString("callUrl"))){
					callUrl = extJson.getString("callUrl");
				}
				if(extJson.containsKey("timeout") && StringUtils.isNotBlank(extJson.getString("timeout"))){
					timeout = extJson.getString("timeout");
				}
				if(extJson.containsKey("cyle") && StringUtils.isNotBlank(extJson.getString("cyle"))){
					cyle = Integer.parseInt(extJson.getString("cyle"));
				}
				if(extJson.containsKey("refundReadType") && StringUtils.isNotBlank(extJson.getString("refundReadType"))){
					refundReadType = extJson.getString("refundReadType");
				}
			}
			
			origiRequest.setCharacterEncoding("UTF-8");
			if (StringUtils.isNotBlank(refundReadType) && refundReadType.equals("0")) {
				Enumeration<String> bodyParams = origiRequest.getParameterNames();
				while (bodyParams.hasMoreElements()) {
					String paraName = bodyParams.nextElement();
					origiRequestMap.put(paraName, origiRequest.getParameter(paraName));
				}
			} else {
				getRequestBody(origiRequestMap, origiRequest, "utf-8");
			}
			logger.info("The original data is : " + origiRequestMap.toString());
			
			
			orderRefundBackRequest.setReturnRequestMap(origiRequestMap);
			orderRefundBackRequest.setPlatform(platform);
			/**退款数据集*/
			List<ApplyPayRefundBatchDetail> dataDetailList = null;
			//请求支付系统的退款参数
			if(!StringUtils.isBlank(ObjectUtils.toString(payRefundBatch.getDetails()))){
				dataDetailList = JSONArray.parseArray(ObjectUtils.toString(payRefundBatch.getDetails()),ApplyPayRefundBatchDetail.class);
				for(ApplyPayRefundBatchDetail applyPayRefundBatchDetail :dataDetailList){
//	    			PaymentOrder paymentOrder = refundmentOrderService.queryOrderByPartnerOrderNo(applyPayRefundBatchDetail.getOrderno());
//	    			//退款交易结果集
//	    			applyPayRefundBatchDetail.setPaymentOrder(paymentOrder);
	    		}
	    	}
			if((null!=dataDetailList) && (dataDetailList.size()>0)){
				orderRefundBackRequest.setDataDetailList(dataDetailList);
			}
			
			DICallRequest paramData = new DICallRequest();
			paramData.setMethod("OrderRefundBackCall");//退款回调
			paramData.setOrderRefundBackRequest(orderRefundBackRequest);

			RequestDatas<DICallRequest> refundBackRequest = new RequestDatas<DICallRequest>();

			refundBackRequest.setAccessId(Long.parseLong(ACCESS_ID));
			refundBackRequest.setAccessPasswd(ACCESS_PWD);
			refundBackRequest.setAccessType(Long.parseLong(ACCESS_TYPE));
			refundBackRequest.setParamdata(paramData);
			refundBackRequest.setVersion("1");
			RequestClientInfo requestClientInfo = new RequestClientInfo();
			requestClientInfo.setClientUserIp(IpUtils.getRemoteAddr(origiRequest));
			requestClientInfo.setLocalReqIp(IpUtils.getLoaclAddr());
			requestClientInfo.setStartReqTime(new Date().getTime());
			refundBackRequest.setClientInfo(requestClientInfo);
			String body = JSON.toJSONString(refundBackRequest);
			String vString = body + refundBackRequest.getAccessId() + refundBackRequest.getAccessType() + refundBackRequest.getAccessPasswd()
					+ ACCESS_KEY;
			String verfiy = MD5Encrypt.encrypt(vString, "utf-8");

			Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("Content-Type", "application/json");
			headers.put("bodyEncode", "urlencode=utf-8");
			headers.put(NameFactory.request_head.accessId.name(), refundBackRequest.getAccessId());
			headers.put(NameFactory.request_head.accessType.name(), refundBackRequest.getAccessType());
			headers.put(NameFactory.request_head.accessPasswd.name(), refundBackRequest.getAccessPasswd());
			headers.put(NameFactory.request_head.accessVerify.name(), verfiy);
			
			// 发起调用
			IntHolder d  = new IntHolder();
//			String ret = HttpClient.post(callUrl, headers, URLEncoder.encode(body, "utf-8"),Integer.parseInt(timeout), "utf-8");
			String ret = callRemoteUrl(callUrl,headers, body, Integer.parseInt(timeout),"utf-8", cyle, JSONObject.parseObject(platform.getExtend()),d);
			
			EchoInfo<DIOrderNoRefundBackCallResponse> echoInfo = JSON.parseObject(ret, new TypeReference<EchoInfo<DIOrderNoRefundBackCallResponse>>() {});
			
			// 退款响应
			response = echoInfo.getData();
			
			if(null!=response){
				// 有退款响应,退款请求成功,处理中
				if(response.getMsgcode().equals("1")){
					returned.put(RefundmentConstant.REFUNDMENT_BATCH, payRefundBatch);//退款订单
					returned.put(RefundmentConstant.REFUND_SUCCESS_NUM, response.getSuccessNum());//退款成功笔数
					returned.put(RefundmentConstant.REFUND_RESULT_DETAILS, response.getOrderNoRefundBackCallDataList());
				}else{
					MonitorData monitorData = response.getMonitorData();
					throw new ValidationException(monitorData.toString());
				}
			}else{
				throw new ValidationException("$_$第三方服务callRefundBack$_$无响应,"+orderRefundBackRequest.toString());
			}
		} catch (Exception e) {
			logger.error("$_$第三方服务callRefund$_$调用出现异常", e);
			throw new ValidationException("$_$第三方服务callRefundBack$_$调用出现异常");
		}
		return returned;
	}
	
	/**
	 * 统一处理查询验证
	 * @param request
	 * @param platform
	 * @return
	 */
	public Map<String, Object> callRefundQuery(Map<String, Object> queryParams){
		Map<String, Object> outParams = ErrorCode.getErrorCode(1);
		
		// 请求
		DIOrderRefundQueryRequest orderRefundQueryRequest = new DIOrderRefundQueryRequest();
		
		// 响应
		DIOrderNoRefundQueryResponse response = new DIOrderNoRefundQueryResponse();
		try {
			
			PayRefundBatch payRefundBatch = (PayRefundBatch) queryParams.get(RefundmentConstant.REFUNDMENT_BATCH);
			Platform platform = (Platform) queryParams.get(RefundmentConstant.PAYMENT_PLATFORM);
			HttpServletRequest req = (HttpServletRequest) queryParams.get(RefundmentConstant.HTTP_REQUEST);
			
			String callUrl = "";
			String extend = platform.getExtend();
			String timeout = "30000";
			Integer cyle = 3;//默认失败重试次数3次
			// 请求查询退款url
			if(StringUtils.isNotEmpty(extend)){
				JSONObject extJson = JSONObject.parseObject(extend);
				if(StringUtils.isNotBlank(extJson.getString("callUrl"))){
					callUrl = extJson.getString("callUrl");
				}
				if(StringUtils.isNotBlank(extJson.getString("timeout"))){
					timeout = extJson.getString("timeout");
				}
				if(StringUtils.isNotBlank(extJson.getString("cyle"))){
					cyle = Integer.parseInt(extJson.getString("cyle"));
				}
			}
			
			orderRefundQueryRequest.setPlatform(platform);
			orderRefundQueryRequest.setPayRefundBatch(payRefundBatch);
			
			/**退款数据集*/
			List<ApplyPayRefundBatchDetail> dataDetailList = null;
			//请求支付系统的退款参数
			if(!StringUtils.isBlank(ObjectUtils.toString(payRefundBatch.getDetails()))){
				dataDetailList = JSONArray.parseArray(ObjectUtils.toString(payRefundBatch.getDetails()),ApplyPayRefundBatchDetail.class);
				for(ApplyPayRefundBatchDetail applyPayRefundBatchDetail :dataDetailList){
//	    			PaymentOrder paymentOrder = refundmentOrderService.queryOrderByPartnerOrderNo(applyPayRefundBatchDetail.getOrderno());
//	    			//退款交易结果集
//	    			applyPayRefundBatchDetail.setPaymentOrder(paymentOrder);
	    		}
	    	}
			if((null!=dataDetailList) && (dataDetailList.size()>0)){
				orderRefundQueryRequest.setDataDetailList(dataDetailList);
			}
			
			
			DICallRequest paramData = new DICallRequest();
			paramData.setMethod("OrderRefundQueryRequest");//退款查询
			paramData.setOrderRefundQueryRequest(orderRefundQueryRequest);

			RequestDatas<DICallRequest> request = new RequestDatas<DICallRequest>();
			request.setAccessId(Long.parseLong(ACCESS_ID));
			request.setAccessPasswd(ACCESS_PWD);
			request.setAccessType(Long.parseLong(ACCESS_TYPE));
			request.setParamdata(paramData);
			request.setVersion("1");
			
			RequestClientInfo requestClientInfo = new RequestClientInfo();
			requestClientInfo.setClientUserIp(IpUtils.getRemoteAddr(req));
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
//			String ret = HttpClient.post(callUrl, headers, URLEncoder.encode(body, "utf-8"),Integer.parseInt(timeout), "utf-8");
			String ret = callRemoteUrl(callUrl,headers, body, Integer.parseInt(timeout),"utf-8", cyle, JSONObject.parseObject(platform.getExtend()),d);
			
			EchoInfo<DIOrderNoRefundQueryResponse> echoInfo = JSON.parseObject(ret, new TypeReference<EchoInfo<DIOrderNoRefundQueryResponse>>() {});
			
			// 退款响应
			response = echoInfo.getData();
			
			if(null!=response){
				// 有响应,退款查询请求成功,处理中或初始化
				if( null!=response.getOrderNoRefundQueryDataList() && response.getOrderNoRefundQueryDataList().size()>0 ){
					outParams.put(RefundmentConstant.REFUND_RESULT_DETAILS, response.getOrderNoRefundQueryDataList());
				}else{
					MonitorData monitorData = response.getMonitorData();
					throw new ValidationException(monitorData.toString());
				}
			}else{
				throw new ValidationException("$_$第三方服务callRefundQuery$_$无响应"+orderRefundQueryRequest.toString());
			}
		} catch (Exception e) {
			logger.error("$_$第三方服务callRefund$_$调用出现异常", e);
			throw new ValidationException("$_$第三方服务callRefundQuery$_$调用出现异常");
		}
		return outParams;
	}
	
	/**
	 * 统一处理回调获取我方退款订单号
	 * @param request
	 * @param platform
	 * @return
	 */
	public String callRefundBackGetNo(HttpServletRequest origiRequest,Platform platform) {
		// 返回结果
		String orderNo = "";
		
		// 原始请求参数
		Map<String, Object> origiRequestMap = new HashMap<String, Object>();
		
		// 远程调用请求对象
		DIOrderRefundBackGetOrderNoRequest orderRefundBackGetOrderNoRequest = new DIOrderRefundBackGetOrderNoRequest();
		
		// 远程调用退款响应
		DIOrderNoRefundBackGetOrderNoResponse response = null;
		try {
			String callUrl = "";
			String timeout = "30000";
			String extend = platform.getExtend();
			Integer cyle = 3;//默认失败重试次数3次
			String refundReadType = "0";// 默认是:0 reuqest.getParameter读取, 1 inputStream读取
			// 请求退款回调url
			if(StringUtils.isNotEmpty(extend)){
				JSONObject extJson = JSONObject.parseObject(extend);
				if(StringUtils.isNotBlank(extJson.getString("callUrl"))){
					callUrl = extJson.getString("callUrl");
				}
				if(StringUtils.isNotBlank(extJson.getString("timeout"))){
					timeout = extJson.getString("timeout");
				}
				if(StringUtils.isNotBlank(extJson.getString("cyle"))){
					cyle = Integer.parseInt(extJson.getString("cyle"));
				}
				if(extJson.containsKey("refundReadType") && StringUtils.isNotBlank(extJson.getString("refundReadType"))){
					refundReadType = extJson.getString("refundReadType");
				}
			}
			
			
			origiRequest.setCharacterEncoding("UTF-8");
			if (StringUtils.isNotBlank(refundReadType) && refundReadType.equals("0")) {
				Enumeration<String> bodyParams = origiRequest.getParameterNames();
				while (bodyParams.hasMoreElements()) {
					String paraName = bodyParams.nextElement();
					origiRequestMap.put(paraName, origiRequest.getParameter(paraName));
				}
			} else {
				getRequestBody(origiRequestMap, origiRequest, "utf-8");
			}
			logger.info("The original data is : " + origiRequestMap.toString());
			
			
			orderRefundBackGetOrderNoRequest.setReqMap(origiRequestMap);
			
			DICallRequest paramData = new DICallRequest();
			paramData.setMethod("OrderRefundBackGetOrderNoRequest");//退款回调,获取我方退款单号
			paramData.setOrderRefundBackGetOrderNoRequest(orderRefundBackGetOrderNoRequest);

			RequestDatas<DICallRequest> refundBackRequest = new RequestDatas<DICallRequest>();

			refundBackRequest.setAccessId(Long.parseLong(ACCESS_ID));
			refundBackRequest.setAccessPasswd(ACCESS_PWD);
			refundBackRequest.setAccessType(Long.parseLong(ACCESS_TYPE));
			refundBackRequest.setParamdata(paramData);
			refundBackRequest.setVersion("1");
			RequestClientInfo requestClientInfo = new RequestClientInfo();
			requestClientInfo.setClientUserIp(IpUtils.getRemoteAddr(origiRequest));
			requestClientInfo.setLocalReqIp(IpUtils.getLoaclAddr());
			requestClientInfo.setStartReqTime(new Date().getTime());
			refundBackRequest.setClientInfo(requestClientInfo);
			String body = JSON.toJSONString(refundBackRequest);
			String vString = body + refundBackRequest.getAccessId() + refundBackRequest.getAccessType() + refundBackRequest.getAccessPasswd()
					+ ACCESS_KEY;
			String verfiy = MD5Encrypt.encrypt(vString, "utf-8");

			Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("Content-Type", "application/json");
			headers.put("bodyEncode", "urlencode=utf-8");
			headers.put(NameFactory.request_head.accessId.name(), refundBackRequest.getAccessId());
			headers.put(NameFactory.request_head.accessType.name(), refundBackRequest.getAccessType());
			headers.put(NameFactory.request_head.accessPasswd.name(), refundBackRequest.getAccessPasswd());
			headers.put(NameFactory.request_head.accessVerify.name(), verfiy);
			
			// 发起调用
			IntHolder d  = new IntHolder();
//			String ret = HttpClient.post(callUrl, headers, URLEncoder.encode(body, "utf-8"),Integer.parseInt(timeout), "utf-8");
			String ret = callRemoteUrl(callUrl,headers, body, Integer.parseInt(timeout),"utf-8", cyle, JSONObject.parseObject(platform.getExtend()),d);
			
			EchoInfo<DIOrderNoRefundBackGetOrderNoResponse> echoInfo = JSON.parseObject(ret, new TypeReference<EchoInfo<DIOrderNoRefundBackGetOrderNoResponse>>() {});
			
			// 退款响应
			response = echoInfo.getData();
			
			if(null!=response){
				// 有响应,退款请求成功,处理中
				if( null != response.getOrderNo() ){
					orderNo = response.getOrderNo();
				}else{
					MonitorData monitorData = response.getMonitorData();
					throw new ValidationException(monitorData.toString());
				}
			}else{
				throw new ValidationException("$_$第三方服务callRefundBack$_$无响应," + orderRefundBackGetOrderNoRequest.toString());
			}
		} catch (Exception e) {
			logger.error("$_$第三方服务callRefund$_$调用出现异常", e);
			throw new ValidationException("$_$第三方服务callRefundBack$_$调用出现异常");
		}
		return orderNo;
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
	private String callRemoteUrl(String callUrl, Map<String, Object> headers, String body, int timeout, String encode, int cycle, Map exts,IntHolder d)
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
						exts.containsKey(timout) ? Integer.parseInt(ObjectUtils.toString(exts.get(timout))) : timeout, "utf-8",d);
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
	public Map<String, Object> callPay(Map<String, Object> inParams) throws ValidationException{
		return null;
	}
	public Map<String, Object> callPayBack(HttpServletRequest origiRequest,Platform platform,PaymentOrder payOrder) throws ValidationException{
		return null;
	}
	public void callPayBackReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
	}
	public Map<String, Object> callPayQuery(Map<String, Object> queryParams){
		return null;
	}
	public String callPayGetOrderNoFromRequest(Platform platform,HttpServletRequest origRequest) {
		return null;
	}
	public String callPayBackGetOrderNoFromRequest(Platform platform,HttpServletRequest origRequest) {
		return null;
	}
}