package com.woniu.sncp.pay.web.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.woniu.sncp.json.JsonUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.lang.DateUtil;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.core.service.PaymentOrderService;
import com.woniu.sncp.pay.core.service.PaymentService;
import com.woniu.sncp.pay.core.service.PlatformService;
import com.woniu.sncp.pay.core.service.payment.Constant;
import com.woniu.sncp.pay.core.service.payment.PaymentFacade;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.paypal.PaypalPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;

@Controller
@RequestMapping("/payment/front/api")
public class PaymentFontController extends ApiBaseController{
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private PaymentService paymentService;
	
	@Resource
	protected PlatformService platformService;
	
	@Resource
	private PaymentFacade paymentFacade;
	
	private final static String Errorurl = "/payment/error";
	
	/**
	 * 运维监控这个地址，勿删
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/test")
	public String testFront(HttpServletRequest request) {
		String orderNo = request.getParameter("orderNo");
		request.setAttribute("orderNo", orderNo);
		return "/payment/test/front";
	}


	private void fontDispatch(String orderNo,HttpServletRequest request, HttpServletResponse response) throws Exception{
		PaymentOrder paymentOrder=null;
		if(StringUtils.isNotBlank(orderNo)){
			paymentOrder=		paymentOrderService.queryOrder(orderNo);
		}


		if(paymentOrder!=null){
			String mapping=String.format("/payment/front/api/common/%s/%s",paymentOrder.getMerchantId()+"",paymentOrder.getPayPlatformId()+"");
			request.getRequestDispatcher(mapping).forward(request,response);

		}else{
			logger.info("找不到定单号:{}",orderNo);

			try{
				response.getWriter().write("{\"ResultCode\":-1}");
			}catch (Exception e){
				logger.error("",e);
			}

		}
	}

	@RequestMapping(value = { "/codapay" })
	public void codapay(HttpServletRequest request, HttpServletResponse response) throws Exception{
		logger.info("codapay 回调  queryString:{}", request.getQueryString());

		logRequestParams("(" + request.getMethod() + ")"
				+ request.getRequestURL().toString(), request);


		String orderNo=request.getParameter("OrderId");
		fontDispatch(orderNo,request,response);

	}
	@RequestMapping(value = { "/bluepay" })
	public void bluepay(HttpServletRequest request, HttpServletResponse response) throws Exception{
		logger.info("bluepay 回调  queryString:{}", request.getQueryString());

		logRequestParams("(" + request.getMethod() + ")"
				+ request.getRequestURL().toString(), request);

		String orderNo=request.getParameter("t_id");
		fontDispatch(orderNo,request,response);
	}
	@RequestMapping(value = { "/molpay" })
	public void molpay(HttpServletRequest request, HttpServletResponse response) throws Exception{
		logger.info("molpay 回调  queryString:{}", request.getQueryString());

		logRequestParams("(" + request.getMethod() + ")"
				+ request.getRequestURL().toString(), request);

		String orderNo=request.getParameter("referenceId");
		fontDispatch(orderNo,request,response);
	}
	
	/**
	 * 前台返回 - 显示订单处理结果
	 * @return
	 */
	@RequestMapping(value="/common/{merchantid}/{paymentid}")
	public String commonPaymentFont(@PathVariable("merchantid") Long merchantId,@PathVariable("paymentid") Long paymentId,HttpServletRequest request) {
		logRequestParams("commonPaymentFont", request);
		
		Platform platform = platformService.queryPlatform(merchantId, paymentId);
		platformService.validatePaymentPlatform(platform);
		request.setAttribute(PaymentConstant.PAYMENT_PLATFORM, platform);
		
		AbstractPayment actualPayment = (AbstractPayment) paymentService.findPaymentById(paymentId);
		Assert.notNull(actualPayment,"抽象支付平台配置不正确,查询平台为空,paymentId:" + paymentId);
		
		if (actualPayment instanceof PaypalPayment) {
			platform = platformService.queryPlatform(Long.valueOf(merchantId), Long.valueOf(paymentId));
			platformService.validatePaymentPlatform(platform);
			
			request.setAttribute(PaymentConstant.PAYMENT_PLATFORM, platform);
			request.setAttribute(PaymentConstant.MERCHANT_ID, merchantId);
		}
		
		Map<String,Object> result = paymentFacade.processPaymentFont(actualPayment, request);
		PaymentOrder order = (PaymentOrder) result.get(PaymentConstant.PAYMENT_ORDER);
		if(order == null){
			request.setAttribute("msg", "订单不存在");
    		request.setAttribute("retCode", ErrorCode.getErrorCode(54208).get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", ErrorCode.getErrorCode(54208).get(ErrorCode.TIP_INFO));
			return Errorurl;
		}
		
		String directUrl = getFrontUrl(order);
		if(StringUtils.isEmpty(directUrl)){
			request.setAttribute("msg", "页面跳转地址未配置");
			request.setAttribute("retCode", ErrorCode.getErrorCode(56101).get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", ErrorCode.getErrorCode(56101).get(ErrorCode.TIP_INFO));
			return Errorurl;
		}
		
		request.setAttribute("msg", result.get(ErrorCode.TIP_INFO));
		if (1 != (Integer)result.get(ErrorCode.TIP_CODE)){
			if(directUrl.contains("?")){
				return "redirect:"+directUrl+"&errMsg="+result.get(ErrorCode.TIP_CODE)+result.get(ErrorCode.TIP_INFO);
			}else{
				return "redirect:"+directUrl+"?errMsg="+result.get(ErrorCode.TIP_CODE)+result.get(ErrorCode.TIP_INFO);
			}
		}
		
		setAttributes(request, result);
		
		String orderNo = order.getPaypartnerOtherOrderNo();
		if(StringUtils.isBlank(orderNo)){
			orderNo = order.getOrderNo();
		}
		
		String ext = order.getInfo();
		String openId = "";
		if(StringUtils.isNotBlank(ext)){
			JSONObject extend = JSONObject.parseObject(ext);
			if(extend.containsKey("openid") && StringUtils.isNotBlank(extend.getString("openid"))){
				openId = extend.getString("openid");
			}
		}
		
		String orderStatus = (String)request.getAttribute(Constant.ORDER_FRONT_CALLBACK_STATUS);
		List<String> param=new ArrayList<>();
		if(StringUtils.isNotBlank(orderNo)) {
			param.add("orderNo="+ orderNo);	
		}
		if(StringUtils.isNotBlank(openId)) {
			param.add("openId="+ openId);	
		}
		if(StringUtils.isNotBlank(orderStatus)) {
			param.add("orderStatus="+  orderStatus);	
		}
		
		if(order.getAid() != null) {
			param.add("aid="+  order.getAid());	
		}
		param.add("platformId="+  paymentId);	
		
		String queryStr = StringUtils.join(param, "&");
		
		if(directUrl.contains("?")){
			return "redirect:"+directUrl + queryStr;
		}else{
			return "redirect:"+directUrl+"?" + queryStr;
		}
	}
	
	private final static String PAGE_PAYMENT_SUCCESS = "/payment/payment_success";
	private final static String PAGE_PAYMENT_PROCESS = "/payment/payment_process";
	@Resource
	private PaymentOrderService paymentOrderService;
	@RequestMapping("/common")
	public String imprestQueryOrder(HttpServletRequest request){
		String orderNo = request.getParameter("orderNo");
		String merchantId = request.getParameter("merchantid");
		PaymentOrder impOrder = paymentOrderService.queryOrderByPartnerOrderNo(orderNo,Long.parseLong(merchantId));
		if(impOrder == null){
			request.setAttribute("msg", "未查询到订单");
			request.setAttribute("retCode", ErrorCode.getErrorCode(54208).get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", ErrorCode.getErrorCode(54208).get(ErrorCode.TIP_INFO));
			return PAGE_PAYMENT_SUCCESS;
		}
		request.setAttribute("msg", "已支付成功");
		request.setAttribute(PaymentConstant.PAYMENT_ORDER, impOrder);
		request.setAttribute("retCode", ErrorCode.getErrorCode(1).get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", ErrorCode.getErrorCode(1).get(ErrorCode.TIP_INFO));
		return PAGE_PAYMENT_SUCCESS;
	}
	
	@RequestMapping("/process")
	public String imprestProcessOrder(HttpServletRequest request){
		String orderNo = request.getParameter("orderNo");
		String merchantId = request.getParameter("merchantid");
		String orderno = request.getParameter("orderno");
		
		Map<String,Object> retMap = new HashMap<String, Object>();
		PaymentOrder paymentOrder = paymentOrderService.queryOrderByPartnerOrderNo(orderno,Long.parseLong(merchantId));
		if(paymentOrder == null){
			request.setAttribute("msg", "未查询到订单");
			request.setAttribute("retCode", ErrorCode.getErrorCode(54208).get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", ErrorCode.getErrorCode(54208).get(ErrorCode.TIP_INFO));
			return PAGE_PAYMENT_PROCESS;
		}
		retMap.put("paymentOrder", paymentOrder);
		request.setAttribute("msg", "已支付成功");
		request.setAttribute("orderNo", orderNo);
		request.setAttribute("money", paymentOrder.getMoney());
		request.setAttribute("createDate", DateUtil.parseDate2Str(paymentOrder.getCreate(), DateUtil.DATE_FORMAT_DATETIME));
		request.setAttribute(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		request.setAttribute("infoMap", retMap);
		request.setAttribute("retCode", ErrorCode.getErrorCode(1).get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", ErrorCode.getErrorCode(1).get(ErrorCode.TIP_INFO));
		return PAGE_PAYMENT_PROCESS;
	}
	
	private String getFrontUrl(PaymentOrder order) {
		return order.getPaypartnerFrontCall();
	}
	
	private void setAttributes(HttpServletRequest request,
			Map<String, Object> result) {
		request.setAttribute(PaymentConstant.PAYMENT_ORDER, result.get(PaymentConstant.PAYMENT_ORDER));
		request.setAttribute("userName", result.get("userName"));
		request.setAttribute("cardTypeName", result.get("cardTypeName"));
	}
}
