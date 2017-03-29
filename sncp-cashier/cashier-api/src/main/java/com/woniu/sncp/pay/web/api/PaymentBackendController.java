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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.service.PaymentMerchantService;
import com.woniu.sncp.pay.core.service.PaymentOrderService;
import com.woniu.sncp.pay.core.service.PaymentService;
import com.woniu.sncp.pay.core.service.payment.PaymentFacade;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.process.PaymentProcess;
import com.woniu.sncp.pay.repository.pay.PaymentMerchant;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.web.IpUtils;
import com.woniu.sncp.web.response.ResultResponse;

/**
 * 支付接口 回调类
 * 
 * e.g. /paybackend/api/xxx 支付宝直连回调接口
 * 
 * 后面添加接口都以/payment/backend/api开头
 * 
 * @author luzz
 *
 */
@Controller
@RequestMapping("/payment/backend/api")
public class PaymentBackendController extends ApiBaseController{
	
	@Resource
	private PaymentFacade paymentFacade;
	
	@Resource
	private PaymentProcess standardPaymentProcess;
	
	@Resource
	private PaymentService paymentService;
	
	@Resource
	private PaymentProcess paymentProcess;
	
	@Resource
	private PaymentOrderService paymentOrderService;
	
	@Resource
	private PaymentMerchantService paymentMerchantService;
	
    /**
     * 支付订单验证
     * 
     * @param orderNo 订单号
     * @param request
     * @return
     */
    @RequestMapping("/verify")
    public @ResponseBody ResultResponse queryPaymentOrder(@RequestParam(value="orderno") String orderNo,HttpServletRequest request){
    	
    	Map<String, Object> retMap = paymentFacade.checkOrder(paymentProcess,orderNo);
    	logger.info("订单验证返回结果:"+JsonUtils.toJson(retMap));
    	
    	PaymentOrder queryOrder = (PaymentOrder) retMap.get(PaymentConstant.PAYMENT_ORDER);
    	Map<String,Object> results = new HashMap<String,Object>();
    	results.put("orderno", orderNo);
    	
    	if(queryOrder == null){
    		return new ResultResponse(ResultResponse.FAIL,"未查询到订单号",results);
    	}
    	
    	results.put(ErrorCode.ERROR_INFO, retMap.get(ErrorCode.ERROR_INFO));
    	results.put(ErrorCode.TIP_INFO, retMap.get(ErrorCode.TIP_INFO));
    	results.put("partnerorderno", queryOrder.getPartnerOrderNo());
    	results.put("orderno", queryOrder.getOrderNo());
    	results.put("aid", queryOrder.getAid());
    	results.put("platformid", queryOrder.getPlatformId());
    	results.put("imprestmode", queryOrder.getImprestMode());
    	results.put("money", queryOrder.getMoney());
    	results.put("paystate", queryOrder.getPaymentState());
    	results.put("impreststate", queryOrder.getImprestState());
    	
    	return new ResultResponse(ResultResponse.SUCCESS,"已发校验请求",results);
    }
	
	/**
	 * 通用回调接口
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = { "/common/{merchantid}/{paymentid}" })
	public void commonPaymentBackend(@PathVariable("merchantid") Long merchantId,@PathVariable("paymentid") Long paymentId,HttpServletRequest request, HttpServletResponse response) {

		AbstractPayment actualPayment = (AbstractPayment) paymentService.findPaymentById(paymentId);
		Assert.notNull(actualPayment,"抽象支付平台配置不正确,查询平台为空,paymentId:" + paymentId);
		
		Map<String, Object> inParams = new HashMap<String, Object>();
		inParams.put("request", request);
		inParams.put("response", response);
		inParams.put("merchantid", merchantId);
		inParams.put("paymentId", paymentId);
		inParams.put("abstractPayment", actualPayment);
		logRequestParams("commonPaymentBackend", request);
		paymentFacade.processPaymentBack(standardPaymentProcess, inParams);
	}
	
	/**
	 * 中国银行信用卡回调，此地址需要配置到对方平台
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = {"/chinabank"})
	public void chinabankPaymentBackend(HttpServletRequest request,HttpServletResponse response){
		String orderNo = request.getParameter("orderNo");
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder,"订单不存在,orderNo:" + orderNo);
		
		Long merchantId = paymentOrder.getMerchantId();
		Long paymentId = paymentOrder.getPlatformId();
		
		AbstractPayment actualPayment = (AbstractPayment) paymentService.findPaymentById(paymentId);
		Assert.notNull(actualPayment,"抽象支付平台配置不正确,查询平台为空,paymentId:" + paymentId);
		
		Map<String, Object> inParams = new HashMap<String, Object>();
		inParams.put("request", request);
		inParams.put("response", response);
		inParams.put("merchantid", merchantId);
		inParams.put("paymentId", paymentId);
		inParams.put("abstractPayment", actualPayment);
		logRequestParams("commonPaymentBackend", request);
		paymentFacade.processPaymentBack(standardPaymentProcess, inParams);
	}
	
	@SuppressWarnings("unused")
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
	
	
	/**
	 * 走消息推送通道回调接口
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = { "/message/push" })
	public void messagePushPaymentBackend(HttpServletRequest request, HttpServletResponse response) {

		PrintWriter writer = null;
		try {
			String sign = (String)request.getParameter("sign");
			String partnerorderno = (String)request.getParameter("partnerorderno");
			
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
			
			PaymentOrder paymentOrder = paymentOrderService.queryOrderByPartnerOrderNo(partnerorderno);
			
			PaymentMerchant payemntMerchnt = paymentMerchantService.queryPayemntMerchnt(paymentOrder.getMerchantId());
			
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
				request.setAttribute("retCode", "1");
				request.setAttribute("retMsg", "操作成功");
			} else {
				logger.error("原串:"+sb.toString()+",local sign:"+localSign + ",sign:"+sign);
				request.setAttribute("retCode", "0");
				request.setAttribute("retMsg", "操作失败");
			}
			
			if (logger.isInfoEnabled())
				logger.info("返回给支付平台的信息：" + message);
			
			writer = response.getWriter();
			writer.print(message);
			writer.flush();
		} catch (IOException ex) {
			logger.error("返回给对方跳转的地址失败", ex);
			request.setAttribute("retCode", "-1");
			request.setAttribute("retMsg", "操作异常");
		} finally {
			try {
				writer.close();
				writer = null;
			} catch (Exception e) {
				request.setAttribute("retCode", "-1");
				request.setAttribute("retMsg", "操作异常");
				e.printStackTrace();
			}
		} 
	}
	
	/**
	 * gbk回调处理
	 * @param merchantId
	 * @param paymentId
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = { "/common/gb/{merchantid}/{paymentid}" })
	public void commonPaymentBackendGB(@PathVariable("merchantid") Long merchantId,@PathVariable("paymentid") Long paymentId,HttpServletRequest request, HttpServletResponse response) {

		AbstractPayment actualPayment = (AbstractPayment) paymentService.findPaymentById(paymentId);
		Assert.notNull(actualPayment,"抽象支付平台配置不正确,查询平台为空,paymentId:" + paymentId);
		
		Map<String, Object> inParams = new HashMap<String, Object>();
		inParams.put("request", request);
		inParams.put("response", response);
		inParams.put("merchantid", merchantId);
		inParams.put("paymentId", paymentId);
		inParams.put("abstractPayment", actualPayment);
		logRequestParams("commonPaymentBackend", request);
		paymentFacade.processPaymentBack(standardPaymentProcess, inParams);
	}
}
