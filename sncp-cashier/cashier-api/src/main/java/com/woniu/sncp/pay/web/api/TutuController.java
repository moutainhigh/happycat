package com.woniu.sncp.pay.web.api;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.woniu.sncp.lang.DateUtil;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.common.utils.PaymentConstant;
import com.woniu.sncp.pay.common.utils.http.HttpUtils;
import com.woniu.sncp.pay.core.service.CorePassportService;
import com.woniu.sncp.pay.core.service.PaymentOrderService;
import com.woniu.sncp.pay.core.service.ocp.OcpAccountService;
import com.woniu.sncp.pay.core.service.payment.PaymentFacade;
import com.woniu.sncp.pay.core.service.sms.SmsService;
import com.woniu.sncp.pay.repository.pay.PaymentOrder;
import com.woniu.sncp.pojo.passport.Passport;
import com.woniu.sncp.web.IpUtils;
import com.woniu.sncp.web.response.ResultResponse;

@Controller
public class TutuController extends ApiBaseController{
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private OcpAccountService ocpAccountService;
	
	@Resource
    private PaymentFacade paymentFacade;
	@Resource
	private PaymentOrderService paymentOrderService;
	@Resource
	private CorePassportService corePassportService;
	@Resource
	private SmsService smsService;

	
    private final static String ORDER_ERROR_PAGE = "/payment/error";
    private final static String PAYMENT_SUCCESS_PAGE = "/payment/payment_success";
    private final static String FCB_SMS_TYPE="CASHIER-FCBSMS";
	
	@RequestMapping("/security/ttb/query")
	@ResponseBody
	public Map<String, Object> queryTutuAmount(HttpServletRequest request){
		String merchantId = request.getParameter("merchantId");
		String loginAccount = (String)request.getSession().getAttribute("loginUser");
		if(StringUtils.isBlank(loginAccount)){
    		request.setAttribute("retCode", ErrorCode.getErrorCode(56102).get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", ErrorCode.getErrorCode(56102).get(ErrorCode.TIP_INFO));
			return null;
		}
		Passport passport = corePassportService.queryPassport(loginAccount);
		
		Long aid = getLoginId();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", aid);
		params.put("spId", 7);
		params.put("appId", "36");
		params.put("areaId", "-1");
		params.put("payTypeId", "-1");
		params.put("eventTimestamp", DateUtil.getCurDateTimeStr());
		params.put("clientIp", IpUtils.getRemoteAddr(request));
		params.put("merchantId", merchantId);
		
		Map<String, Object> resultMap = ocpAccountService.queryAmount2(params);
		resultMap.put("mobile", shortMobile(passport.getMobile()));
		resultMap.put("aid", passport.getId());
		request.setAttribute("retCode", ErrorCode.getErrorCode(1).get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", ErrorCode.getErrorCode(1).get(ErrorCode.TIP_INFO));
		return resultMap;
	}
	
	@RequestMapping("/security/sms/send")
	@ResponseBody
	public Map<String, Object> sendSmsCode(HttpServletRequest request){
		Map<String, Object> retMap = new HashMap<String, Object>();
		String loginAccount = (String)request.getSession().getAttribute("loginUser");
		if(StringUtils.isBlank(loginAccount)){
			retMap.put("msgcode", "0");
			retMap.put("message", "你还未登录");
			retMap = ErrorCode.getErrorCode(56102);
			request.setAttribute("retCode", ErrorCode.getErrorCode(56102).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56102).get(ErrorCode.TIP_INFO));
			return retMap;
		}
		Passport passport = corePassportService.queryPassport(loginAccount);
		if(passport == null){
			retMap.put("msgcode", "0");
			retMap.put("message", "账号不存在");
			retMap = ErrorCode.getErrorCode(56103);
			request.setAttribute("retCode", ErrorCode.getErrorCode(56103).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56103).get(ErrorCode.TIP_INFO));
			return retMap;
		}
		
		if(!"1".equals(passport.getMobileAuthed()) || StringUtils.isBlank(passport.getMobile())){
			retMap.put("msgcode", "0");
			retMap.put("message", "未绑定手机，请前往会员中心绑定安全手机。");
			retMap = ErrorCode.getErrorCode(56104);
			request.setAttribute("retCode", ErrorCode.getErrorCode(56104).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56104).get(ErrorCode.TIP_INFO));
			return retMap;
		}
		
