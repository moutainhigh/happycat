package com.woniu.sncp.pay.web.api;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.sncp.lang.DateUtil;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.core.service.GameManagerService;
import com.woniu.sncp.pay.core.service.PaymentOrderService;
import com.woniu.sncp.pay.core.service.payment.PaymentFacade;
import com.woniu.sncp.pay.core.service.payment.platform.huifubao.HuifubaoPayment;
import com.woniu.sncp.pay.core.service.payment.platform.shenzpay.ShenzpayDPPayment;
import com.woniu.sncp.pay.core.service.sms.SmsService;
import com.woniu.sncp.pojo.game.GameServer;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.tools.IpUtils;
import com.woniu.sncp.web.response.ResultResponse;

/**
 * 
 * <p>descrption: wap收银台对外支付接口</p>
 * 
 * @author fuzl
 * @date   2016年10月26日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@Controller
@RequestMapping("/wap/api")
public class PaymentWapController extends ApiBaseController{
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
    private PaymentFacade paymentFacade;
	@Resource
	private PaymentOrderService paymentOrderService;
	@Resource
	private SmsService smsService;
	
    private final static String REDIRECT_PAY_PAGE = "/payment/payment_order";
    private final static String ORDER_ERROR_PAGE = "/payment/error";
    private final static String PAYMENT_SUCCESS_PAGE = "/payment/payment_success";
    private final static String PAYMENT_PROCESS_PAGE = "/payment/payment_process";
    private final static String FCB_SMS_TYPE="CASHIER-FCBSMS";
    
    /**
     * 支付接口
     * 
     * @param orderNo 订单号
     * @param platformId 支付平台id
     * @param money 支付金额，单位元
     * @param clientIp 客服端ip
     * @param bankCd 银行代码
     * @param imprestMode 充值方式
     * @param productName 商品名称
     * @param gameId 游戏Id
     * @param account 蜗牛通行证
     * @param ext 扩展
     * @param request
     * @return
     */
    @RequestMapping("/dp")
	public String payment(
			@RequestParam(value="orderno") String orderNo,
			@RequestParam(value="merchantid") String merchantId,
			@RequestParam(value="gameid",required=false) String gameId,
			@RequestParam(value="account",required=false) String account,
			@RequestParam(value="platformid") String platformId,
			@RequestParam(value="money") String money,
			@RequestParam(value="clientip") String clientIp,
			@RequestParam(value="bankcd",required=false) String bankCd,
			@RequestParam(value="bankCardType",required=false) String bankCardType,
			@RequestParam(value="mode",required=false) String mode,
			@RequestParam(value="cardtype",required=false) String cardtype,
			@RequestParam(value="productname") String productName,
			@RequestParam(value="backendurl") String backendurl,
			@RequestParam(value="fontendurl",required=false) String fontendurl,
			@RequestParam(value="ext",required=false) String ext,
			@RequestParam(value="stagePlan",required=false) String stagePlan,
			@RequestParam(value="stageNum",required=false) String stageNum,
			@RequestParam(value="yueMoney",required=false) String yueMoney,
			@RequestParam(value="yueCurrency",required=false) String yueCurrency,
			@RequestParam(value="fcbsmscode",required=false) String smscode,
			@RequestParam(value="currency",required=false) String moneyCurrency,
			@RequestParam(value="body",required=false) String body,
			@RequestParam(value="goodsDetail",required=false) String goodsDetail,
			@RequestParam(value="terminalType",required=false) String terminalType,
			@RequestParam(value="timeoutExpress",required=false) String timeoutExpress,
			@RequestParam(value="aid",required=false) String aid,
			HttpServletRequest request){
    	
    	Map<String,Object> retMap = new HashMap<String, Object>();
    	
    	if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		logger.error("订单号为空或长度超出,account:"+account);
    		request.setAttribute("msg", "订单号为空或长度超出");
    		retMap = ErrorCode.getErrorCode(56007);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return ORDER_ERROR_PAGE;
    	}
    	
    	if(StringUtils.isBlank(money) || "0".equals(StringUtils.trim(money))){
    		logger.error("订单金额不可以为空或零,account:"+account);
    		request.setAttribute("msg", "money为空或等于零");
    		retMap = ErrorCode.getErrorCode(56008);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return ORDER_ERROR_PAGE;
    	}
    	
    	if(StringUtils.isBlank(backendurl) || backendurl.indexOf("http") != 0){
    		logger.error("回调地址不可以为空,account:"+account);
    		request.setAttribute("msg", "backendurl为空或格式错误");
    		retMap = ErrorCode.getErrorCode(56009);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return ORDER_ERROR_PAGE;
    	}
    	
    	//1.创建订单
		HashMap<String, Object> exMap = new HashMap<String, Object>();
		exMap.put("defaultbank", bankCd);
		exMap.put("backendurl", backendurl);
		exMap.put("fontendurl", fontendurl);
		exMap.put("cardtype", cardtype);
		//增加信用卡分期支付参数
		exMap.put("stagePlan", stagePlan);
		exMap.put("stageNum", stageNum);
		//增加银行卡类型‘bankCardType
		exMap.put("bankCardType", bankCardType);
		
		//增加扩展参数设置
		exMap.put("ext", ext);
		
		try{
			mode = paymentOrderService.getOrderMode(mode, bankCd);
			//判断是否是组合支付
	    	if(StringUtils.isNotBlank(yueMoney) && NumberUtils.toFloat(yueMoney) > 0){
	    		//验证翡翠身份
	        	boolean isFcb = ApiBaseController.hasFcbIdentity(request);
	    		if(!isFcb){
	    			logger.error("未拿到翡翠身份");
	        		request.setAttribute("msg", "未拿到翡翠身份");
	        		return ORDER_ERROR_PAGE;
	    		}
	    		//获得翡翠账号、翡翠号码
	    		//String fcbAccount = ApiBaseController.getFcbAccount(request);
	    		String fcbPhone = ApiBaseController.getFcbPhone(request);
	        		
	    		Map<String, Object> smsMap = smsService.validateSmsCode(fcbPhone, FCB_SMS_TYPE, smscode);
	    		String code = ObjectUtils.toString(smsMap.get("code"));
	    		if(!"20".equals(code)){
	    			String message = ObjectUtils.toString(smsMap.get("message"));
	    			request.setAttribute("msg", message);
	        		return ORDER_ERROR_PAGE;
	    		}
	    		
    			retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
    					yueCurrency,yueMoney,StringUtils.trim(productName),account,NumberUtils.toLong(gameId),  
						mode, clientIp, exMap,body,goodsDetail,terminalType,timeoutExpress);
	    			
	    	}else{
	    		retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
						StringUtils.trim(productName),NumberUtils.toLong(aid),account,"0",NumberUtils.toLong(gameId),  
						mode, clientIp, exMap,moneyCurrency,body,goodsDetail,terminalType,timeoutExpress);
	    	}
		} catch (Exception e){
			request.setAttribute("msg", e.getMessage());
			retMap = ErrorCode.getErrorCode(56099);
		}
		
		logger.info("支付订单信息,"+retMap);
    	
    	//2.跳转到第三方支付页面
    	if (retMap != null && StringUtils.equalsIgnoreCase(ObjectUtils.toString(retMap.get(ErrorCode.TIP_CODE)), "1")) {
    		request.setAttribute("infoMap", retMap);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
            return REDIRECT_PAY_PAGE;
        }
    	
    	String referer = request.getParameter("referer");
    	request.setAttribute("referer", referer);
    	request.setAttribute("msg", retMap.get(ErrorCode.TIP_INFO));
    	request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    	return ORDER_ERROR_PAGE;
    }
    /**
     * 支付接口
     * 
     * @param orderNo 订单号
     * @param platformId 支付平台id
     * @param money 支付金额，单位元
     * @param clientIp 客服端ip
     * @param bankCd 银行代码
     * @param imprestMode 充值方式
     * @param productName 商品名称
     * @param gameId 游戏Id
     * @param account 蜗牛通行证
     * @param ext 扩展
     * @param request
     * @return
     */
    @RequestMapping("/dp/json")
	public @ResponseBody ResultResponse paymentDpJson(
			@RequestParam(value="orderno") String orderNo,
			@RequestParam(value="merchantid") String merchantId,
			@RequestParam(value="gameid",required=false) String gameId,
			@RequestParam(value="account",required=false) String account,
			@RequestParam(value="platformid") String platformId,
			@RequestParam(value="money") String money,
			@RequestParam(value="clientip") String clientIp,
			@RequestParam(value="bankcd",required=false) String bankCd,
			@RequestParam(value="mode",required=false) String mode,
			@RequestParam(value="cardtype",required=false) String cardtype,
			@RequestParam(value="productname") String productName,
			@RequestParam(value="backendurl") String backendurl,
			@RequestParam(value="fontendurl",required=false) String fontendurl,
			@RequestParam(value="ext",required=false) String ext,
			@RequestParam(value="stagePlan",required=false) String stagePlan,
			@RequestParam(value="stageNum",required=false) String stageNum,
			@RequestParam(value="payexpired",required=false) String payExpired,
			@RequestParam(value="currency",required=false) String moneyCurrency,
			@RequestParam(value="body",required=false) String body,
			@RequestParam(value="goodsDetail",required=false) String goodsDetail,
			@RequestParam(value="terminalType",required=false) String terminalType,
			@RequestParam(value="timeoutExpress",required=false) String timeoutExpress,
			@RequestParam(value="aid",required=false) String aid,
			HttpServletRequest request){
    	
    	Map<String,Object> retMap = new HashMap<String, Object>();
    	
    	if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		logger.error("订单号为空或长度超出,account:"+account);
    		retMap = ErrorCode.getErrorCode(56007);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return new ResultResponse(ResultResponse.FAIL,"订单号为空或长度超出",retMap);
    	}
    	
    	if(StringUtils.isBlank(money) || "0".equals(StringUtils.trim(money))){
    		logger.error("订单金额不可以为空或零,account:"+account);
    		retMap = ErrorCode.getErrorCode(56008);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return new ResultResponse(ResultResponse.FAIL,"订单金额不可以为空或零",retMap);
    	}
    	
    	if(StringUtils.isBlank(backendurl) || backendurl.indexOf("http") != 0){
    		logger.error("回调地址不可以为空,account:"+account);
    		retMap = ErrorCode.getErrorCode(56009);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return new ResultResponse(ResultResponse.FAIL,"backendurl为空或格式错误",retMap);
    	}
    	
    	//1.创建订单
		HashMap<String, Object> exMap = new HashMap<String, Object>();
		exMap.put("defaultbank", bankCd);
		exMap.put("backendurl", backendurl);
		exMap.put("fontendurl", fontendurl);
		exMap.put("cardtype", cardtype);
		//增加信用卡分期支付参数
		exMap.put("stagePlan", stagePlan);
		exMap.put("stageNum", stageNum);
		exMap.put("payexpired", payExpired);
		exMap.put("ext", ext);
		
		try{
			mode = paymentOrderService.getOrderMode(mode, bankCd);
			retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
														StringUtils.trim(productName),NumberUtils.toLong(aid),account,"0",NumberUtils.toLong(gameId),  
														mode, clientIp, exMap,moneyCurrency,body,goodsDetail,terminalType,timeoutExpress);
		} catch (Exception e){
			request.setAttribute("msg", e.getMessage());
			retMap = ErrorCode.getErrorCode(56099);
		}
		
		logger.info("支付订单信息,"+retMap);
    	
    	//2.跳转到第三方支付页面
    	if (retMap != null && StringUtils.equalsIgnoreCase(ObjectUtils.toString(retMap.get(ErrorCode.TIP_CODE)), "1")) {
    		request.setAttribute("infoMap", retMap);
    		retMap.put("paymentPlatform", null);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
            return new ResultResponse(ResultResponse.SUCCESS,"支付请求成功",retMap);
        }
    	request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
		return new ResultResponse(ResultResponse.FAIL,String.valueOf(retMap.get(ErrorCode.TIP_INFO)),retMap);
    }
    /**
     * 手机充值卡支付接口
     * 
     * @param orderNo 订单号
     * @param platformId 支付平台id
     * @param money 支付金额，单位元
     * @param clientIp 客服端ip
     * @param bankCd 银行代码
     * @param imprestMode 充值方式
     * @param productName 商品名称
     * @param gameId 游戏Id
     * @param account 蜗牛通行证
     * @param ext 扩展
     * @param request
     * @return
     */
    @RequestMapping("/phonecard/dp")
	public String phonePayment(
			@RequestParam(value="orderno") String orderNo,
			@RequestParam(value="merchantid") String merchantId,
			@RequestParam(value="gameid",required=false) String gameId,
			@RequestParam(value="account",required=false) String account,
			@RequestParam(value="platformid") String platformId,
			@RequestParam(value="money") String money,
			@RequestParam(value="clientip") String clientIp,
			@RequestParam(value="bankcd",required=false) String bankCd,
			@RequestParam(value="mode",required=false) String mode,
			@RequestParam(value="cardtype",required=false) String cardtype,
			@RequestParam(value="productname") String productName,
			@RequestParam(value="backendurl") String backendurl,
			@RequestParam(value="fontendurl",required=false) String fontendurl,
			@RequestParam(value="ext",required=false) String ext,
			@RequestParam(value="cardNo",required=true) String cardNo,
			@RequestParam(value="cardPwd",required=true) String cardPwd,
			@RequestParam(value="captchaValue",required=true) String captchaValue,
			@RequestParam(value="currency",required=false) String moneyCurrency,
			@RequestParam(value="body",required=false) String body,
			@RequestParam(value="goodsDetail",required=false) String goodsDetail,
			@RequestParam(value="terminalType",required=false) String terminalType,
			@RequestParam(value="timeoutExpress",required=false) String timeoutExpress,
			@RequestParam(value="aid",required=false) String aid,
			HttpServletRequest request){
    	
    	Map<String,Object> retMap = new HashMap<String, Object>();
    	
    	if(StringUtils.isBlank(captchaValue)){
    		request.setAttribute("msg", "验证码为空");
    		retMap = ErrorCode.getErrorCode(56001);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return ORDER_ERROR_PAGE;
    	}
    	
    	if (!this.isCaptchaPass(captchaValue)) {
    		String random = request.getParameter("accountid");
    		if(StringUtils.isNotBlank(random) && this.isCaptchaGamePass(captchaValue, random)){
    			//验证通过
    			if(logger.isInfoEnabled()){
    				logger.info("验证通过:"+captchaValue);
    			}
    		}else{
    			//验证失败
    			request.setAttribute("msg", "验证码不正确");
        		retMap = ErrorCode.getErrorCode(56002);
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
        		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			return ORDER_ERROR_PAGE;
    		}
		}
    	
    	if(StringUtils.isBlank(cardNo)){
    		request.setAttribute("msg", "卡号不能为空");
    		retMap = ErrorCode.getErrorCode(56003);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			return ORDER_ERROR_PAGE;
    	}
    	
    	if(StringUtils.isBlank(cardPwd)){
    		request.setAttribute("msg", "密码不能为空");
    		retMap = ErrorCode.getErrorCode(56004);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			return ORDER_ERROR_PAGE;
    	}
    	
    	if(!ShenzpayDPPayment.isValidCard(cardNo, cardPwd)){
    		request.setAttribute("msg", "手机充值卡卡号或密码不正确");
    		retMap = ErrorCode.getErrorCode(56006);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
        	return ORDER_ERROR_PAGE;
        }
    	
    	if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		logger.error("订单号为空或长度超出,account:"+account);
    		request.setAttribute("msg", "订单号为空或长度超出");
    		retMap = ErrorCode.getErrorCode(56007);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return ORDER_ERROR_PAGE;
    	}
    	
    	if(StringUtils.isBlank(money) || "0".equals(StringUtils.trim(money))){
    		logger.error("订单金额不可以为空或零,account:"+account);
    		request.setAttribute("msg", "money为空或等于零");
    		retMap = ErrorCode.getErrorCode(56008);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return ORDER_ERROR_PAGE;
    	}
    	
    	if(StringUtils.isBlank(backendurl) || backendurl.indexOf("http") != 0){
    		logger.error("回调地址不可以为空,account:"+account);
    		request.setAttribute("msg", "backendurl为空或格式错误");
    		retMap = ErrorCode.getErrorCode(56009);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return ORDER_ERROR_PAGE;
    	}
    	
    	//1.创建订单
		HashMap<String, Object> exMap = new HashMap<String, Object>();
		exMap.put("defaultbank", bankCd);
		exMap.put("backendurl", backendurl);
		exMap.put("fontendurl", fontendurl);
		exMap.put("cardtype", cardtype);
		exMap.put("cardNo", cardNo);
		exMap.put("cardPwd", cardPwd);
		String procductType = ShenzpayDPPayment.getCardType(cardNo, cardPwd);//获取手机充值卡是哪个运营商的
		exMap.put("procductType", procductType);
		exMap.put("ext", ext);
		
		try{
			mode = paymentOrderService.getOrderMode(mode, bankCd);
			retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
														StringUtils.trim(productName),NumberUtils.toLong(aid),account,"0",NumberUtils.toLong(gameId),  
														mode, clientIp, exMap,moneyCurrency,body,goodsDetail,terminalType,timeoutExpress);
		} catch (Exception e){
			request.setAttribute("msg", e.getMessage());
			retMap = ErrorCode.getErrorCode(56099);
		}
		
		logger.info("支付订单信息,"+retMap);
    	
    	//2.跳转到直连页面
		String referer = request.getParameter("referer");
    	if (retMap != null && StringUtils.equalsIgnoreCase(ObjectUtils.toString(retMap.get(ErrorCode.TIP_CODE)), "1")) {
    		request.setAttribute("infoMap", retMap);
    		Map<String, String> reqmap = (Map<String, String>)retMap.get("paymentParams");
    		String paycode = ObjectUtils.toString(reqmap.get("msgcode"));
    		if("200".equals(paycode)){
        		//支付请求成功
    			PaymentOrder paymentOrder = (PaymentOrder)retMap.get("paymentOrder");
    			request.setAttribute(PaymentConstant.PAYMENT_ORDER, paymentOrder);
    			request.setAttribute("orderNo", orderNo);
    			request.setAttribute("money", paymentOrder.getMoney());
    			request.setAttribute("productName", productName);
    			request.setAttribute("createDate", DateUtil.parseDate2Str(paymentOrder.getCreate(), DateUtil.DATE_FORMAT_DATETIME));
    			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
        		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
        		return PAYMENT_PROCESS_PAGE;
        	}else{
        		//支付请求失败
        		String errormsg = ErrorCode.translate(reqmap.get("msgcode"));
        		if(StringUtils.isNotEmpty(errormsg)){
        			request.setAttribute("msg", errormsg);
        		}else{
        			request.setAttribute("msg", "支付请求失败，失败码["+paycode+"]");
        		}
        		logger.info("订单号：" + ((PaymentOrder)retMap.get("paymentOrder")).getOrderNo()+",支付请求失败，失败码["+paycode+"]");
            	request.setAttribute("referer", referer);
            	request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
        		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
        		return ORDER_ERROR_PAGE;
        	}
        }
    	
    	request.setAttribute("referer", referer);
    	request.setAttribute("msg", retMap.get(ErrorCode.TIP_INFO));
    	request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    	return ORDER_ERROR_PAGE;
    }
    
    /**
     * wap手机充值卡支付接口
     * 
     * @param orderNo 订单号
     * @param platformId 支付平台id
     * @param money 支付金额，单位元
     * @param clientIp 客服端ip
     * @param bankCd 银行代码
     * @param imprestMode 充值方式
     * @param productName 商品名称
     * @param gameId 游戏Id
     * @param account 蜗牛通行证
     * @param callback 跨域
     * @param ext 扩展
     * @param request
     * @return
     */
    @RequestMapping("/phonecard/dp/jsonp")
	public void phonePaymentJsonp(HttpServletRequest request, HttpServletResponse response){
    	String orderNo = request.getParameter("orderno");
    	String merchantId = request.getParameter("merchantid");
    	String gameId = request.getParameter("gameid");
    	String account = request.getParameter("account");
    	String platformId = request.getParameter("platformid");
    	String money = request.getParameter("money");
    	String clientIp = request.getParameter("clientip");
    	String bankCd = request.getParameter("bankcd");
    	String mode = request.getParameter("mode");
    	String cardtype = request.getParameter("cardtype");
    	String productName = request.getParameter("productname");
    	String backendurl = request.getParameter("backendurl");
    	String fontendurl = request.getParameter("fontendurl");
    	String ext = request.getParameter("ext");
    	String callback = request.getParameter("callback");
    	String cardNo = request.getParameter("cardNo");
    	String cardPwd = request.getParameter("cardPwd");
    	String captchaValue = request.getParameter("captchaValue");
    	String aid = request.getParameter("aid");
    	
    	String moneyCurrency = request.getParameter("currency");
    	String body = request.getParameter("body");
    	String goodsDetail = request.getParameter("goodsDetail");
    	String terminalType = request.getParameter("terminalType");
    	String timeoutExpress = request.getParameter("timeoutExpress");
    	
    	Map<String,Object> retMap = new HashMap<String, Object>();
    	
    	if(StringUtils.isBlank(captchaValue)){
    		request.setAttribute("msg", "验证码为空");
			retMap = ErrorCode.getErrorCode(56001);
	    	request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
	        return;
    	}
    	
    	if (!this.isCaptchaPass(captchaValue)) {
    		String random = request.getParameter("accountid");
    		if(StringUtils.isNotBlank(random) && this.isCaptchaGamePass(captchaValue, random)){
    			//验证通过
    			if(logger.isInfoEnabled()){
    				logger.info("验证通过:"+captchaValue);
    			}
    		}else{
    			request.setAttribute("msg", "验证码不正确");
        		retMap = ErrorCode.getErrorCode(56002);
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
    			return;
    		}
		}
    	
    	if(StringUtils.isBlank(cardNo)){
    		request.setAttribute("msg", "卡号不能为空");
    		retMap = ErrorCode.getErrorCode(56003);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
	        return;
    	}
    	
    	if(StringUtils.isBlank(cardPwd)){
    		request.setAttribute("msg", "密码不能为空");
    		retMap = ErrorCode.getErrorCode(56004);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
	        return;
    	}
    	
    	if(!ShenzpayDPPayment.isValidCard(cardNo, cardPwd)){
    		request.setAttribute("msg", "手机充值卡卡号或密码不正确");
    		retMap = ErrorCode.getErrorCode(56006);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
	        return;
        }
    	
    	if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		logger.error("订单号为空或长度超出,account:"+account);
    		request.setAttribute("msg", "订单号为空或长度超出");
    		retMap = ErrorCode.getErrorCode(56007);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
	        return;
    	}
    	
    	if(StringUtils.isBlank(money) || "0".equals(StringUtils.trim(money))){
    		logger.error("订单金额不可以为空或零,account:"+account);
    		request.setAttribute("msg", "money为空或等于零");
    		retMap = ErrorCode.getErrorCode(56008);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
	        return;
    	}
    	
    	if(StringUtils.isBlank(backendurl) || backendurl.indexOf("http") != 0){
    		logger.error("回调地址不可以为空,account:"+account);
    		request.setAttribute("msg", "backendurl为空或格式错误");
    		retMap = ErrorCode.getErrorCode(56009);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
	        return;
    	}
    	
    	//1.创建订单
		HashMap<String, Object> exMap = new HashMap<String, Object>();
		exMap.put("defaultbank", bankCd);
		exMap.put("backendurl", backendurl);
		exMap.put("fontendurl", fontendurl);
		exMap.put("cardtype", cardtype);
		exMap.put("cardNo", cardNo);
		exMap.put("cardPwd", cardPwd);
		exMap.put("ext", ext);
		String procductType = ShenzpayDPPayment.getCardType(cardNo, cardPwd);//获取手机充值卡是哪个运营商的
		//联通 - UNICOM  神州行 - SZX  电信 - TELECOM
		if(cardtype.equals("YD") && !procductType.equals(PaymentConstant.PAYMENT_PRODUCTION_TYPE_SZX)){
			retMap = ErrorCode.getErrorCode(56013);//56013=运营商不正确
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
	        return;
		}
		if(cardtype.equals("DX") && !procductType.equals(PaymentConstant.PAYMENT_PRODUCTION_TYPE_TELECOM)){
			retMap = ErrorCode.getErrorCode(56013);//56013=运营商不正确
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
	        return;
		}
		if(cardtype.equals("LT") && !procductType.equals(PaymentConstant.PAYMENT_PRODUCTION_TYPE_UNICOM)){
			retMap = ErrorCode.getErrorCode(56013);//56013=运营商不正确
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
	        return;
		}
		exMap.put("procductType", procductType);
		
		try{
			
			if(StringUtils.isNotBlank(clientIp) && clientIp.equals("0.0.0.0")){
				clientIp = IpUtils.getRemoteAddr(request);
			}
			
			mode = paymentOrderService.getOrderMode(mode, bankCd);
			retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
														StringUtils.trim(productName),NumberUtils.toLong(aid),account,"0",NumberUtils.toLong(gameId),  
														mode, clientIp, exMap,moneyCurrency,body,goodsDetail,terminalType,timeoutExpress);
		} catch (Exception e){
			request.setAttribute("msg", e.getMessage());
			retMap = ErrorCode.getErrorCode(56099);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
	        return;
		}
		
		logger.info("支付订单信息,"+retMap);
    	
    	//2.跳转到直连页面
		String referer = request.getParameter("referer");
    	if (retMap != null && StringUtils.equalsIgnoreCase(ObjectUtils.toString(retMap.get(ErrorCode.TIP_CODE)), "1")) {
    		request.setAttribute("infoMap", retMap);
    		Map<String, String> reqmap = (Map<String, String>)retMap.get("paymentParams");
    		String paycode = ObjectUtils.toString(reqmap.get("msgcode"));
    		if("200".equals(paycode)){
        		//支付请求成功
    			PaymentOrder paymentOrder = (PaymentOrder)retMap.get("paymentOrder");
    			request.setAttribute(PaymentConstant.PAYMENT_ORDER, paymentOrder);
    			request.setAttribute("orderNo", orderNo);
    			request.setAttribute("money", paymentOrder.getMoney());
    			request.setAttribute("productName", productName);
    			request.setAttribute("createDate", DateUtil.parseDate2Str(paymentOrder.getCreate(), DateUtil.DATE_FORMAT_DATETIME));
    			
    			retMap.put(ErrorCode.TIP_CODE, ErrorCode.getErrorCode(1).get(ErrorCode.TIP_CODE));
    			retMap.put(ErrorCode.TIP_INFO, ErrorCode.getErrorCode(1).get(ErrorCode.TIP_INFO));
    			retMap.put("createDate", DateUtil.parseDate2Str(paymentOrder.getCreate(), DateUtil.DATE_FORMAT_DATETIME));
    			retMap.put("paymentPlatform", null);
    			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, new ResultResponse(ResultResponse.SUCCESS,"创建订单成功",retMap));
    	        return;
        	}else{
        		//支付请求失败
        		String errormsg = ErrorCode.translate(reqmap.get("msgcode"));
        		if(StringUtils.isNotEmpty(errormsg)){
        			request.setAttribute("msg", errormsg);
        		}else{
        			request.setAttribute("msg", "支付请求失败，失败码["+paycode+"]");
        		}
        		logger.info("订单号：" + ((PaymentOrder)retMap.get("paymentOrder")).getOrderNo()+",支付请求失败，失败码["+paycode+"]");
            	request.setAttribute("referer", referer);
            	retMap = ErrorCode.getErrorCode(56098);
            	request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
            	writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
                return;
        	}
        }
    	
    	request.setAttribute("referer", referer);
    	request.setAttribute("msg", retMap.get(ErrorCode.TIP_INFO));
		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
		return;
    }
    
    /**
     * 蜗牛卡支付接口
     * 包含蜗牛一卡通,蜗牛移动卡,蜗牛全能卡
     * @param orderNo 订单号
     * @param platformId 支付平台id
     * @param money 支付金额，单位元
     * @param clientIp 客服端ip
     * @param bankCd 银行代码
     * @param imprestMode 充值方式
     * @param cardtype  卡类型
     * @param productName 商品名称
     * @param gameId 游戏Id
     * @param account 蜗牛通行证
     * @param ext 扩展
     * @param request
     * @return
     */
    @RequestMapping("/wncard/dp/jsonp")
	public void wncardPaymentJsonp(HttpServletRequest request,HttpServletResponse response){
    	
    	String orderNo = request.getParameter("orderno");
    	String merchantId = request.getParameter("merchantid");
    	String gameId = request.getParameter("gameid");
    	String account = request.getParameter("account");
    	String platformId = request.getParameter("platformid");
    	String money = request.getParameter("money");
    	String clientIp = request.getParameter("clientip");
    	String bankCd = request.getParameter("bankcd");
    	String mode = request.getParameter("mode");
    	String cardtype = request.getParameter("cardtype");
    	String productName = request.getParameter("productname");
    	String backendurl = request.getParameter("backendurl");
    	String fontendurl = request.getParameter("fontendurl");
    	String ext = request.getParameter("ext");
    	String callback = request.getParameter("callback");
    	String cardNo = request.getParameter("cardNo");
    	String cardPwd = request.getParameter("cardPwd");
    	String captchaValue = request.getParameter("captchaValue");
    	String aid = request.getParameter("aid");
    	
    	String moneyCurrency = request.getParameter("currency");
    	String body = request.getParameter("body");
    	String goodsDetail = request.getParameter("goodsDetail");
    	String terminalType = request.getParameter("terminalType");
    	String timeoutExpress = request.getParameter("timeoutExpress");
    	
    	Map<String,Object> retMap = new HashMap<String, Object>();
    	
    	if(StringUtils.isBlank(cardtype)){
    		request.setAttribute("msg", "参数不正确");
    		retMap = ErrorCode.getErrorCode(56010);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
			return;
    	}
    	
    	if(StringUtils.isBlank(captchaValue)){
    		request.setAttribute("msg", "验证码为空");
    		retMap = ErrorCode.getErrorCode(56001);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
			return;
    	}
    	
    	if (!this.isCaptchaPass(captchaValue)) {
    		String random = request.getParameter("accountid");
    		if(StringUtils.isNotBlank(random) && this.isCaptchaGamePass(captchaValue, random)){
    			//验证通过
    			if(logger.isInfoEnabled()){
    				logger.info("验证通过:"+captchaValue);
    			}
    		}else{
    			request.setAttribute("msg", "验证码不正确");
        		retMap = ErrorCode.getErrorCode(56002);
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
    			return;
    		}
		}
    	
    	//cardtype WG 蜗牛一卡通,WM 蜗牛移动充值卡,SM 蜗牛移动全能卡
    	if(PaymentConstant.WN_GAME_CARD.equals(cardtype)){
    		if(StringUtils.isBlank(cardNo)){
        		request.setAttribute("msg", "卡号不能为空");
        		retMap = ErrorCode.getErrorCode(56003);
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
    			return;
        	}
        	
        	if(StringUtils.isBlank(cardPwd)){
        		request.setAttribute("msg", "密码不能为空");
        		retMap = ErrorCode.getErrorCode(56004);
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
    			return;
        	}
    	}else if( PaymentConstant.WN_MOBILE_CARD.equals(cardtype) || PaymentConstant.WN_QMOBILE_CARD.equals(cardtype)){
    		if(StringUtils.isBlank(cardPwd)){
        		request.setAttribute("msg", "卡密不能为空");
        		retMap = ErrorCode.getErrorCode(56005);
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
    			return;
        	}
    		if(cardPwd.length() != 18){
        		request.setAttribute("msg", "卡密不正确");
        		retMap = ErrorCode.getErrorCode(56011);
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
    			return;
        	}
    	}
    	
    	if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		logger.error("订单号为空或长度超出,account:"+account);
    		request.setAttribute("msg", "订单号为空或长度超出");
    		retMap = ErrorCode.getErrorCode(56007);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
			return;
    	}
    	
    	if(StringUtils.isBlank(money) || "0".equals(StringUtils.trim(money))){
    		logger.error("订单金额不可以为空或零,account:"+account);
    		request.setAttribute("msg", "money为空或等于零");
    		retMap = ErrorCode.getErrorCode(56008);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
			return;
    	}
    	
    	if(StringUtils.isBlank(backendurl) || backendurl.indexOf("http") != 0){
    		logger.error("回调地址不可以为空,account:"+account);
    		request.setAttribute("msg", "backendurl为空或格式错误");
    		retMap = ErrorCode.getErrorCode(56009);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
			return;
    	}
    	
    	//1.创建订单
		HashMap<String, Object> exMap = new HashMap<String, Object>();
		exMap.put("defaultbank", bankCd);
		exMap.put("backendurl", backendurl);
		exMap.put("fontendurl", fontendurl);
		exMap.put("cardtype", cardtype);
		exMap.put("cardNo", cardNo);
		exMap.put("cardPwd", cardPwd);
		exMap.put("ext", ext);
		try{
			
			if(StringUtils.isNotBlank(clientIp) && clientIp.equals("0.0.0.0")){
				clientIp = IpUtils.getRemoteAddr(request);
			}
			
			mode = paymentOrderService.getOrderMode(mode, bankCd);
			retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
														StringUtils.trim(productName),NumberUtils.toLong(aid),account,"0",NumberUtils.toLong(gameId),  
														mode, clientIp, exMap ,moneyCurrency,body,goodsDetail,terminalType,timeoutExpress);
		} catch (Exception e){
			request.setAttribute("msg", e.getMessage());
			retMap = ErrorCode.getErrorCode(56099);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
	        return;
		}
		
		logger.info("支付订单信息,"+retMap);
    	
    	//2.跳转到直连页面
		String referer = request.getParameter("referer");
    	if (retMap != null && StringUtils.equalsIgnoreCase(ObjectUtils.toString(retMap.get(ErrorCode.TIP_CODE)), "1")) {
    		request.setAttribute("infoMap", retMap);
    		Map<String, String> reqmap = (Map<String, String>)retMap.get("paymentParams");
    		String paycode = ObjectUtils.toString(reqmap.get("msgcode"));
    		if("1".equals(paycode)){
        		//支付请求成功
    			PaymentOrder paymentOrder = (PaymentOrder)retMap.get("paymentOrder");
    			request.setAttribute(PaymentConstant.PAYMENT_ORDER, paymentOrder);
    			request.setAttribute("orderNo", orderNo);
    			request.setAttribute("money", paymentOrder.getMoney());
    			request.setAttribute("productName", productName);
    			request.setAttribute("createDate", DateUtil.parseDate2Str(paymentOrder.getCreate(), DateUtil.DATE_FORMAT_DATETIME));
    			retMap.put(ErrorCode.TIP_CODE, ErrorCode.getErrorCode(1).get(ErrorCode.TIP_CODE));
    			retMap.put(ErrorCode.TIP_INFO, ErrorCode.getErrorCode(1).get(ErrorCode.TIP_INFO));
    			retMap.put("createDate", DateUtil.parseDate2Str(paymentOrder.getCreate(), DateUtil.DATE_FORMAT_DATETIME));
    			retMap.put("paymentPlatform", null);
    			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, new ResultResponse(ResultResponse.SUCCESS,"创建订单成功",retMap));
    			return;
        	}else{
        		//支付请求失败
        		String errormsg = ObjectUtils.toString(reqmap.get("message"));
        		if(StringUtils.isNotEmpty(errormsg)){
        			request.setAttribute("msg", errormsg);
        		}else{
        			request.setAttribute("msg", "支付请求失败，失败码["+paycode+"]");
        		}
        		logger.info("订单号：" + ((PaymentOrder)retMap.get("paymentOrder")).getOrderNo()+",支付请求失败，失败码["+paycode+"]");
            	request.setAttribute("referer", referer);
            	retMap.put(ErrorCode.TIP_CODE, reqmap.get(ErrorCode.TIP_CODE));
            	retMap.put(ErrorCode.TIP_INFO, reqmap.get(ErrorCode.TIP_INFO));
            	request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
        		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
        		return;
        	}
        }
    	
    	request.setAttribute("referer", referer);
    	request.setAttribute("msg", retMap.get(ErrorCode.TIP_INFO));
    	writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
		return;
    }
    
    /**
     * 骏网卡支付接口
     * 
     * @param orderNo 订单号
     * @param platformId 支付平台id
     * @param money 支付金额，单位元
     * @param clientIp 客服端ip
     * @param bankCd 银行代码
     * @param imprestMode 充值方式
     * @param productName 商品名称
     * @param gameId 游戏Id
     * @param account 蜗牛通行证
     * @param ext 扩展
     * @param request
     * @return
     */
    @RequestMapping("/jcard/dp")
	public String gamecardPayment(
			@RequestParam(value="orderno") String orderNo,
			@RequestParam(value="merchantid") String merchantId,
			@RequestParam(value="gameid",required=false) String gameId,
			@RequestParam(value="account",required=false) String account,
			@RequestParam(value="platformid") String platformId,
			@RequestParam(value="money") String money,
			@RequestParam(value="clientip") String clientIp,
			@RequestParam(value="bankcd",required=false) String bankCd,
			@RequestParam(value="mode",required=false) String mode,
			@RequestParam(value="cardtype",required=false) String cardtype,
			@RequestParam(value="productname") String productName,
			@RequestParam(value="backendurl") String backendurl,
			@RequestParam(value="fontendurl",required=false) String fontendurl,
			@RequestParam(value="ext",required=false) String ext,
			@RequestParam(value="cardNo",required=true) String cardNo,
			@RequestParam(value="cardPwd",required=true) String cardPwd,
			@RequestParam(value="captchaValue",required=true) String captchaValue,
			@RequestParam(value="currency",required=false) String moneyCurrency,
			@RequestParam(value="body",required=false) String body,
			@RequestParam(value="goodsDetail",required=false) String goodsDetail,
			@RequestParam(value="terminalType",required=false) String terminalType,
			@RequestParam(value="timeoutExpress",required=false) String timeoutExpress,
			@RequestParam(value="aid",required=false) String aid,
			HttpServletRequest request){
    	
    	Map<String,Object> retMap = new HashMap<String, Object>();
    	
    	if(StringUtils.isBlank(captchaValue)){
    		request.setAttribute("msg", "验证码为空");
    		retMap = ErrorCode.getErrorCode(56001);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return ORDER_ERROR_PAGE;
    	}
    	
    	if (!this.isCaptchaPass(captchaValue)) {
    		String random = request.getParameter("accountid");
    		if(StringUtils.isNotBlank(random) && this.isCaptchaGamePass(captchaValue, random)){
    			//验证通过
    			if(logger.isInfoEnabled()){
    				logger.info("验证通过:"+captchaValue);
    			}
    		}else{
    			request.setAttribute("msg", "验证码不正确");
        		retMap = ErrorCode.getErrorCode(56002);
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			return ORDER_ERROR_PAGE;
    		}
		}
    	
    	if(StringUtils.isBlank(cardNo)){
    		request.setAttribute("msg", "卡号不能为空");
    		retMap = ErrorCode.getErrorCode(56003);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			return ORDER_ERROR_PAGE;
    	}
    	
    	if(StringUtils.isBlank(cardPwd)){
    		request.setAttribute("msg", "密码不能为空");
    		retMap = ErrorCode.getErrorCode(56004);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			return ORDER_ERROR_PAGE;
    	}
    	
    	if(!HuifubaoPayment.isValidCard(cardNo, cardPwd)){
    		request.setAttribute("msg", "骏网充值卡卡号或密码不正确");
    		retMap = ErrorCode.getErrorCode(56012);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
        	return ORDER_ERROR_PAGE;
        }
    	
    	if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		logger.error("订单号为空或长度超出,account:"+account);
    		request.setAttribute("msg", "订单号为空或长度超出");
    		retMap = ErrorCode.getErrorCode(56007);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return ORDER_ERROR_PAGE;
    	}
    	
    	if(StringUtils.isBlank(money) || "0".equals(StringUtils.trim(money))){
    		logger.error("订单金额不可以为空或零,account:"+account);
    		request.setAttribute("msg", "money为空或等于零");
    		retMap = ErrorCode.getErrorCode(56008);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return ORDER_ERROR_PAGE;
    	}
    	
    	if(StringUtils.isBlank(backendurl) || backendurl.indexOf("http") != 0){
    		logger.error("回调地址不可以为空,account:"+account);
    		request.setAttribute("msg", "backendurl为空或格式错误");
    		retMap = ErrorCode.getErrorCode(56009);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return ORDER_ERROR_PAGE;
    	}
    	
    	//1.创建订单
		HashMap<String, Object> exMap = new HashMap<String, Object>();
		exMap.put("defaultbank", bankCd);
		exMap.put("backendurl", backendurl);
		exMap.put("fontendurl", fontendurl);
		exMap.put("cardtype", cardtype);
		exMap.put("cardNo", cardNo);
		exMap.put("cardPwd", cardPwd);
		
		try{
			mode = paymentOrderService.getOrderMode(mode, bankCd);
			retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
														StringUtils.trim(productName),NumberUtils.toLong(aid),account,"0",NumberUtils.toLong(gameId),  
														mode, clientIp, exMap,moneyCurrency,body,goodsDetail,terminalType,timeoutExpress);
		} catch (Exception e){
			request.setAttribute("msg", e.getMessage());
		}
		
		logger.info("支付订单信息,"+retMap);
    	
    	//2.跳转到直连页面
		String referer = request.getParameter("referer");
    	if (retMap != null && StringUtils.equalsIgnoreCase(ObjectUtils.toString(retMap.get(ErrorCode.TIP_CODE)), "1")) {
    		request.setAttribute("infoMap", retMap);
    		Map<String, String> reqmap = (Map<String, String>)retMap.get("paymentParams");
    		String paycode = ObjectUtils.toString(reqmap.get("msgcode"));
    		if("0".equals(paycode)){
        		//支付请求成功
    			PaymentOrder paymentOrder = (PaymentOrder)retMap.get("paymentOrder");
    			request.setAttribute(PaymentConstant.PAYMENT_ORDER, paymentOrder);
    			request.setAttribute("orderNo", orderNo);
    			request.setAttribute("money", paymentOrder.getMoney());
    			request.setAttribute("productName", productName);
    			request.setAttribute("createDate", DateUtil.parseDate2Str(paymentOrder.getCreate(), DateUtil.DATE_FORMAT_DATETIME));
    			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			return PAYMENT_PROCESS_PAGE;
        	}else{
        		//支付请求失败
        		String errormsg = ObjectUtils.toString(reqmap.get("message"));
        		if(StringUtils.isNotEmpty(errormsg)){
        			request.setAttribute("msg", errormsg);
        		}else{
        			request.setAttribute("msg", "支付请求失败，失败码["+paycode+"]");
        		}
        		logger.info("订单号：" + ((PaymentOrder)retMap.get("paymentOrder")).getOrderNo()+",支付请求失败，失败码["+paycode+"]");
            	request.setAttribute("referer", referer);
            	request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
        		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
        		return ORDER_ERROR_PAGE;
        	}
        }
    	
    	request.setAttribute("referer", referer);
    	request.setAttribute("msg", retMap.get(ErrorCode.TIP_INFO));
    	return ORDER_ERROR_PAGE;
    }
    
    /**
     * 蜗牛卡支付接口
     * 包含蜗牛一卡通,蜗牛移动卡,蜗牛全能卡
     * @param orderNo 订单号
     * @param platformId 支付平台id
     * @param money 支付金额，单位元
     * @param clientIp 客服端ip
     * @param bankCd 银行代码
     * @param imprestMode 充值方式
     * @param cardtype  卡类型
     * @param productName 商品名称
     * @param gameId 游戏Id
     * @param account 蜗牛通行证
     * @param ext 扩展
     * @param request
     * @return
     */
    @RequestMapping("/wncard/dp")
	public void wncardPayment(
			@RequestParam(value="orderno") String orderNo,
			@RequestParam(value="merchantid") String merchantId,
			@RequestParam(value="gameid",required=false) String gameId,
			@RequestParam(value="account",required=false) String account,
			@RequestParam(value="platformid") String platformId,
			@RequestParam(value="money") String money,
			@RequestParam(value="clientip") String clientIp,
			@RequestParam(value="bankcd",required=false) String bankCd,
			@RequestParam(value="mode",required=false) String mode,
			@RequestParam(value="cardtype",required=false) String cardtype,
			@RequestParam(value="productname") String productName,
			@RequestParam(value="backendurl") String backendurl,
			@RequestParam(value="fontendurl",required=false) String fontendurl,
			@RequestParam(value="ext",required=false) String ext,
			@RequestParam(value="callback",required=false) String callback,
			@RequestParam(value="cardNo",required=true) String cardNo,
			@RequestParam(value="cardPwd",required=true) String cardPwd,
			@RequestParam(value="captchaValue",required=true) String captchaValue,
			@RequestParam(value="currency",required=false) String moneyCurrency,
			@RequestParam(value="body",required=false) String body,
			@RequestParam(value="goodsDetail",required=false) String goodsDetail,
			@RequestParam(value="terminalType",required=false) String terminalType,
			@RequestParam(value="timeoutExpress",required=false) String timeoutExpress,
			@RequestParam(value="aid",required=false) String aid,
			HttpServletRequest request,HttpServletResponse response){
    	
    	Map<String,Object> retMap = new HashMap<String, Object>();
    	
    	if(StringUtils.isBlank(cardtype)){
    		request.setAttribute("msg", "参数不正确");
			retMap = ErrorCode.getErrorCode(56010);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, retMap);
			return;
    	}
    	
    	if(StringUtils.isBlank(captchaValue)){
    		request.setAttribute("msg", "验证码为空");
			retMap = ErrorCode.getErrorCode(56001);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, retMap);
			return;
    	}
    	
    	if (!this.isCaptchaPass(captchaValue)) {
    		String random = request.getParameter("accountid");
    		if(StringUtils.isNotBlank(random) && this.isCaptchaGamePass(captchaValue, random)){
    			//验证通过
    			if(logger.isInfoEnabled()){
    				logger.info("验证通过:"+captchaValue);
    			}
    		}else{
    			request.setAttribute("msg", "验证码不正确");
        		retMap = ErrorCode.getErrorCode(56002);
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
    			return;
    		}
		}
    	
    	//cardtype WG 蜗牛一卡通,WM 蜗牛移动充值卡,SM 蜗牛移动全能卡
    	if(PaymentConstant.WN_GAME_CARD.equals(cardtype)){
    		if(StringUtils.isBlank(cardNo)){
        		request.setAttribute("msg", "卡号不能为空");
        		retMap = ErrorCode.getErrorCode(56003);
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, retMap);
    			return;
        	}
        	
        	if(StringUtils.isBlank(cardPwd)){
        		request.setAttribute("msg", "密码不能为空");
        		retMap = ErrorCode.getErrorCode(56004);
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, retMap);
    			return;
        	}
    	}else if( PaymentConstant.WN_MOBILE_CARD.equals(cardtype) || PaymentConstant.WN_QMOBILE_CARD.equals(cardtype)){
    		if(StringUtils.isBlank(cardPwd)){
        		request.setAttribute("msg", "卡密不能为空");
        		retMap = ErrorCode.getErrorCode(56005);
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, retMap);
    			return;
        	}
    	}
    	
    	if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		logger.error("订单号为空或长度超出,account:"+account);
    		request.setAttribute("msg", "订单号为空或长度超出");
    		retMap = ErrorCode.getErrorCode(56007);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, retMap);
			return;
    	}
    	
    	if(StringUtils.isBlank(money) || "0".equals(StringUtils.trim(money))){
    		logger.error("订单金额不可以为空或零,account:"+account);
    		request.setAttribute("msg", "money为空或等于零");
    		retMap = ErrorCode.getErrorCode(56008);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, retMap);
			return;
    	}
    	
    	if(StringUtils.isBlank(backendurl) || backendurl.indexOf("http") != 0){
    		logger.error("回调地址不可以为空,account:"+account);
    		request.setAttribute("msg", "backendurl为空或格式错误");
    		retMap = ErrorCode.getErrorCode(56009);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, retMap);
			return;
    	}
    	
		try{
			//1.创建订单
			HashMap<String, Object> exMap = new HashMap<String, Object>();
			exMap.put("defaultbank", bankCd);
			exMap.put("backendurl", backendurl);
			exMap.put("fontendurl", fontendurl);
			exMap.put("cardtype", cardtype);
			exMap.put("cardNo", cardNo);
			exMap.put("cardPwd", cardPwd);
			exMap.put("ext", ext);
			
			mode = paymentOrderService.getOrderMode(mode, bankCd);
			retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
														StringUtils.trim(productName),NumberUtils.toLong(aid),account,"0",NumberUtils.toLong(gameId),  
														mode, clientIp, exMap ,moneyCurrency,body,goodsDetail,terminalType,timeoutExpress);
		} catch (Exception e){
			request.setAttribute("msg", e.getMessage());
			retMap = ErrorCode.getErrorCode(56099);
		}
		
		logger.info("支付订单信息,"+retMap);
    	
    	//2.跳转到直连页面
		String referer = request.getParameter("referer");
    	if (retMap != null && StringUtils.equalsIgnoreCase(ObjectUtils.toString(retMap.get(ErrorCode.TIP_CODE)), "1")) {
    		request.setAttribute("infoMap", retMap);
    		Map<String, String> reqmap = (Map<String, String>)retMap.get("paymentParams");
    		String paycode = ObjectUtils.toString(reqmap.get("msgcode"));
    		if("1".equals(paycode)){
        		//支付请求成功
    			PaymentOrder paymentOrder = (PaymentOrder)retMap.get("paymentOrder");
    			request.setAttribute(PaymentConstant.PAYMENT_ORDER, paymentOrder);
    			request.setAttribute("orderNo", orderNo);
    			request.setAttribute("money", paymentOrder.getMoney());
    			request.setAttribute("productName", productName);
    			request.setAttribute("createDate", DateUtil.parseDate2Str(paymentOrder.getCreate(), DateUtil.DATE_FORMAT_DATETIME));
    			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
        		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
				retMap.put(ErrorCode.TIP_CODE, 1);
    			retMap.put(ErrorCode.TIP_INFO, "成功");
    			writeJsonp(callback, response, retMap);
    			return;
        	}else{
        		//支付请求失败
        		String errormsg = ObjectUtils.toString(reqmap.get("message"));
        		if(StringUtils.isNotEmpty(errormsg)){
        			request.setAttribute("msg", errormsg);
        		}else{
        			request.setAttribute("msg", "支付请求失败，失败码["+paycode+"]");
        		}
        		logger.info("订单号：" + ((PaymentOrder)retMap.get("paymentOrder")).getOrderNo()+",支付请求失败，失败码["+paycode+"]");
            	request.setAttribute("referer", referer);
            	retMap.put(ErrorCode.TIP_CODE, 2);
        		retMap.put(ErrorCode.TIP_INFO, retMap.get(ErrorCode.TIP_INFO));
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
        		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
        		writeJsonp(callback, response, retMap);
        		return;
        	}
        }
    	
    	request.setAttribute("referer", referer);
    	request.setAttribute("msg", retMap.get(ErrorCode.TIP_INFO));
    	request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
		retMap.put(ErrorCode.TIP_CODE, 0);
		retMap.put(ErrorCode.TIP_INFO, retMap.get(ErrorCode.TIP_INFO));
		writeJsonp(callback, response, retMap);
		return;
    }
    
    /**
     * 支付接口
     * wap 支付
     * @param orderNo 订单号
     * @param gameId 游戏Id
     * @param account 蜗牛通行证
     * @param platformId 支付平台id
     * @param money 支付金额，单位元
     * @param clientIp 客服端ip
     * @param bankCd 银行代码
     * @param imprestMode 充值方式
     * @param productName 商品名称
     * @param ext 扩展
     * @param request
     * @return
     */
    @RequestMapping("/jsonp")
	public void paymentJsonp(
			@RequestParam(value="orderno") String orderNo,
			@RequestParam(value="merchantid") String merchantId,
			@RequestParam(value="gameid",required=false) String gameId,
			@RequestParam(value="account",required=false) String account,
			@RequestParam(value="platformid") String platformId,
			@RequestParam(value="money") String money,
			@RequestParam(value="clientip") String clientIp,
			@RequestParam(value="bankcd",required=false) String bankCd,
			@RequestParam(value="mode",required=false) String mode,
			@RequestParam(value="productname") String productName,
			@RequestParam(value="backendurl") String backendurl,
			@RequestParam(value="fontendurl",required=false) String fontendurl,
			@RequestParam(value="openid",required=false) String openId,
			@RequestParam(value="ext",required=false) String ext,
			@RequestParam(value="callback",required=false) String callback,
			@RequestParam(value="yueMoney",required=false) String yueMoney,
			@RequestParam(value="yueCurrency",required=false) String yueCurrency,
			@RequestParam(value="fcbsmscode",required=false) String fcbsmscode,
			@RequestParam(value="currency",required=false) String moneyCurrency,
			@RequestParam(value="body",required=false) String body,
			@RequestParam(value="goodsDetail",required=false) String goodsDetail,
			@RequestParam(value="terminalType",required=false) String terminalType,
			@RequestParam(value="timeoutExpress",required=false) String timeoutExpress,
			@RequestParam(value="aid",required=false) String aid,
			HttpServletRequest request, HttpServletResponse response){
    	
    	Map<String,Object> retMap = new HashMap<String, Object>();
    	//1.参数验证
    	if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		logger.error("订单号为空或长度超出,account:"+account);
    		retMap.put("msg", "订单号为空或长度超出");
    		retMap = ErrorCode.getErrorCode(56007);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
	        return;
    	}
    	
    	if(StringUtils.isBlank(money) || "0".equals(StringUtils.trim(money))){
    		logger.error("订单金额不可以为空或零,account:"+account);
    		retMap.put("msg", "money为空或等于零");
    		retMap = ErrorCode.getErrorCode(56008);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
	        return;
    	}
    	
    	if(StringUtils.isBlank(backendurl) || backendurl.indexOf("http") != 0){
    		logger.error("回调地址不可以为空,account:"+account);
    		retMap.put("msg", "backendurl为空或格式错误");
    		retMap = ErrorCode.getErrorCode(56009);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
	        return;
    	}
    	
    	//2.创建订单
		HashMap<String, Object> exMap = new HashMap<String, Object>();
		exMap.put("defaultbank", bankCd);
		exMap.put("backendurl", backendurl);
		exMap.put("fontendurl", fontendurl);
		exMap.put("openid", openId);
		exMap.put("ext", ext);
		
		try{
			mode = paymentOrderService.getOrderMode(mode, bankCd);
			//判断是否是组合支付
	    	if(StringUtils.isNotBlank(yueMoney) && NumberUtils.toFloat(yueMoney) > 0){
	    		//验证翡翠身份
	    		boolean isFcb = ApiBaseController.hasFcbIdentity(request);
	    		if(!isFcb){
	    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未拿到翡翠分身",retMap));
	    	        return;
	    		}
	    		//获得翡翠账号、翡翠号码
	    		//String fcbAccount = ApiBaseController.getFcbAccount(request);
	    		String fcbPhone = ApiBaseController.getFcbPhone(request);
	        		
	    		Map<String, Object> smsMap = smsService.validateSmsCode(fcbPhone, FCB_SMS_TYPE, fcbsmscode);
	    		String code = ObjectUtils.toString(smsMap.get("code"));
	    		if(!"20".equals(code)){
	    			String message = ObjectUtils.toString(smsMap.get("message"));
	    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,message,retMap));
	    	        return;
	    		}
	    		
    			retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
						yueMoney,yueCurrency,StringUtils.trim(productName),account,NumberUtils.toLong(gameId),  
						mode, clientIp, exMap,body,goodsDetail,terminalType,timeoutExpress);
	    	}else{
	    		retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
						StringUtils.trim(productName),NumberUtils.toLong(aid),account,"0",NumberUtils.toLong(gameId),  
						mode, clientIp, exMap,moneyCurrency,body,goodsDetail,terminalType,timeoutExpress);
	    	}
			
		} catch (Exception e){
			retMap = ErrorCode.getErrorCode(56099);
			request.setAttribute("msg", e.getMessage());
		}
		
		logger.info("支付订单信息,"+retMap);
    	
    	if (retMap != null && StringUtils.equalsIgnoreCase(ObjectUtils.toString(retMap.get(ErrorCode.TIP_CODE)), "1")) {
    		retMap.put("paymentPlatform", null);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.SUCCESS,"创建订单成功",retMap));
	        return;
		}
    	request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
        return;
    }

    @Resource
    private GameManagerService gameManagerService;
    /**
     * 生成第三方订单号
	 *
     */
    @RequestMapping("/orderno/create")
    public @ResponseBody ResultResponse createThirdPartyOrderNo(@RequestParam(value="platformid") String platformId,
    		@RequestParam(value="serverid") String serverId,HttpServletRequest request){
    	GameServer gameServer = gameManagerService.queryGameServerById(Long.valueOf(serverId));//验证服务器id
    	if(gameServer == null){
    		request.setAttribute("retCode", ErrorCode.getErrorCode(56100).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56100).get(ErrorCode.TIP_INFO));
    		return new ResultResponse(ResultResponse.FAIL,"生成订单失败,服务器id未查询到");
    	}
    	String orderNo = paymentOrderService.genThirdPartyNo(platformId, serverId);
    	
    	Map<String,Object> results = new HashMap<String,Object>();
    	results.put("orderno", orderNo);
    	request.setAttribute("retCode", ErrorCode.getErrorCode(1).get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", ErrorCode.getErrorCode(1).get(ErrorCode.TIP_INFO));
    	return new ResultResponse(ResultResponse.SUCCESS,"生成订单成功",results);
    }
    	
    
    @ExceptionHandler(Exception.class)
    public @ResponseBody ResultResponse  handleTypeMismatchException(RuntimeException ex) {
    	logger.error(ex.getMessage(),ex);
    	return new ResultResponse(ResultResponse.FAIL,"参数类型或格式错误,"+ex.getMessage());
    }
}
