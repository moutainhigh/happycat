package com.woniu.sncp.pay.web.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.woniu.pay.common.utils.RefundmentConstant;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.core.service.PaymentMerchantService;
import com.woniu.sncp.pay.core.service.RefundmentOrderService;
import com.woniu.sncp.pay.core.service.RefundmentService;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.refundment.RefundmentFacade;
import com.woniu.sncp.pay.core.service.refundment.process.RefundmentProcess;
import com.woniu.sncp.pay.repository.pay.PaymentMerchant;
import com.woniu.sncp.pojo.refund.PayRefundBatch;
import com.woniu.sncp.web.IpUtils;
import com.woniu.sncp.web.response.ResultResponse;

/**
 * 退款接口 回调类
 * 
 * e.g. 
 * 后面添加接口都以/refundment/backend/api开头
 * @author fuzl
 */
@Controller
@RequestMapping("/refundment/backend/api")
public class RefundmentBackendController extends ApiBaseController{
	
	@Resource
	private RefundmentFacade refundmentFacade;
	
	@Resource
	private RefundmentProcess standardRefundmentProcess;
	
	@Resource
	private RefundmentService refundmentService;
	
	@Resource
	private RefundmentProcess refundmentProcess;
	
	@Resource
	private RefundmentOrderService refundmentOrderService;
	