		Map<String, Object> smsMap = smsService.sendSmsValidateCode(passport.getMobile(), null);
		String code =ObjectUtils.toString(smsMap.get("code"));
		JSONObject data = null;
		if(null!=ObjectUtils.toString(smsMap.get("data"))){
			Map<String,Object> _data = (Map<String, Object>) (smsMap.get("data"));
			String sd = JSON.toJSONString(_data);
			data = JSONObject.parseObject(sd);
		}
		if("0".equals(code)){
			retMap.put("msgcode", "1");
			retMap.put("message", "验证码发送成功");
			retMap.put("data", data);
			request.setAttribute("retCode", ErrorCode.getErrorCode(56106).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56106).get(ErrorCode.TIP_INFO));
		}else{
			retMap.put("msgcode", "0");
			retMap.put("message", ObjectUtils.toString(smsMap.get("message")));
			retMap = ErrorCode.getErrorCode(56105);
			retMap.put("data", data);
			request.setAttribute("retCode", ErrorCode.getErrorCode(56105).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56105).get(ErrorCode.TIP_INFO));
		}
		
		return retMap;
	}
	
	@RequestMapping(value={"/security/ttb/pay"})
	public String payment(
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
			@RequestParam(value="ttbDjjMoney",required=false) String ttbDjjMoney,
			@RequestParam(value="smscode",required=false) String smscode,
			@RequestParam(value="yueMoney",required=false) String yueMoney,
			@RequestParam(value="yueCurrency",required=false) String yueCurrency,
			@RequestParam(value="fcbsmscode",required=false) String fcbsmscode,
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
    	
    	String loginAccount = (String)request.getSession().getAttribute("loginUser");
		if(StringUtils.isBlank(loginAccount)){
			request.setAttribute("msg", "尚未登录");
			retMap = ErrorCode.getErrorCode(56102);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return ORDER_ERROR_PAGE;
		}
		Passport passport = corePassportService.queryPassport(loginAccount);
		if(passport == null){
			request.setAttribute("msg", "账号不存在");
			retMap = ErrorCode.getErrorCode(56103);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return ORDER_ERROR_PAGE;
		}
		
    	//判断兔兔币代金卷是否需要短信验证
    	boolean isSms = ocpAccountService.isSmsCheck(ttbDjjMoney,merchantId);
    	if(isSms){
    		if(!"1".equals(passport.getMobileAuthed()) || StringUtils.isBlank(passport.getMobile())){
    			request.setAttribute("msg", "未绑定手机，请前往会员中心绑定安全手机。");
    			retMap = ErrorCode.getErrorCode(56104);
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
        		return ORDER_ERROR_PAGE;
    		}
    		
    		Map<String, Object> smsMap = smsService.validateSmsCode(passport.getMobile(), null, smscode);
    		String code = ObjectUtils.toString(smsMap.get("code"));
    		if(!"20".equals(code)){
    			String message = ObjectUtils.toString(smsMap.get("message"));
    			request.setAttribute("msg", message);
    			retMap = ErrorCode.getErrorCode(56107);
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
        		return ORDER_ERROR_PAGE;
    		}
    	}
    	
    	//1.创建订单
		HashMap<String, Object> exMap = new HashMap<String, Object>();
		exMap.put("defaultbank", bankCd);
		exMap.put("backendurl", backendurl);
		exMap.put("fontendurl", fontendurl);
		exMap.put("cardtype", cardtype);
		//兔兔币代金卷参数
		exMap.put("ttbDjjMoney", ttbDjjMoney);
		exMap.put("ext", ext);
		
		try{
			mode = paymentOrderService.getOrderMode(platformId);
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
	    		
	    		//验证翡翠验证码
	    		Map<String, Object> smsMap = smsService.validateSmsCode(fcbPhone, FCB_SMS_TYPE, fcbsmscode);
	    		String code = ObjectUtils.toString(smsMap.get("code"));
	    		if(!"20".equals(code)){
	    			String message = ObjectUtils.toString(smsMap.get("message"));
	    			request.setAttribute("msg", message);
	        		return ORDER_ERROR_PAGE;
	    		}
	    		
    			retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
    					yueCurrency,yueMoney,StringUtils.trim(productName),loginAccount,NumberUtils.toLong(gameId),  
						mode, clientIp, exMap);
	    			
	    	}else{
	    		retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
						StringUtils.trim(productName),loginAccount,NumberUtils.toLong(gameId),  
						mode, clientIp, exMap);
	    	}
		} catch (Exception e){
			request.setAttribute("msg", e.getMessage());
			retMap = ErrorCode.getErrorCode(56099);
		}
		
		logger.info("支付订单信息,"+retMap);
    	
    	//2.跳转到直连页面
		String referer = request.getParameter("referer");
    	if (retMap != null && StringUtils.equalsIgnoreCase(ObjectUtils.toString(retMap.get(ErrorCode.TIP_CODE)), "1")) {
    		//request.setAttribute("infoMap", retMap);
    		Map<String, String> reqmap = (Map<String, String>)retMap.get("paymentParams");
    		String paycode = ObjectUtils.toString(reqmap.get("msgcode"));
    		if("1".equals(paycode)){
        		//支付成功
    			PaymentOrder paymentOrder = (PaymentOrder)retMap.get("paymentOrder");
    			request.setAttribute(PaymentConstant.PAYMENT_ORDER, paymentOrder);
    			request.setAttribute("orderNo", orderNo);
    			request.setAttribute("money", paymentOrder.getMoney());
    			request.setAttribute("productName", productName);
    			request.setAttribute("createDate", DateUtil.parseDate2Str(paymentOrder.getCreateDate(), DateUtil.DATE_FORMAT_DATETIME));
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			return PAYMENT_SUCCESS_PAGE;
        	}else{
        		//支付失败
        		request.setAttribute("msg", ObjectUtils.toString(reqmap.get("message")));
        		logger.info("订单号：" + ((PaymentOrder)retMap.get("paymentOrder")).getOrderNo()+",充值请求失败，失败码["+paycode+"]");
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
	
	
	@RequestMapping("/api/tgt/sms/send/json")
	public void sendSmsCode(@RequestParam(value="tgt",required=false) String tgt,
			@RequestParam(value="callback",required=false) String callback
			,HttpServletRequest request, HttpServletResponse response){
		Map<String, Object> resultMap = new HashMap<String,Object>();
		if(StringUtils.isBlank(tgt)){
			resultMap = ErrorCode.getErrorCode(56108);
			request.setAttribute("retCode", resultMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", resultMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"缺少tgt参数",resultMap));
			return;
    	}
		
		String sAid = getAidFormSSO(tgt);
		if(StringUtils.isBlank(sAid)){
			resultMap.put("code", "0");
			resultMap.put("message", "ST过期或已失效");
			request.setAttribute("retCode", ErrorCode.getErrorCode(56109).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56109).get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"ST过期或已失效",resultMap));
			return;
		} 
		
		Passport passport = corePassportService.queryPassport(Long.valueOf(sAid));
		if(passport == null){
			resultMap.put("code", "0");
			resultMap.put("message", "账号不存在");
			request.setAttribute("retCode", ErrorCode.getErrorCode(56103).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56103).get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"账号不存在",resultMap));
			return;
		}
		
		if(!"1".equals(passport.getMobileAuthed()) || StringUtils.isBlank(passport.getMobile())){
			resultMap.put("code", "0");
			resultMap.put("message", "未绑定手机");
			request.setAttribute("retCode", ErrorCode.getErrorCode(56104).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56104).get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未绑定手机",resultMap));
			return;
		}
		
		Map<String, Object> smsMap = smsService.sendSmsValidateCode(passport.getMobile(), null);
		String code = ObjectUtils.toString(smsMap.get("code"));
		JSONObject data = null;
		if(null!=ObjectUtils.toString(smsMap.get("data"))){
			Map<String,Object> _data = (Map<String, Object>) (smsMap.get("data"));
			String sd = JSON.toJSONString(_data);
			data = JSONObject.parseObject(sd);
		}
		if("0".equals(code)){
			//发生成功
			resultMap = ErrorCode.getErrorCode(1);
			resultMap.put("data", data);
			request.setAttribute("retCode", resultMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", resultMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.SUCCESS,"短信发送请求成功",resultMap));
			return;
		}else{
			//发生失败
			String message = ObjectUtils.toString(smsMap.get("message"));
			resultMap = ErrorCode.getErrorCode(56105);
			resultMap.put("data", data);
			request.setAttribute("retCode", code);
			request.setAttribute("retMsg", message);
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"短信发送请求失败",resultMap));
			return;
		}
	}
	
	@RequestMapping("/api/tgt/ttb/query/json")
	public void queryTutuAmountForStApi(@RequestParam(value="tgt",required=false) String tgt,
			@RequestParam(value="callback",required=false) String callback
			,HttpServletRequest request, HttpServletResponse response){
		Map<String, Object> resultMap = new HashMap<String,Object>();
		if(StringUtils.isBlank(tgt)){
			resultMap = ErrorCode.getErrorCode(56108);
			request.setAttribute("retCode", resultMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", resultMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"缺少tgt参数",null));
			return;
    	}
    	String sAid = getAidFormSSO(tgt);
		if(StringUtils.isBlank(sAid)){
			resultMap.put("code", "0");
			resultMap.put("message", "ST过期或已失效");
			request.setAttribute("retCode", ErrorCode.getErrorCode(56109).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56109).get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"ST过期或已失效",resultMap));
			return;
		} 
		
		String merchantId = request.getParameter("merchantId");
		Long aid = Long.valueOf(sAid);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", aid);
		params.put("spId", 7);
		params.put("appId", "36");
		params.put("areaId", "-1");
		params.put("payTypeId", "-1");
		params.put("eventTimestamp", DateUtil.getCurDateTimeStr());
		params.put("clientIp", IpUtils.getRemoteAddr(request));
		params.put("merchantId", merchantId);
		resultMap = ocpAccountService.queryAmount2(params);
		Passport passport = corePassportService.queryPassport(Long.valueOf(sAid));
		resultMap.put("mobile", StringUtils.isBlank(shortMobile(passport.getMobile()))?"":shortMobile(passport.getMobile()));
		resultMap.put("aid", passport.getId());
		request.setAttribute("retCode", resultMap.get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", resultMap.get(ErrorCode.TIP_INFO));
		writeJsonp(callback, response, new ResultResponse(ResultResponse.SUCCESS,"获取兔兔币余额成功",resultMap));
	}
	
	@RequestMapping("/security/sms/send/json")
	public void sendSmsCode(@RequestParam(value="callback",required=false) String callback
			,HttpServletRequest request, HttpServletResponse response){
		Map<String, Object> resultMap = new HashMap<String,Object>();
		String loginAccount = (String)request.getSession().getAttribute("loginUser");
		if(StringUtils.isBlank(loginAccount)){
			resultMap.put("code", "0");
			resultMap.put("message", "未登录");
			resultMap = ErrorCode.getErrorCode(56102);
			request.setAttribute("retCode", ErrorCode.getErrorCode(56102).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56102).get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未登录",resultMap));
			return;
		} 
		
		Passport passport = corePassportService.queryPassport(loginAccount);
		if(passport == null){
			resultMap.put("code", "0");
			resultMap.put("message", "账号不存在");
			resultMap = ErrorCode.getErrorCode(56103);
			request.setAttribute("retCode", ErrorCode.getErrorCode(56103).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56103).get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"账号不存在",resultMap));
			return;
		}
		
		if(!"1".equals(passport.getMobileAuthed()) || StringUtils.isBlank(passport.getMobile())){
			resultMap = ErrorCode.getErrorCode(56104);
			request.setAttribute("retCode", ErrorCode.getErrorCode(56104).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56104).get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未绑定手机",resultMap));
			return;
		}
		
		Map<String, Object> smsMap = smsService.sendSmsValidateCode(passport.getMobile(), null);
		String code = ObjectUtils.toString(smsMap.get("code"));
		JSONObject data = null;
		if(null!=ObjectUtils.toString(smsMap.get("data"))){
			Map<String,Object> _data = (Map<String, Object>) (smsMap.get("data"));
			String sd = JSON.toJSONString(_data);
			data = JSONObject.parseObject(sd);
		}
		if(!"0".equals(code)){
			String message = ObjectUtils.toString(smsMap.get("message"));
			resultMap = ErrorCode.getErrorCode(56105);
			resultMap.put("data", data);
			request.setAttribute("retCode", resultMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", resultMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,message,resultMap));
    		return;
		}
		request.setAttribute("retCode", smsMap.get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", smsMap.get(ErrorCode.TIP_INFO));
		writeJsonp(callback, response, new ResultResponse(ResultResponse.SUCCESS,"短信发送请求成功",smsMap));
		return;
	}
	
	@RequestMapping("/security/ttb/query/json")
	public void queryTutuAmountApi(@RequestParam(value="callback",required=false) String callback
			,HttpServletRequest request, HttpServletResponse response){
		Map<String, Object> resultMap = new HashMap<String,Object>();
		String loginAccount = (String)request.getSession().getAttribute("loginUser");
		if(StringUtils.isBlank(loginAccount)){
			resultMap.put("code", "0");
			resultMap.put("message", "未登录");
			request.setAttribute("retCode", ErrorCode.getErrorCode(56102).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56102).get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未登录",resultMap));
			return;
		} 
		
		String merchantId = request.getParameter("merchantid");
		Long aid = getLoginId();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", aid);
		params.put("spId", 7);
		params.put("appId", "36");
		params.put("areaId", "-1");
		params.put("payTypeId", "-1");
		params.put("eventTimestamp", DateUtil.getCurDateTimeStr());
		params.put("clientIp", IpUtils.getRemoteAddr(request));
		params.put("merchantId", merchantId);
		resultMap = ocpAccountService.queryAmount2(params);
		Passport passport = corePassportService.queryPassport(loginAccount);
		
		
		resultMap.put("aid", passport.getId());
		resultMap.put("mobileAuthed", passport.getMobileAuthed());
		resultMap.put("mobile", shortMobile(passport.getMobile()));
		request.setAttribute("retCode", resultMap.get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", resultMap.get(ErrorCode.TIP_INFO));
		writeJsonp(callback, response, new ResultResponse(ResultResponse.SUCCESS,"获取兔兔币余额成功",resultMap));
	}
	
	@RequestMapping("/api/tgt/ttb/pay/json")
	public void paymentSt(
			@RequestParam(value="tgt",required=false) String tgt,
			@RequestParam(value="callback",required=false) String callback,
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
			@RequestParam(value="ttbDjjMoney",required=false) String ttbDjjMoney,
			@RequestParam(value="smscode",required=false) String smscode,
			@RequestParam(value="fcbsmscode",required=false) String fcbsmscode,
			@RequestParam(value="yueMoney",required=false) String yueMoney,
			@RequestParam(value="yueCurrency",required=false) String yueCurrency,
			HttpServletRequest request, HttpServletResponse response){
    	
		Map<String, Object> retMap = new HashMap<String,Object>();
		if(StringUtils.isBlank(tgt)){
			retMap = ErrorCode.getErrorCode(56108);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"缺少tgt参数",null));
			return;
    	}
    	String sAid = getAidFormSSO(tgt);
		if(StringUtils.isBlank(sAid)){
			retMap.put("code", "0");
			retMap.put("message", "ST过期或已失效");
			request.setAttribute("retCode", ErrorCode.getErrorCode(56109).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56109).get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"ST过期或已失效",retMap));
			return;
		} 
    	
    	if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		logger.error("订单号为空或长度超出,account:"+account);
    		request.setAttribute("retCode", ErrorCode.getErrorCode(56007).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56007).get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"订单号为空或长度超出",null));
    		return;
    	}
    	
    	if(StringUtils.isBlank(money) || "0".equals(StringUtils.trim(money))){
    		logger.error("订单金额不可以为空或零,account:"+account);
    		request.setAttribute("retCode", ErrorCode.getErrorCode(56008).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56008).get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"money为空或等于零",null));
    		return;
    	}
    	
    	if(StringUtils.isBlank(backendurl) || backendurl.indexOf("http") != 0){
    		logger.error("回调地址不可以为空,account:"+account);
    		request.setAttribute("retCode", ErrorCode.getErrorCode(56009).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56009).get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"backendurl为空或格式错误",null));
    		return;
    	}
    	
    	//判断兔兔币代金卷是否需要短信验证
    	Passport passport = corePassportService.queryPassport(Long.valueOf(sAid));
		if(passport == null){
			request.setAttribute("retCode", ErrorCode.getErrorCode(56103).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56103).get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"账号不存在",null));
    		return;
		}
    	boolean isSms = ocpAccountService.isSmsCheck(ttbDjjMoney,merchantId);
    	if(isSms){
    		if(!"1".equals(passport.getMobileAuthed()) || StringUtils.isBlank(passport.getMobile())){
    			request.setAttribute("retCode", ErrorCode.getErrorCode(56104).get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", ErrorCode.getErrorCode(56104).get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未绑定手机",null));
        		return;
    		}
    		
    		Map<String, Object> smsMap = smsService.validateSmsCode(passport.getMobile(), null, smscode);
    		String code = ObjectUtils.toString(smsMap.get("code"));
    		if(!"20".equals(code)){
    			String message = ObjectUtils.toString(smsMap.get("message"));
    			request.setAttribute("retCode", ErrorCode.getErrorCode(56107).get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", ErrorCode.getErrorCode(56107).get(ErrorCode.TIP_INFO));
        		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,message,null));
        		return;
    		}
    	}
    	
    	//1.创建订单
		HashMap<String, Object> exMap = new HashMap<String, Object>();
		exMap.put("defaultbank", bankCd);
		exMap.put("backendurl", backendurl);
		exMap.put("fontendurl", fontendurl);
		exMap.put("cardtype", cardtype);
		exMap.put("aid", sAid);
		//兔兔币代金卷参数
		exMap.put("ttbDjjMoney", ttbDjjMoney);
		exMap.put("ext", ext);
		
		try{
			mode = paymentOrderService.getOrderMode(mode, bankCd);
			//判断是否是组合支付
	    	if(StringUtils.isNotBlank(yueMoney) && NumberUtils.toFloat(yueMoney) > 0){
	    		//验证翡翠身份
	        	boolean isFcb = ApiBaseController.hasFcbIdentity(request);
	    		if(!isFcb){
	    			logger.error("未拿到翡翠身份");
	    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未拿到翡翠身份",retMap));
	    			return;
	    		}
	    		
	    		//获得翡翠账号、翡翠号码
	    		//String fcbAccount = ApiBaseController.getFcbAccount(request);
	    		String fcbPhone = ApiBaseController.getFcbPhone(request);
	    		
	    		//验证翡翠验证码
	    		Map<String, Object> smsMap = smsService.validateSmsCode(fcbPhone, FCB_SMS_TYPE, fcbsmscode);
	    		String code = ObjectUtils.toString(smsMap.get("code"));
	    		if(!"20".equals(code)){
	    			String message = ObjectUtils.toString(smsMap.get("message"));
	    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,message,retMap));
	    	        return;
	    		}
	    		
    			retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
    					yueCurrency,yueMoney,StringUtils.trim(productName),passport.getAccount(),NumberUtils.toLong(gameId),  
						mode, clientIp, exMap);
	    	}else{
	    		retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
						StringUtils.trim(productName),passport.getAccount(),NumberUtils.toLong(gameId),  
						mode, clientIp, exMap);
	    	}
		} catch (Exception e){
			request.setAttribute("msg", e.getMessage());
			retMap = ErrorCode.getErrorCode(56099);
		}
		
		logger.info("支付订单信息,"+retMap);
		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
		writeJsonp(callback, response, new ResultResponse(ResultResponse.SUCCESS,"支付请求成功",retMap));
		return;
    }
	
	@RequestMapping("/wap/api/tgt/ttb/pay/json")
	public void paymentStWap(
			@RequestParam(value="tgt",required=false) String tgt,
			@RequestParam(value="callback",required=false) String callback,
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
			@RequestParam(value="ttbDjjMoney",required=false) String ttbDjjMoney,
			@RequestParam(value="smscode",required=false) String smscode,
			@RequestParam(value="fcbsmscode",required=false) String fcbsmscode,
			@RequestParam(value="yueMoney",required=false) String yueMoney,
			@RequestParam(value="yueCurrency",required=false) String yueCurrency,
			HttpServletRequest request, HttpServletResponse response){
    	
		Map<String, Object> retMap = new HashMap<String,Object>();
		if(StringUtils.isBlank(tgt)){
			retMap = ErrorCode.getErrorCode(56108);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"缺少tgt参数",retMap));
			return;
    	}
    	String sAid = getAidFormSSO(tgt);
		if(StringUtils.isBlank(sAid)){
			retMap.put("code", "0");
			retMap.put("message", "ST过期或已失效");
			retMap = ErrorCode.getErrorCode(56109);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"ST过期或已失效",retMap));
			return;
		} 
    	
    	if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		logger.error("订单号为空或长度超出,account:"+account);
    		retMap = ErrorCode.getErrorCode(56007);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"订单号为空或长度超出",retMap));
    		return;
    	}
    	
    	if(StringUtils.isBlank(money) || "0".equals(StringUtils.trim(money))){
    		logger.error("订单金额不可以为空或零,account:"+account);
    		retMap = ErrorCode.getErrorCode(56008);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"money为空或等于零",retMap));
    		return;
    	}
    	
    	if(StringUtils.isBlank(backendurl) || backendurl.indexOf("http") != 0){
    		logger.error("回调地址不可以为空,account:"+account);
    		retMap = ErrorCode.getErrorCode(56009);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"backendurl为空或格式错误",retMap));
    		return;
    	}
    	
    	//判断兔兔币代金卷是否需要短信验证
    	Passport passport = corePassportService.queryPassport(Long.valueOf(sAid));
		if(passport == null){
			retMap = ErrorCode.getErrorCode(56103);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"账号不存在",retMap));
    		return;
		}
    	boolean isSms = ocpAccountService.isSmsCheck(ttbDjjMoney,merchantId);
    	if(isSms){
    		if(!"1".equals(passport.getMobileAuthed()) || StringUtils.isBlank(passport.getMobile())){
    			retMap = ErrorCode.getErrorCode(56104);
    			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未绑定手机",retMap));
        		return;
    		}
    		
    		Map<String, Object> smsMap = smsService.validateSmsCode(passport.getMobile(), null, smscode);
    		String code = ObjectUtils.toString(smsMap.get("code"));
    		if(!"20".equals(code)){
    			String message = ObjectUtils.toString(smsMap.get("message"));
    			retMap = ErrorCode.getErrorCode(56107);
    			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,message,retMap));
        		return;
    		}
    	}
    	
    	//1.创建订单
		HashMap<String, Object> exMap = new HashMap<String, Object>();
		exMap.put("defaultbank", bankCd);
		exMap.put("backendurl", backendurl);
		exMap.put("fontendurl", fontendurl);
		exMap.put("cardtype", cardtype);
		exMap.put("aid", sAid);
		//兔兔币代金卷参数
		exMap.put("ttbDjjMoney", ttbDjjMoney);
		exMap.put("ext", ext);
		
		try{
			mode = paymentOrderService.getOrderMode(mode, bankCd);
			//判断是否是组合支付
	    	if(StringUtils.isNotBlank(yueMoney) && NumberUtils.toFloat(yueMoney) > 0){
	    		//验证翡翠身份
	        	boolean isFcb = ApiBaseController.hasFcbIdentity(request);
	    		if(!isFcb){
	    			logger.error("未拿到翡翠身份");
	    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未拿到翡翠身份",retMap));
	    			return;
	    		}
	    		
	    		//获得翡翠账号、翡翠号码
	    		//String fcbAccount = ApiBaseController.getFcbAccount(request);
	    		String fcbPhone = ApiBaseController.getFcbPhone(request);
	    		
	    		//验证翡翠验证码
	    		Map<String, Object> smsMap = smsService.validateSmsCode(fcbPhone, FCB_SMS_TYPE, fcbsmscode);
	    		String code = ObjectUtils.toString(smsMap.get("code"));
	    		if(!"20".equals(code)){
	    			String message = ObjectUtils.toString(smsMap.get("message"));
	    			request.setAttribute("retCode", ErrorCode.getErrorCode(56107).get(ErrorCode.TIP_CODE));
	    			request.setAttribute("retMsg", ErrorCode.getErrorCode(56107).get(ErrorCode.TIP_INFO));
	    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,message,retMap));
	    	        return;
	    		}
	    		
    			retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
    					yueCurrency,yueMoney,StringUtils.trim(productName),passport.getAccount(),NumberUtils.toLong(gameId),  
						mode, clientIp, exMap);
	    	}else{
	    		retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
						StringUtils.trim(productName),passport.getAccount(),NumberUtils.toLong(gameId),  
						mode, clientIp, exMap);
	    	}
		} catch (Exception e){
			request.setAttribute("msg", e.getMessage());
			retMap = ErrorCode.getErrorCode(56099);
		}
		
		logger.info("支付订单信息,"+retMap);
		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
		writeJsonp(callback, response, new ResultResponse(ResultResponse.SUCCESS,"支付请求成功",retMap));
		return;
    }
	
	@RequestMapping(value={"/security/ttb/pay/json"})
	public void payment(
			@RequestParam(value="callback",required=false) String callback,
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
			@RequestParam(value="ttbDjjMoney",required=false) String ttbDjjMoney,
			@RequestParam(value="smscode",required=false) String smscode,
			@RequestParam(value="fcbsmscode",required=false) String fcbsmscode,
			@RequestParam(value="yueMoney",required=false) String yueMoney,
			@RequestParam(value="yueCurrency",required=false) String yueCurrency,
			HttpServletRequest request, HttpServletResponse response){
    	
    	Map<String,Object> retMap = new HashMap<String, Object>();
    	
    	String loginAccount = (String)request.getSession().getAttribute("loginUser");
		if(StringUtils.isBlank(loginAccount)){
			retMap.put("code", "0");
			retMap.put("message", "未登录");
			request.setAttribute("retCode", ErrorCode.getErrorCode(56102).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56102).get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未登录",retMap));
			return;
		}
    	
    	if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		logger.error("订单号为空或长度超出,account:"+account);
    		retMap = ErrorCode.getErrorCode(56007);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"订单号为空或长度超出",retMap));
    		return;
    	}
    	
    	if(StringUtils.isBlank(money) || "0".equals(StringUtils.trim(money))){
    		logger.error("订单金额不可以为空或零,account:"+account);
    		retMap = ErrorCode.getErrorCode(56008);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"money为空或等于零",retMap));
    		return;
    	}
    	
    	if(StringUtils.isBlank(backendurl) || backendurl.indexOf("http") != 0){
    		logger.error("回调地址不可以为空,account:"+account);
    		retMap = ErrorCode.getErrorCode(56009);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"backendurl为空或格式错误",retMap));
    		return;
    	}
    	
    	//判断兔兔币代金卷是否需要短信验证
    	Passport passport = corePassportService.queryPassport(loginAccount);
		if(passport == null){
			retMap = ErrorCode.getErrorCode(56103);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"账号不存在",retMap));
    		return;
		}
		
    	boolean isSms = ocpAccountService.isSmsCheck(ttbDjjMoney,merchantId);
    	if(isSms){
    		if(!"1".equals(passport.getMobileAuthed()) || StringUtils.isBlank(passport.getMobile())){
    			retMap = ErrorCode.getErrorCode(56104);
    			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未绑定手机",retMap));
        		return;
    		}
    		
    		Map<String, Object> smsMap = smsService.validateSmsCode(passport.getMobile(), null, smscode);
    		String code = ObjectUtils.toString(smsMap.get("code"));
    		if(!"20".equals(code)){
    			if("24".equals(code)){
    				String message = ObjectUtils.toString(smsMap.get("message"));
        			retMap = ErrorCode.getErrorCode(56110);
        			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
        			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
            		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,message,retMap));
            		return;
    			}
    			String message = ObjectUtils.toString(smsMap.get("message"));
    			retMap = ErrorCode.getErrorCode(56107);
    			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
        		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,message,retMap));
        		return;
    		}
    	}
    	
    	//1.创建订单
		HashMap<String, Object> exMap = new HashMap<String, Object>();
		exMap.put("defaultbank", bankCd);
		exMap.put("backendurl", backendurl);
		exMap.put("fontendurl", fontendurl);
		exMap.put("cardtype", cardtype);
		//兔兔币代金卷参数
		exMap.put("ttbDjjMoney", ttbDjjMoney);
		exMap.put("ext", ext);
		
		try{
			mode = paymentOrderService.getOrderMode(mode, bankCd);
			//判断是否是组合支付
	    	if(StringUtils.isNotBlank(yueMoney) && NumberUtils.toFloat(yueMoney) > 0){
	    		//验证翡翠身份
	        	boolean isFcb = ApiBaseController.hasFcbIdentity(request);
	    		if(!isFcb){
	    			logger.error("未拿到翡翠身份");
	    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未拿到翡翠身份",retMap));
	    			return;
	    		}
	    		
	    		//获得翡翠账号、翡翠号码
	    		//String fcbAccount = ApiBaseController.getFcbAccount(request);
	    		String fcbPhone = ApiBaseController.getFcbPhone(request);
	    		
	    		//验证翡翠验证码
	    		Map<String, Object> smsMap = smsService.validateSmsCode(fcbPhone, FCB_SMS_TYPE, fcbsmscode);
	    		String code = ObjectUtils.toString(smsMap.get("code"));
	    		if(!"20".equals(code)){
	    			String message = ObjectUtils.toString(smsMap.get("message"));
	    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,message,retMap));
	    	        return;
	    		}
	    		
    			retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
    					yueCurrency,yueMoney,StringUtils.trim(productName),passport.getAccount(),NumberUtils.toLong(gameId),  
						mode, clientIp, exMap);
	    	}else{
	    		retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
						StringUtils.trim(productName),passport.getAccount(),NumberUtils.toLong(gameId),  
						mode, clientIp, exMap);
	    	}
		} catch (Exception e){
			request.setAttribute("msg", e.getMessage());
			retMap = ErrorCode.getErrorCode(56099);
		}
		
		logger.info("支付订单信息,"+retMap);
		Map<String, Object> paymentParams = (Map<String, Object>) retMap.get("paymentParams");
		if(null!=paymentParams.get(ErrorCode.TIP_CODE) && !paymentParams.get(ErrorCode.TIP_CODE).equals("1")){
			retMap.put(ErrorCode.TIP_CODE, paymentParams.get(ErrorCode.TIP_CODE));
			retMap.put(ErrorCode.TIP_INFO, paymentParams.get(ErrorCode.TIP_INFO));
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.SUCCESS,"支付请求失败",retMap));
			return;
		}
		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
		writeJsonp(callback, response, new ResultResponse(ResultResponse.SUCCESS,"支付请求成功",retMap));
		return;
    }
	
	private String getAidFormSSO(String tgt){
		String aid = "";
		if(StringUtils.isEmpty(tgt)){
			return "";
		}
		
		try {
			String requestStUrl = "https://sso.woniu.com/v1/tickets/"+tgt;
			Map<String,String> nameValuePair = new HashMap<String,String>();
			nameValuePair.put("service", "http://cashier.woniu.com");
			String serviceTicket = HttpUtils.post(requestStUrl, nameValuePair);
			
			String validStUrl = "https://sso.woniu.com/serviceValidate";
			nameValuePair = new HashMap<String,String>();
			nameValuePair.put("ticket", serviceTicket);
			nameValuePair.put("service", "http://cashier.woniu.com");
			
			String casRet = HttpUtils.post(validStUrl, nameValuePair);

			aid = readXmlNode(casRet,"/serviceResponse/authenticationSuccess/attributes/naid");
		} catch (ClientProtocolException e) {
		} catch (Exception e) {}
		
		return aid;
	}
	
	private static String readXmlNode(String resData,String nodePath) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		ByteArrayInputStream bi;
		String name = "";
		try {
			bi = new ByteArrayInputStream(resData.getBytes("utf-8"));
			org.w3c.dom.Document doc = factory.newDocumentBuilder().parse(bi);
			XPathFactory xFactory = XPathFactory.newInstance();
			XPath xPath = xFactory.newXPath();

			name = (String) xPath.compile(nodePath).evaluate(doc,XPathConstants.STRING);
			System.out.println(name);
		} catch (Exception e) {
		}
		
		return name;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value={"/wap/api/security/ttb/pay/json"})
	public void paymentWap(
			@RequestParam(value="callback",required=false) String callback,
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
			@RequestParam(value="ttbDjjMoney",required=false) String ttbDjjMoney,
			@RequestParam(value="smscode",required=false) String smscode,
			@RequestParam(value="fcbsmscode",required=false) String fcbsmscode,
			@RequestParam(value="yueMoney",required=false) String yueMoney,
			@RequestParam(value="yueCurrency",required=false) String yueCurrency,
			HttpServletRequest request, HttpServletResponse response){
    	
    	Map<String,Object> retMap = new HashMap<String, Object>();
    	
    	String loginAccount = (String)request.getSession().getAttribute("loginUser");
		if(StringUtils.isBlank(loginAccount)){
			retMap.put("code", "0");
			retMap.put("message", "未登录");
			request.setAttribute("retCode", ErrorCode.getErrorCode(56102).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56102).get(ErrorCode.TIP_INFO));
			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未登录",retMap));
			return;
		}
    	
    	if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		logger.error("订单号为空或长度超出,account:"+account);
    		retMap = ErrorCode.getErrorCode(56007);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"订单号为空或长度超出",null));
    		return;
    	}
    	
    	if(StringUtils.isBlank(money) || "0".equals(StringUtils.trim(money))){
    		logger.error("订单金额不可以为空或零,account:"+account);
    		retMap = ErrorCode.getErrorCode(56008);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"money为空或等于零",null));
    		return;
    	}
    	
    	if(StringUtils.isBlank(backendurl) || backendurl.indexOf("http") != 0){
    		logger.error("回调地址不可以为空,account:"+account);
    		retMap = ErrorCode.getErrorCode(56009);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"backendurl为空或格式错误",null));
    		return;
    	}
    	
    	//判断兔兔币代金卷是否需要短信验证
    	Passport passport = corePassportService.queryPassport(loginAccount);
		if(passport == null){
			retMap = ErrorCode.getErrorCode(56103);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"账号不存在",null));
    		return;
		}
		
    	boolean isSms = ocpAccountService.isSmsCheck(ttbDjjMoney,merchantId);
    	if(isSms){
    		if(!"1".equals(passport.getMobileAuthed()) || StringUtils.isBlank(passport.getMobile())){
    			retMap = ErrorCode.getErrorCode(56104);
    			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
        		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未绑定手机",null));
        		return;
    		}
    		
    		Map<String, Object> smsMap = smsService.validateSmsCode(passport.getMobile(), null, smscode);
    		String code = ObjectUtils.toString(smsMap.get("code"));
    		if(!"20".equals(code)){
    			String message = ObjectUtils.toString(smsMap.get("message"));
    			retMap = ErrorCode.getErrorCode(56107);
    			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
        		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,message,retMap));
        		return;
    		}
    	}
    	
    	//1.创建订单
		HashMap<String, Object> exMap = new HashMap<String, Object>();
		exMap.put("defaultbank", bankCd);
		exMap.put("backendurl", backendurl);
		exMap.put("fontendurl", fontendurl);
		exMap.put("cardtype", cardtype);
		//兔兔币代金卷参数
		exMap.put("ttbDjjMoney", ttbDjjMoney);
		exMap.put("ext", ext);
		
		try{
			mode = paymentOrderService.getOrderMode(mode, bankCd);
			//判断是否是组合支付
	    	if(StringUtils.isNotBlank(yueMoney) && NumberUtils.toFloat(yueMoney) > 0){
	    		//验证翡翠身份
	        	boolean isFcb = ApiBaseController.hasFcbIdentity(request);
	    		if(!isFcb){
	    			logger.error("未拿到翡翠身份");
	    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未拿到翡翠身份",retMap));
	    			return;
	    		}
	    		
	    		//获得翡翠账号、翡翠号码
	    		//String fcbAccount = ApiBaseController.getFcbAccount(request);
	    		String fcbPhone = ApiBaseController.getFcbPhone(request);
	    		
	    		//验证翡翠验证码
	    		Map<String, Object> smsMap = smsService.validateSmsCode(fcbPhone, FCB_SMS_TYPE, fcbsmscode);
	    		String code = ObjectUtils.toString(smsMap.get("code"));
	    		if(!"20".equals(code)){
	    			String message = ObjectUtils.toString(smsMap.get("message"));
	    			writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,message,retMap));
	    	        return;
	    		}
	    		
    			retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
    					yueCurrency,yueMoney,StringUtils.trim(productName),passport.getAccount(),NumberUtils.toLong(gameId),  
						mode, clientIp, exMap);
	    	}else{
	    		retMap = paymentFacade.createOrder(orderNo, NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),money,
						StringUtils.trim(productName),passport.getAccount(),NumberUtils.toLong(gameId),  
						mode, clientIp, exMap);
	    	}
		} catch (Exception e){
			request.setAttribute("msg", e.getMessage());
			retMap = ErrorCode.getErrorCode(56099);
		}
		
		logger.info("支付订单信息,"+retMap);
		
		if (retMap != null && StringUtils.equalsIgnoreCase(ObjectUtils.toString(retMap.get(ErrorCode.TIP_CODE)), "1")) {
    		
    		Map<String, String> reqmap = (Map<String, String>)retMap.get("paymentParams");
    		String paycode = ObjectUtils.toString(reqmap.get("msgcode"));
    		if("1".equals(paycode)){
        		//支付请求成功
    			PaymentOrder paymentOrder = (PaymentOrder)retMap.get("paymentOrder");
    			request.setAttribute(PaymentConstant.PAYMENT_ORDER, paymentOrder);
    			request.setAttribute("orderNo", orderNo);
    			request.setAttribute("money", paymentOrder.getMoney());
    			request.setAttribute("productName", productName);
    			request.setAttribute("createDate", DateUtil.parseDate2Str(paymentOrder.getCreateDate(), DateUtil.DATE_FORMAT_DATETIME));
    			
    			retMap.put(ErrorCode.TIP_CODE, ErrorCode.getErrorCode(1).get(ErrorCode.TIP_CODE));
    			retMap.put(ErrorCode.TIP_INFO, ErrorCode.getErrorCode(1).get(ErrorCode.TIP_INFO));
    			retMap.put("createDate", DateUtil.parseDate2Str(paymentOrder.getCreateDate(), DateUtil.DATE_FORMAT_DATETIME));
    			retMap.put("paymentPlatform", null);
    			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    			writeJsonp(callback, response, new ResultResponse(ResultResponse.SUCCESS,"创建订单成功",retMap));
    	        return;
        	}else{
        		//支付请求失败
        		Map<String,Object> errorMap = ErrorCode.getErrorCode(paycode);
        		if(StringUtils.isNotEmpty(ObjectUtils.toString(errorMap.get(ErrorCode.TIP_INFO)))){
        			request.setAttribute("msg", ObjectUtils.toString(errorMap.get(ErrorCode.TIP_INFO)));
        		}else{
        			request.setAttribute("msg", "支付请求失败，失败码["+paycode+"]");
        		}
        		logger.info("订单号：" + ((PaymentOrder)retMap.get("paymentOrder")).getOrderNo()+",支付请求失败，失败码["+paycode+"]");
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
            	retMap = ErrorCode.getErrorCode(56098);
            	writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
                return;
        	}
        }
		
		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"创建订单失败",retMap));
		return;
    }
	
	@RequestMapping("/tutu/pay/success")
	@ResponseBody
	public ModelAndView tutuPaySuccess(HttpServletRequest request,
			@RequestParam(value = "orderno") String orderNo,
			@RequestParam(value = "productname") String productName) {
		
		Map<String,Object> retMap = new HashMap<String, Object>();
		String loginAccount = (String)request.getSession().getAttribute("loginUser");
		if(StringUtils.isBlank(loginAccount)){
    		request.setAttribute("retCode", ErrorCode.getErrorCode(56102).get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", ErrorCode.getErrorCode(56102).get(ErrorCode.TIP_INFO));
    		return new ModelAndView(ORDER_ERROR_PAGE);
		}
		if(StringUtils.isBlank(orderNo)){
			return new ModelAndView(ORDER_ERROR_PAGE);
    	}
    	PaymentOrder paymentOrder = paymentOrderService.queryOrderByPartnerOrderNo(orderNo);
    	if(paymentOrder ==null){
    		return new ModelAndView(ORDER_ERROR_PAGE);
    	}
		if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		request.setAttribute("msg", "订单号为空或长度超出");
    		retMap = ErrorCode.getErrorCode(56007);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return new ModelAndView(ORDER_ERROR_PAGE);
    	}
		retMap = ErrorCode.getErrorCode(1);
		request.setAttribute(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		request.setAttribute("orderNo", orderNo);
		request.setAttribute("money", paymentOrder.getMoney());
		request.setAttribute("productName", productName);
		request.setAttribute("createDate", DateUtil.parseDate2Str(paymentOrder.getCreateDate(), DateUtil.DATE_FORMAT_DATETIME));
		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
		return new ModelAndView(PAYMENT_SUCCESS_PAGE);
	}
	
	public static void main(String[] args) {
		String cas_success = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"+
								"<cas:authenticationSuccess>"+
									"<cas:user>testaaaa9</cas:user>"+
									"<cas:attributes>"+
										"<cas:naid>1502464977</cas:naid>"+
										"<cas:SSOPrincipal>TESTAAAA9</cas:SSOPrincipal>"+
										"<cas:snailPassport>TESTAAAA9</cas:snailPassport>"+
									"</cas:attributes>"+
								"</cas:authenticationSuccess>"+
							"</cas:serviceResponse>";
		
		String cas = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"+
						"<cas:authenticationFailure code='INVALID_TICKET'>"+
							"未能够识别出目标 &#039;abc&#039;票根"+
						"</cas:authenticationFailure>"+
					 "</cas:serviceResponse>";	
		
		System.out.println(readXmlNode(cas,"/serviceResponse/authenticationSuccess/attributes/naid"));
	}
}