	@Resource
	private PaymentMerchantService paymentMerchantService;
    /**
     * 退款批次订单验证
     * 
     * @param batchNo 业务方订单号
     * @param request
     * @return
     */
    @RequestMapping("/verify")
    public @ResponseBody ResultResponse queryRefundmentBatch(@RequestParam(value="partnerbatchno") String pBatchNo,HttpServletRequest request){
    	
    	PayRefundBatch payRefundBatch = refundmentOrderService.queryRefundBatchByPartnerBatchNo(pBatchNo);
    	Map<String,Object> results = new HashMap<String,Object>();
    	if(payRefundBatch == null){
    		results = ErrorCode.getErrorCode(53203);
    		request.setAttribute("retCode", results.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", results.get(ErrorCode.TIP_INFO));
    		return new ResultResponse(ResultResponse.FAIL,"未查询到退款批次号",results);
    	}
    	results.put("partnerbatchno", pBatchNo);

    	Map<String, Object> retMap = refundmentFacade.checkRefundBatch(request,refundmentProcess,pBatchNo);
    	logger.info("退款批次订单验证返回结果:"+JsonUtils.toJson(retMap));
    	
    	results.put(ErrorCode.TIP_CODE, retMap.get(ErrorCode.TIP_CODE));//msgcode
    	results.put(ErrorCode.TIP_INFO, retMap.get(ErrorCode.TIP_INFO));// 操作结果
    	results.put(ErrorCode.ERROR_INFO, retMap.get(ErrorCode.ERROR_INFO));
    	results.put(RefundmentConstant.REFUNDMENT_STATE, retMap.get(RefundmentConstant.REFUNDMENT_STATE));
		results.put("batchno", payRefundBatch.getBatchNo());//计费侧批次号
		results.put("platformid", payRefundBatch.getPlatformId());//交易平台id
		
		
		if(!ObjectUtils.toString(retMap.get(ErrorCode.TIP_CODE)).equals("1")){
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return new ResultResponse(ResultResponse.FAIL,"退款批次订单验证返回失败",results);
    	}
		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    	return new ResultResponse(ResultResponse.SUCCESS,"已发校验请求",results);
    }
	
	/**
	 * 通用回调接口
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = { "/common/{merchantid}/{paymentid}" })
	public void commonRefundmentBackend(@PathVariable("merchantid") Long merchantId,@PathVariable("paymentid") Long paymentId,HttpServletRequest request, HttpServletResponse response) {

		AbstractPayment actualPayment = (AbstractPayment) refundmentService.findPaymentById(paymentId);
		Assert.notNull(actualPayment,"抽象退款平台配置不正确,查询平台为空,paymentId:" + paymentId);
		
		Map<String, Object> inParams = new HashMap<String, Object>();
		inParams.put("request", request);
		inParams.put("response", response);
		inParams.put("merchantid", merchantId);
		inParams.put("paymentId", paymentId);
		inParams.put("actualPayment", actualPayment);
		logRequestParams("commonPaymentBackend", request);
		refundmentFacade.processRefundmentBack(standardRefundmentProcess, inParams);
	}
	
	@RequestMapping("/test")
	public void testBackend(HttpServletRequest request, HttpServletResponse response) {
		String sign = (String)request.getParameter("sign");	
		String seed = "RxrO6S2OTAoeE3FKGE";
		
		Enumeration<String> requestParams = request.getParameterNames();
		Map<String, String> treeMap = new TreeMap<String, String>();
		StringBuffer logStr = new StringBuffer();
		logStr.append("\n++++++参数 开始++++++\n");
		logStr.append("requestIp=" + IpUtils.getRemoteAddr(request));
		logStr.append("\n");
		
		while(requestParams.hasMoreElements()){
			String key = requestParams.nextElement();
			String value = request.getParameter(key);
			treeMap.put(key, value);
			logStr.append(key + "=" + value);
			logStr.append("\n");
		}
		
		logStr.append("++++++参数 结束++++++");
		logger.info(logStr.toString());
		
		StringBuffer sb = new StringBuffer();
		treeMap.remove("sign");

		Iterator<String> iter = treeMap.keySet().iterator();
		while (iter.hasNext()) {
			String name = (String) iter.next();
			sb.append(name).append(treeMap.get(name));
		}
		sb.append(seed);
		String localSign = MD5Encrypt.encrypt(sb.toString());
		
		String message = "failed";
//		if(localSign.equalsIgnoreCase(sign)){
			message = "success";
//		} else {
//			logger.error("原串:"+sb.toString()+",local sign:"+localSign + ",sign:"+sign);
//		}
		
		PrintWriter writer = null;
		try {
			if (logger.isInfoEnabled())
				logger.info("返回给退款平台的信息：" + message);
			
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
	}

	
	@RequestMapping("/agent/back")
	public void agentBackend(HttpServletRequest request, HttpServletResponse response) {
		String sign = (String)request.getParameter("sign");	
		String seed = "654321";
		
		Enumeration<String> requestParams = request.getParameterNames();
		Map<String, String> treeMap = new TreeMap<String, String>();
		StringBuffer logStr = new StringBuffer();
		logStr.append("\n++++++参数 开始++++++\n");
		logStr.append("requestIp=" + IpUtils.getRemoteAddr(request));
		logStr.append("\n");
		
		while(requestParams.hasMoreElements()){
			String key = requestParams.nextElement();
			String value = request.getParameter(key);
			treeMap.put(key, value);
			logStr.append(key + "=" + value);
			logStr.append("\n");
		}
		
		logStr.append("++++++参数 结束++++++");
		logger.info(logStr.toString());
		
		StringBuffer sb = new StringBuffer();
		treeMap.remove("sign");

		Iterator<String> iter = treeMap.keySet().iterator();
		while (iter.hasNext()) {
			String name = (String) iter.next();
			sb.append(name).append(treeMap.get(name));
		}
		sb.append(seed);
		String localSign = MD5Encrypt.encrypt(sb.toString());
		
		Map<String,Object> retMap = new HashMap<String, Object>();
		retMap.put("status", "200");
		PrintWriter writer = null;
		try {
			if (logger.isInfoEnabled())
				logger.info("返回给退款平台的信息：" + JsonUtils.toJson(retMap));
			
			writer = response.getWriter();
			writer.print(JsonUtils.toJson(retMap));
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
	}
	
	/**
	 * eai退款通道回调接口
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = { "/eai" })
	public void eaiRefundPaymentBackend(HttpServletRequest request, HttpServletResponse response) {

		PrintWriter writer = null;
		try {
			String sign = (String)request.getParameter("sign");
			String partnerorderno = (String)request.getParameter("partnerbatchno");
			
			Enumeration<String> requestParams = request.getParameterNames();
			Map<String, String> treeMap = new TreeMap<String, String>();
			StringBuffer logStr = new StringBuffer();
			logStr.append("\n++++++参数 开始++++++\n");
			logStr.append("requestIp=" + IpUtils.getRemoteAddr(request));
			logStr.append("\n");
			
			while(requestParams.hasMoreElements()){
				String key = requestParams.nextElement();
				String value = request.getParameter(key);
				treeMap.put(key, value);
				logStr.append(key + "=" + value);
				logStr.append("\n");
			}
			
			logStr.append("++++++参数 结束++++++");
			logger.info(logStr.toString());
			
			PayRefundBatch refundBatch = refundmentOrderService.queryRefundBatchByPartnerBatchNo(partnerorderno);
			logger.info("业务订单号:"+refundBatch.getPartnerBatchNo());
			PaymentMerchant payemntMerchnt = paymentMerchantService.queryPayemntMerchnt(refundBatch.getPartnerId());
			logger.info("业务商户号:"+refundBatch.getPartnerId());
			//默认返回这个
			String message = "fail";
			
			StringBuffer sb = new StringBuffer();
			treeMap.remove("sign");

			Iterator<String> iter = treeMap.keySet().iterator();
			while (iter.hasNext()) {
				String name = (String) iter.next();
				sb.append(name).append(treeMap.get(name));
			}
			sb.append(payemntMerchnt.getMerchantKey());
			String localSign = MD5Encrypt.encrypt(sb.toString());
			
			if(localSign.equalsIgnoreCase(sign)){
				message = "success";
			} else {
				logger.error("原串:"+sb.toString()+",local sign:"+localSign + ",sign:"+sign);
			}
			
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
	}
}
