package com.woniu.sncp.pay;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.CreditStage;
import com.woniu.pay.pojo.GamePropsCurrency;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.lang.DateUtil;
import com.woniu.sncp.lang.ObjectUtil;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.core.service.CorePassportService;
import com.woniu.sncp.pay.core.service.PaymentMerchantService;
import com.woniu.sncp.pay.core.service.PaymentOrderService;
import com.woniu.sncp.pay.core.service.fcb.FcbService;
import com.woniu.sncp.pay.repository.pay.PaymentMerchant;
import com.woniu.sncp.pay.repository.pay.PaymentMerchantDetail;
import com.woniu.sncp.pay.web.api.ApiBaseController;
import com.woniu.sncp.pojo.passport.Passport;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.web.CookieUtil;
import com.woniu.sncp.web.IpUtils;
import com.woniu.sncp.web.response.ResultResponse;

@Controller
public class IndexController extends ApiBaseController{
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private PaymentMerchantService paymentMerchantService;
	@Autowired
	private PaymentOrderService paymentOrderService;
	@Autowired
	private FcbService fcbService;
	@Autowired
	private CorePassportService corePassportService;
	
	private final static String ORDER_CONFIRM_PAGE = "/payment/payment_confirm";
	private final static String ORDER_ERROR_PAGE = "/payment/error";
	private final static String COOKIE_FROM_KEY = "c_r";
	
	
	@RequestMapping("/")
    public String home(HttpServletRequest req) {
		return "redirect:/index.html";
    }
	@RequestMapping("/405")
	public String error405Page(HttpServletRequest req) {
		return "/405";
	}
	@RequestMapping("/404")
	public String error404Page(HttpServletRequest req) {
		return "/404";
	}
	@RequestMapping("/400")
	public String error400Page(HttpServletRequest req) {
		return "/400";
	}
//	@RequestMapping("/error")
//    public String errorPage(HttpServletRequest req) {
//		String referer = CookieUtil.getCookieByName(req, COOKIE_FROM_KEY).getValue();
//		try{
//			req.setAttribute("referer", URLDecoder.decode(referer,"utf-8"));
//		} catch (Exception e){
//			
//		}
//		return ORDER_ERROR_PAGE;
//    }
	
    /**
     * 收银台首页
     * 
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
    @RequestMapping("/payment/page")
	public String paymentPage(@RequestParam(value="orderno") String orderNo,
			@RequestParam(value="merchantid") Long merchantId,
			@RequestParam(value="gameid",required=false) Long gameId,
			@RequestParam(value="account",required=false) String account,
			@RequestParam(value="money") String money,
			@RequestParam(value="clientip") String clientIp,
			@RequestParam(value="imprestmode",required=false) String imprestMode,
			@RequestParam(value="productname") String productName,
			@RequestParam(value="backendurl") String backendurl,
			@RequestParam(value="fontendurl",required=false) String fontendurl,
			@RequestParam(value="sign") String sign,
			@RequestParam(value="ext",required=false) String ext,
			@RequestParam(value="body",required=false) String body,
			@RequestParam(value="goodsDetail",required=false) String goodsDetail,
			@RequestParam(value="terminalType",required=false) String terminalType,
			@RequestParam(value="timeoutExpress",required=false) String timeoutExpress,
			HttpServletRequest request, HttpServletResponse response) {
    	if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		logger.error("订单号为空或长度超出,account:"+account);
    		request.setAttribute("msg", "订单号为空或长度超出");
    		return ORDER_ERROR_PAGE;
    	}
    	
    	if(StringUtils.isBlank(money) || "0".equals(StringUtils.trim(money))){
    		logger.error("订单金额不可以为空或零,account:"+account);
    		request.setAttribute("msg", "money为空或等于零");
    		return ORDER_ERROR_PAGE;
    	}
    	
    	if(StringUtils.isBlank(backendurl) || backendurl.indexOf("http") != 0){
    		logger.error("回调地址不可以为空,account:"+account);
    		request.setAttribute("msg", "backendurl为空或格式错误");
    		return ORDER_ERROR_PAGE;
    	}
    	
    	Map<String, Object> retMap = putToMap(request,request.getParameterNames());
    	
    	//查询可支付平台
    	List<PaymentMerchantDetail> merchantDtl = paymentMerchantService.queryPaymentMerchantDtl(merchantId);
    	
    	List<PaymentMerchantDetail> paymentDebitList = new ArrayList<PaymentMerchantDetail>();
    	List<PaymentMerchantDetail> paymentCreditList = new ArrayList<PaymentMerchantDetail>();
    	List<PaymentMerchantDetail> paymentThirdList = new ArrayList<PaymentMerchantDetail>();
    	List<PaymentMerchantDetail> paymentTtbList = new ArrayList<PaymentMerchantDetail>();
    	List<PaymentMerchantDetail> paymentYxCardList = new ArrayList<PaymentMerchantDetail>();
    	List<PaymentMerchantDetail> paymentMobileCardList = new ArrayList<PaymentMerchantDetail>();
    	List<PaymentMerchantDetail> paymentWnMobileSpecCardList = new ArrayList<PaymentMerchantDetail>();//蜗牛移动全能充值卡
    	List<PaymentMerchantDetail> paymentFcbList = new ArrayList<PaymentMerchantDetail>();
    	for (PaymentMerchantDetail paymentMerchantDetail : merchantDtl) {
			if(PaymentMerchantDetail.TYPE_BANK.equals(paymentMerchantDetail.getType())){
				paymentDebitList.add(paymentMerchantDetail);
			} else if (PaymentMerchantDetail.TYPE_CREDIT_STAGE.equals(paymentMerchantDetail.getType())){
				paymentCreditList.add(paymentMerchantDetail);
			} else if (PaymentMerchantDetail.TYPE_THIRD.equals(paymentMerchantDetail.getType())){
				paymentThirdList.add(paymentMerchantDetail);
			} else if( (PaymentMerchantDetail.TYPE_TTB.equals(paymentMerchantDetail.getType())) || (PaymentMerchantDetail.TYPE_TTB_PC.equals(paymentMerchantDetail.getType()))){
				paymentTtbList.add(paymentMerchantDetail);
			} else if((PaymentMerchantDetail.TYPE_YX_CARD.equals(paymentMerchantDetail.getType())) 
					&& (!PaymentConstant.WN_GAME_CARD.equals(paymentMerchantDetail.getContent())) ){
				paymentYxCardList.add(paymentMerchantDetail);
			} else if(PaymentMerchantDetail.TYPE_YX_MOBILE.equals(paymentMerchantDetail.getType())){
				paymentMobileCardList.add(paymentMerchantDetail);
			} else if(PaymentMerchantDetail.TYPE_WEB_YUE.equals(paymentMerchantDetail.getType())){
				//如果含有余额支付，则要进行身份验证
				String currencyId = paymentMerchantDetail.getContent();
				GamePropsCurrency currency = fcbService.queryById(Long.valueOf(currencyId));
				//验证身份
				Map<String, Object> tokenMap = fcbService.validateToken(ext);
				if(tokenMap == null)
					continue;
				String tokenCode = ObjectUtils.toString(tokenMap.get("code"));
				if(!"0".equals(tokenCode)){
					//身份认证失败
					logger.info("翡翠币身份认证失败,token:" + ext);
					continue;
				}
				Map<String, Object> dataMap =  (Map<String, Object>)tokenMap.get("data");
				String fcbAccount = ObjectUtils.toString(dataMap.get("account"));
				String fcbPhone = ObjectUtils.toString(dataMap.get("phone"));
				super.registFcbIdentity(request, fcbAccount, fcbPhone);
				
				//查询余额
				Map<String, Object> fcbMap = null;
				try {
					fcbMap = this.queryFcbAmount(fcbAccount, currency.getGameId(), IpUtils.getRemoteAddr(request));
				} catch (Exception e) {
					logger.error("查询翡翠币余额异常：" + e.getMessage());
					continue;
				}
				retMap.put("fcbInfo", fcbMap.get("fcbInfo"));
				retMap.put("fcbPhone", fcbPhone);
				retMap.put("fcbAccount", fcbAccount);
				paymentFcbList.add(paymentMerchantDetail);
			}
			
			if( (PaymentMerchantDetail.TYPE_WN_MOBILE_SPEC_CARD.equalsIgnoreCase(paymentMerchantDetail.getType())
					&& PaymentConstant.WN_QMOBILE_CARD.equalsIgnoreCase(paymentMerchantDetail.getContent())) || 
					(PaymentMerchantDetail.TYPE_WN_MOBILE_SPEC_CARD.equalsIgnoreCase(paymentMerchantDetail.getType())
							&& PaymentConstant.WN_MOBILE_CARD.equalsIgnoreCase(paymentMerchantDetail.getContent())) || 
					( PaymentMerchantDetail.TYPE_YX_CARD.equalsIgnoreCase(paymentMerchantDetail.getType()) && PaymentConstant.WN_GAME_CARD.equals(paymentMerchantDetail.getContent()))){
				//蜗牛移动充值卡+游戏一卡通
				paymentWnMobileSpecCardList.add(paymentMerchantDetail);
			}
		}

    	//组装平台列表
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	retMap.put("paydate", sdf.format(new Date()));
    	
    	retMap.put("debitlist", paymentDebitList);
    	retMap.put("creditlist", paymentCreditList);
    	retMap.put("thirdlist", paymentThirdList);
    	retMap.put("ttbList", paymentTtbList);
    	retMap.put("yxCardList", paymentYxCardList);
    	retMap.put("mobileCardList", paymentMobileCardList);
    	//蜗牛移动全能充值卡
    	retMap.put("wnMobileSpecCardList", paymentWnMobileSpecCardList);
    	//翡翠币
    	retMap.put("fcbList", paymentFcbList);
    	
    	request.setAttribute("ret", retMap);
    	
    	//错误页面返回首页
    	String referer = request.getHeader("Referer");
    	CookieUtil.addCookie(response, COOKIE_FROM_KEY, URLEncoder.encode(referer), 0);
    	return ORDER_CONFIRM_PAGE;
    }
    
    /**
     * 收银台首页
     * 
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
    @RequestMapping("/payment/wap")
	public void paymentWapApi(@RequestParam(value="orderno") String orderNo,
			@RequestParam(value="merchantid") Long merchantId,
			@RequestParam(value="gameid",required=false) Long gameId,
			@RequestParam(value="account",required=false) String account,
			@RequestParam(value="callback",required=false) String callback,
			@RequestParam(value="ext",required=false) String ext,
			@RequestParam(value="body",required=false) String body,
			@RequestParam(value="goodsDetail",required=false) String goodsDetail,
			@RequestParam(value="terminalType",required=false) String terminalType,
			@RequestParam(value="timeoutExpress",required=false) String timeoutExpress,
			HttpServletRequest request, HttpServletResponse response) {
    	
    	Map<String,Object> retMap = new HashMap<String,Object>();
    	if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		logger.error("订单号为空或长度超出,account:"+account);
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"订单号不可以为空",null));
    		return;
    	}
    	
    	//查询可支付平台
    	List<PaymentMerchantDetail> merchantDtl = paymentMerchantService.queryPaymentMerchantDtl(merchantId);
    	
    	List<PaymentMerchantDetail> paymentList = new ArrayList<PaymentMerchantDetail>();
    	List<PaymentMerchantDetail> bankList = new ArrayList<PaymentMerchantDetail>();
    	List<PaymentMerchantDetail> fcbList = new ArrayList<PaymentMerchantDetail>();
    	
    	for (PaymentMerchantDetail paymentMerchantDetail : merchantDtl) {
			if(PaymentMerchantDetail.TYPE_WAP.equalsIgnoreCase(paymentMerchantDetail.getType())){
				paymentList.add(paymentMerchantDetail);
			} 
			if( (PaymentMerchantDetail.TYPE_TTB.equals(paymentMerchantDetail.getType())) || (PaymentMerchantDetail.TYPE_TTB_WAP.equals(paymentMerchantDetail.getType()))){
				paymentList.add(paymentMerchantDetail);
			}
			//游戏充值卡wap
			if(PaymentMerchantDetail.TYPE_WN_GAME_WAP_CARD.equals(paymentMerchantDetail.getType())){
				paymentList.add(paymentMerchantDetail);
			}
			//手机充值卡wap
			if(PaymentMerchantDetail.TYPE_WN_MOBILE_WAP_CARD.equals(paymentMerchantDetail.getType())){
				paymentList.add(paymentMerchantDetail);
			}
			//蜗牛充值卡wap
			if(PaymentMerchantDetail.TYPE_WN_QMOBILE_WAP_CARD.equals(paymentMerchantDetail.getType())){
				paymentList.add(paymentMerchantDetail);
			}
			
			//快钱快捷wap-借记卡
			if(PaymentMerchantDetail.TYPE_QUICK_BANK_DEBIT.equals(paymentMerchantDetail.getType())){
				bankList.add(paymentMerchantDetail);
			}
			//快钱快捷wap-信用卡
			if(PaymentMerchantDetail.TYPE_QUICK_BANK_CREDIT.equals(paymentMerchantDetail.getType())){
				bankList.add(paymentMerchantDetail);
			}
			if(PaymentMerchantDetail.TYPE_WAP_YUE.equals(paymentMerchantDetail.getType())){
				//如果含有余额支付，则要进行身份验证
				String currencyId = paymentMerchantDetail.getContent();
				GamePropsCurrency currency = fcbService.queryById(Long.valueOf(currencyId));
				//验证身份
				Map<String, Object> tokenMap = fcbService.validateToken(ext);
				if(tokenMap == null)
					continue;
				String tokenCode = ObjectUtils.toString(tokenMap.get("code"));
				if(!"0".equals(tokenCode)){
					//身份认证失败
					logger.info("翡翠币身份认证失败,token:" + ext);
					continue;
				}
				Map<String, Object> dataMap =  (Map<String, Object>)tokenMap.get("data");
				String fcbAccount = ObjectUtils.toString(dataMap.get("account"));
				String fcbPhone = ObjectUtils.toString(dataMap.get("phone"));
				super.registFcbIdentity(request, fcbAccount, fcbPhone);
				
				//查询余额
				Map<String, Object> fcbMap = null;
				try {
					fcbMap = this.queryFcbAmount(fcbAccount, currency.getGameId(), IpUtils.getRemoteAddr(request));
				} catch (Exception e) {
					logger.error("查询翡翠币余额异常：" + e.getMessage());
					continue;
				}
				retMap.put("fcbInfo", fcbMap.get("fcbInfo"));
				retMap.put("fcbPhone", fcbPhone);
				retMap.put("fcbAccount", fcbAccount);
				fcbList.add(paymentMerchantDetail);
			}
		}

    	if(paymentList == null || paymentList.isEmpty()){
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未获取到支付方式",null));
    		return;
    	}
    	    	
    	retMap.put("payments", paymentList);
    	retMap.put("bankList",bankList);
    	retMap.put("fcbList",fcbList);
    	
    	PaymentOrder order = paymentOrderService.queryOrderByPartnerOrderNo(orderNo,merchantId);
    	retMap.put("order", order);
    	
    	writeJsonp(callback, response, new ResultResponse(ResultResponse.SUCCESS,"获取支付方式成功",retMap));
    }
    
    /**
     * 收银台首页
     * 
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
    @RequestMapping("/payment/app")
	public void paymentAppApi(@RequestParam(value="orderno") String orderNo,
			@RequestParam(value="merchantid") Long merchantId,
			@RequestParam(value="type") String type,
			@RequestParam(value="gameid",required=false) Long gameId,
			@RequestParam(value="account",required=false) String account,
			@RequestParam(value="callback",required=false) String callback,
			@RequestParam(value="ext",required=false) String ext,
			@RequestParam(value="body",required=false) String body,
			@RequestParam(value="goodsDetail",required=false) String goodsDetail,
			@RequestParam(value="terminalType",required=false) String terminalType,
			@RequestParam(value="timeoutExpress",required=false) String timeoutExpress,
			HttpServletRequest request, HttpServletResponse response) {
    	
    	logRequestParams("(" + request.getMethod() + ")"
				+ request.getRequestURL().toString(), request);
    	
    	Map<String,Object> retMap = new HashMap<String,Object>();
    	if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		logger.error("订单号为空或长度超出,account:"+account);
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"订单号不可以为空",null));
    		return;
    	}
    	
    	//查询可支付平台
    	List<PaymentMerchantDetail> merchantDtl = paymentMerchantService.queryPaymentMerchantDtl(merchantId);
    	
    	List<PaymentMerchantDetail> paymentList = new ArrayList<PaymentMerchantDetail>();
    	List<PaymentMerchantDetail> fcbList = new ArrayList<PaymentMerchantDetail>();
    	String bt = "";
		if(type.equalsIgnoreCase(PaymentMerchantDetail.TYPE_IOD))
			bt = PaymentMerchantDetail.TYPE_IOS_YUE;
		else if(type.equalsIgnoreCase(PaymentMerchantDetail.TYPE_ANDROID))
			bt = PaymentMerchantDetail.TYPE_AND_YUE;
		
    	for (PaymentMerchantDetail paymentMerchantDetail : merchantDtl) {
			if(type.equalsIgnoreCase(paymentMerchantDetail.getType())){
				paymentList.add(paymentMerchantDetail);
			}
			//余额
			if(bt.equalsIgnoreCase(paymentMerchantDetail.getType())){
				//如果含有余额支付，则要进行身份验证
				String currencyId = paymentMerchantDetail.getContent();
				GamePropsCurrency currency = fcbService.queryById(Long.valueOf(currencyId));
				//验证身份
				Map<String, Object> tokenMap = fcbService.validateToken(ext);
				if(tokenMap == null)
					continue;
				String tokenCode = ObjectUtils.toString(tokenMap.get("code"));
				if(!"0".equals(tokenCode)){
					//身份认证失败
					logger.info("翡翠币身份认证失败,token:" + ext);
					continue;
				}
				Map<String, Object> dataMap =  (Map<String, Object>)tokenMap.get("data");
				String fcbAccount = ObjectUtils.toString(dataMap.get("account"));
				String fcbPhone = ObjectUtils.toString(dataMap.get("phone"));
				//super.registFcbIdentity(request, fcbAccount, fcbPhone);
				fcbService.registFcbIdentity(fcbAccount, fcbPhone);
				
				//查询余额
				Map<String, Object> fcbMap = null;
				try {
					fcbMap = this.queryFcbAmount(fcbAccount, currency.getGameId(), IpUtils.getRemoteAddr(request));
				} catch (Exception e) {
					logger.error("查询翡翠币余额异常：" + e.getMessage());
					continue;
				}
				retMap.put("fcbInfo", fcbMap.get("fcbInfo"));
				retMap.put("fcbPhone", fcbPhone);
				retMap.put("fcbAccount", fcbAccount);
				fcbList.add(paymentMerchantDetail);
			}
		}

    	if(paymentList.isEmpty() && fcbList.isEmpty()){
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未获取到支付方式",null));
    		return;
    	}
    	    	
    	retMap.put("payments", paymentList);
    	retMap.put("fcbList",fcbList);
    	
    	PaymentOrder order = paymentOrderService.queryOrderByPartnerOrderNo(orderNo,merchantId);
    	retMap.put("order", order);
    	
    	writeJsonp(callback, response, new ResultResponse(ResultResponse.SUCCESS,"获取支付方式成功",retMap));
    }
    
    Map<String, Object> putToMap(HttpServletRequest request,
    		Enumeration<String> requestParams) {
		Map<String, Object> map = new HashMap<String, Object>();
		while(requestParams.hasMoreElements()){
			String key = requestParams.nextElement();
			String value = request.getParameter(key);
			map.put(key, value);
		}
//		for (Iterator<Entry<String, Object>> keyValuePairs = requestParams
//				.entrySet().iterator(); keyValuePairs.hasNext();) {
//			Map.Entry<String, Object> entry = keyValuePairs.next();
//			String key = entry.getKey();
//			String value = request.getParameter(key);
//			
//			map.put(key, value);
//		}
		return map;
	}
    
    @RequestMapping("/payment/stage/json")
    public @ResponseBody ResultResponse queryCreditStage(HttpServletRequest request){
    	String bankCode = request.getParameter("bankCode");
    	
    	Map<String,Object> retMap = new HashMap<String, Object>();
    	if(StringUtils.isBlank(bankCode)){
    		return new ResultResponse(ResultResponse.FAIL, "参数不能为空");
    	}
    	
    	List<CreditStage> list = paymentMerchantService.queryCreditStage(bankCode);
    	if(list ==null || list.isEmpty()){
    		return new ResultResponse(ResultResponse.FAIL,"查询结果为空");
    	}
    	
    	retMap.put("list", list);
    	return new ResultResponse(ResultResponse.SUCCESS, "查询分期计划成功", retMap);
    	
    }
    
    @RequestMapping("/payment/order/query")
    @ResponseBody
    public ResultResponse queryOrder(HttpServletRequest request){
    	String orderNo = request.getParameter("orderNo");
    	String merchantId = request.getParameter("merchantid");
    	if(StringUtils.isBlank(orderNo) || StringUtils.isBlank(merchantId) ){
    		return new ResultResponse(ResultResponse.FAIL, "参数不能为空");
    	}
    	PaymentOrder paymentOrder = paymentOrderService.queryOrderByPartnerOrderNo(orderNo,Long.parseLong(merchantId));
    	if(paymentOrder ==null){
    		return new ResultResponse(ResultResponse.FAIL, "订单查询为空");
    	}
    	Map<String,Object> retMap = new HashMap<String, Object>();
    	retMap.put("paymentOrder", paymentOrder);
    	return new ResultResponse(ResultResponse.SUCCESS, "订单查询成功", retMap);
    }
    
    private Map<String, Object> queryFcbAmount(String account,Long gameId,String clientIp) 
    		throws ValidationException{
    	if(StringUtils.isBlank(account)
    			|| StringUtils.isBlank(clientIp)){
    		logger.info("account:" + account + ",gameId：" + gameId + ",clientIp:" + clientIp);
    		throw new ValidationException("缺少参数！");
    	}
    	
		Passport passport = corePassportService.queryPassport(account);
		if(passport == null){
			logger.info("通行证不存在,account:" + account);
			throw new ValidationException("通行证不存在！");
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", passport.getId());
		params.put("spId", 7);
		params.put("appId", gameId);
		params.put("areaId", "-1");
		params.put("payTypeId", "-1");
		params.put("eventTimestamp", DateUtil.getCurDateTimeStr());
		params.put("clientIp", clientIp);
		Map<String, Object> fcbMap = fcbService.queryAmount(params);
		
		if(!"1".equals(ObjectUtil.toString(fcbMap.get("msgcode")))){
			logger.info("查询翡翠币余额异常，msgcode:" + fcbMap.get("msgcode") + ",message:" + fcbMap.get("message"));
			throw new ValidationException("查询翡翠币余额异常");
		}
		
		Map<String, Object> retMap = new HashMap<String, Object>();
		List<Map<String, Object>> data = (List<Map<String, Object>>)fcbMap.get("data");
		retMap.put("fcbInfo", data);
		return retMap;
    }
    
    /**
     * 负责将业务方的请求转为标准收银台接口的post请求
     * @param req
     * @return
     */
    @RequestMapping("/payment/trans")
    public String trans(HttpServletRequest req) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("orderNo", req.getAttribute("orderno"));
		retMap.put("money", req.getAttribute("money"));
		retMap.put("backendurl", req.getAttribute("backendurl"));
		retMap.put("merchantid", req.getAttribute("merchantid"));
		retMap.put("gameid", req.getAttribute("gameid"));
		retMap.put("account", req.getAttribute("account"));
		retMap.put("clientip", req.getAttribute("clientip"));
		retMap.put("imprestmode", req.getAttribute("imprestmode"));
		retMap.put("productname", req.getAttribute("productname"));
		retMap.put("fontendurl", req.getAttribute("fontendurl"));
		retMap.put("sign", req.getAttribute("security"));
		retMap.put("ext", ObjectUtils.toString(req.getAttribute("ext")).replaceAll("\"", "'"));
		retMap.put("body", req.getAttribute("body"));
		retMap.put("goodsDetail", ObjectUtils.toString(req.getAttribute("goodsDetail")).replaceAll("\"", "'"));
		retMap.put("terminalType", req.getAttribute("terminalType"));
		retMap.put("timeoutExpress", req.getAttribute("timeoutExpress"));
		req.setAttribute("ret", retMap);
		return "/payment/payment_trans";
    }
    
    /**
     * 收银台首页
     * 
     * @param paytype 支付方式:PC,WAP,ios,android
     * @param type APP类型 Android:A iOS:I
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
    @RequestMapping("/payments/{paytype}")
	public void payments(@PathVariable("paytype") String paytype,
			@RequestParam(value="orderno") String orderNo,
			@RequestParam(value="merchantid") Long merchantId,
			@RequestParam(value="gameid",required=false) Long gameId,
			@RequestParam(value="account",required=false) String account,
			@RequestParam(value="callback",required=false) String callback,
			@RequestParam(value="ext",required=false) String ext,
			@RequestParam(value="body",required=false) String body,
			@RequestParam(value="goodsDetail",required=false) String goodsDetail,
			@RequestParam(value="terminalType",required=false) String terminalType,
			@RequestParam(value="timeoutExpress",required=false) String timeoutExpress,
			HttpServletRequest request, HttpServletResponse response) {
    	
    	logRequestParams("(" + request.getMethod() + ")"
				+ request.getRequestURL().toString(), request);
    	
    	Map<String,Object> retMap = new HashMap<String,Object>();
    	if(StringUtils.isBlank(orderNo) || orderNo.length() >60){
    		logger.error("订单号为空或长度超出,account:"+account);
    		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"订单号不可以为空",null));
    		return;
    	}
    	
    	List<PaymentMerchantDetail> paymentList = new ArrayList<PaymentMerchantDetail>();
    	//查询可支付平台
    	List<PaymentMerchantDetail> merchantDtl = paymentMerchantService.queryPaymentMerchantDtl(merchantId);
    	List<PaymentMerchantDetail> fcbList = new ArrayList<PaymentMerchantDetail>();
    	
    	//PC
    	if(paytype.equalsIgnoreCase(PaymentConstant.PAYMENT_PC)){
        	List<PaymentMerchantDetail> paymentDebitList = new ArrayList<PaymentMerchantDetail>();
        	List<PaymentMerchantDetail> paymentCreditList = new ArrayList<PaymentMerchantDetail>();
        	List<PaymentMerchantDetail> paymentThirdList = new ArrayList<PaymentMerchantDetail>();
        	List<PaymentMerchantDetail> paymentTtbList = new ArrayList<PaymentMerchantDetail>();
        	List<PaymentMerchantDetail> paymentYxCardList = new ArrayList<PaymentMerchantDetail>();
        	List<PaymentMerchantDetail> paymentMobileCardList = new ArrayList<PaymentMerchantDetail>();
        	List<PaymentMerchantDetail> paymentWnMobileSpecCardList = new ArrayList<PaymentMerchantDetail>();//蜗牛移动全能充值卡
        	List<PaymentMerchantDetail> paymentFcbList = new ArrayList<PaymentMerchantDetail>();
        	for (PaymentMerchantDetail paymentMerchantDetail : merchantDtl) {
    			if(PaymentMerchantDetail.TYPE_BANK.equals(paymentMerchantDetail.getType())){
    				paymentDebitList.add(paymentMerchantDetail);
    			} else if (PaymentMerchantDetail.TYPE_CREDIT_STAGE.equals(paymentMerchantDetail.getType())){
    				paymentCreditList.add(paymentMerchantDetail);
    			} else if (PaymentMerchantDetail.TYPE_THIRD.equals(paymentMerchantDetail.getType())){
    				paymentThirdList.add(paymentMerchantDetail);
    			} else if( (PaymentMerchantDetail.TYPE_TTB.equals(paymentMerchantDetail.getType())) || (PaymentMerchantDetail.TYPE_TTB_PC.equals(paymentMerchantDetail.getType()))){
    				paymentTtbList.add(paymentMerchantDetail);
    			} else if((PaymentMerchantDetail.TYPE_YX_CARD.equals(paymentMerchantDetail.getType())) 
    					&& (!PaymentConstant.WN_GAME_CARD.equals(paymentMerchantDetail.getContent())) ){
    				paymentYxCardList.add(paymentMerchantDetail);
    			} else if(PaymentMerchantDetail.TYPE_YX_MOBILE.equals(paymentMerchantDetail.getType())){
    				paymentMobileCardList.add(paymentMerchantDetail);
    			} else if(PaymentMerchantDetail.TYPE_WEB_YUE.equals(paymentMerchantDetail.getType())){
    				//如果含有余额支付，则要进行身份验证
    				String currencyId = paymentMerchantDetail.getContent();
    				GamePropsCurrency currency = fcbService.queryById(Long.valueOf(currencyId));
    				//验证身份
    				Map<String, Object> tokenMap = fcbService.validateToken(ext);
    				if(tokenMap == null)
    					continue;
    				String tokenCode = ObjectUtils.toString(tokenMap.get("code"));
    				if(!"0".equals(tokenCode)){
    					//身份认证失败
    					logger.info("翡翠币身份认证失败,token:" + ext);
    					continue;
    				}
    				Map<String, Object> dataMap =  (Map<String, Object>)tokenMap.get("data");
    				String fcbAccount = ObjectUtils.toString(dataMap.get("account"));
    				String fcbPhone = ObjectUtils.toString(dataMap.get("phone"));
    				super.registFcbIdentity(request, fcbAccount, fcbPhone);
    				
    				//查询余额
    				Map<String, Object> fcbMap = null;
    				try {
    					fcbMap = this.queryFcbAmount(fcbAccount, currency.getGameId(), IpUtils.getRemoteAddr(request));
    				} catch (Exception e) {
    					logger.error("查询翡翠币余额异常：" + e.getMessage());
    					continue;
    				}
    				retMap.put("fcbInfo", fcbMap.get("fcbInfo"));
    				retMap.put("fcbPhone", fcbPhone);
    				retMap.put("fcbAccount", fcbAccount);
    				paymentFcbList.add(paymentMerchantDetail);
    			}
    			
    			if( (PaymentMerchantDetail.TYPE_WN_MOBILE_SPEC_CARD.equalsIgnoreCase(paymentMerchantDetail.getType())
    					&& PaymentConstant.WN_QMOBILE_CARD.equalsIgnoreCase(paymentMerchantDetail.getContent())) || 
    					(PaymentMerchantDetail.TYPE_WN_MOBILE_SPEC_CARD.equalsIgnoreCase(paymentMerchantDetail.getType())
    							&& PaymentConstant.WN_MOBILE_CARD.equalsIgnoreCase(paymentMerchantDetail.getContent())) || 
    					( PaymentMerchantDetail.TYPE_YX_CARD.equalsIgnoreCase(paymentMerchantDetail.getType()) && PaymentConstant.WN_GAME_CARD.equals(paymentMerchantDetail.getContent()))){
    				//蜗牛移动充值卡+游戏一卡通
    				paymentWnMobileSpecCardList.add(paymentMerchantDetail);
    			}
    		}

        	//组装平台列表
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	retMap.put("paydate", sdf.format(new Date()));
        	
        	retMap.put("debitlist", paymentDebitList);
        	retMap.put("creditlist", paymentCreditList);
        	retMap.put("thirdlist", paymentThirdList);
        	retMap.put("ttbList", paymentTtbList);
        	retMap.put("yxCardList", paymentYxCardList);
        	retMap.put("mobileCardList", paymentMobileCardList);
        	//蜗牛移动全能充值卡
        	retMap.put("wnMobileSpecCardList", paymentWnMobileSpecCardList);
        	//翡翠币
        	retMap.put("fcbList", paymentFcbList);
        	
        	request.setAttribute("ret", retMap);
    	}
    	
    	//WAP
    	if(paytype.equalsIgnoreCase(PaymentConstant.PAYMENT_WAP)){
        	List<PaymentMerchantDetail> bankList = new ArrayList<PaymentMerchantDetail>();
        	
        	for (PaymentMerchantDetail paymentMerchantDetail : merchantDtl) {
    			if(PaymentMerchantDetail.TYPE_WAP.equalsIgnoreCase(paymentMerchantDetail.getType())){
    				paymentList.add(paymentMerchantDetail);
    			} 
    			if((PaymentMerchantDetail.TYPE_TTB.equals(paymentMerchantDetail.getType())) || (PaymentMerchantDetail.TYPE_TTB_WAP.equals(paymentMerchantDetail.getType()))){
    				paymentList.add(paymentMerchantDetail);
    			}
    			//游戏充值卡wap
    			if(PaymentMerchantDetail.TYPE_WN_GAME_WAP_CARD.equals(paymentMerchantDetail.getType())){
    				paymentList.add(paymentMerchantDetail);
    			}
    			//手机充值卡wap
    			if(PaymentMerchantDetail.TYPE_WN_MOBILE_WAP_CARD.equals(paymentMerchantDetail.getType())){
    				paymentList.add(paymentMerchantDetail);
    			}
    			//蜗牛充值卡wap
    			if(PaymentMerchantDetail.TYPE_WN_QMOBILE_WAP_CARD.equals(paymentMerchantDetail.getType())){
    				paymentList.add(paymentMerchantDetail);
    			}
    			
    			//快钱快捷wap-借记卡
    			if(PaymentMerchantDetail.TYPE_QUICK_BANK_DEBIT.equals(paymentMerchantDetail.getType())){
    				bankList.add(paymentMerchantDetail);
    			}
    			//快钱快捷wap-信用卡
    			if(PaymentMerchantDetail.TYPE_QUICK_BANK_CREDIT.equals(paymentMerchantDetail.getType())){
    				bankList.add(paymentMerchantDetail);
    			}
    			if(PaymentMerchantDetail.TYPE_WAP_YUE.equals(paymentMerchantDetail.getType())){
    				//如果含有余额支付，则要进行身份验证
    				String currencyId = paymentMerchantDetail.getContent();
    				GamePropsCurrency currency = fcbService.queryById(Long.valueOf(currencyId));
    				//验证身份
    				Map<String, Object> tokenMap = fcbService.validateToken(ext);
    				if(tokenMap == null)
    					continue;
    				String tokenCode = ObjectUtils.toString(tokenMap.get("code"));
    				if(!"0".equals(tokenCode)){
    					//身份认证失败
    					logger.info("翡翠币身份认证失败,token:" + ext);
    					continue;
    				}
    				Map<String, Object> dataMap =  (Map<String, Object>)tokenMap.get("data");
    				String fcbAccount = ObjectUtils.toString(dataMap.get("account"));
    				String fcbPhone = ObjectUtils.toString(dataMap.get("phone"));
    				super.registFcbIdentity(request, fcbAccount, fcbPhone);
    				
    				//查询余额
    				Map<String, Object> fcbMap = null;
    				try {
    					fcbMap = this.queryFcbAmount(fcbAccount, currency.getGameId(), IpUtils.getRemoteAddr(request));
    				} catch (Exception e) {
    					logger.error("查询翡翠币余额异常：" + e.getMessage());
    					continue;
    				}
    				retMap.put("fcbInfo", fcbMap.get("fcbInfo"));
    				retMap.put("fcbPhone", fcbPhone);
    				retMap.put("fcbAccount", fcbAccount);
    				fcbList.add(paymentMerchantDetail);
    			}
    		}

        	if(paymentList == null || paymentList.isEmpty()){
        		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未获取到支付方式",null));
        		return;
        	}
        	    	
        	retMap.put("payments", paymentList);
        	retMap.put("bankList",bankList);
        	retMap.put("fcbList",fcbList);
        	
    	}
    	
    	
    	//app的ios和android类型
    	if(paytype.equalsIgnoreCase(PaymentConstant.PAYMENT_IOS) || paytype.equalsIgnoreCase(PaymentConstant.PAYMENT_ANDROID)){
        	String bt = "";
    		if(paytype.equalsIgnoreCase(PaymentConstant.PAYMENT_IOS)){
    			bt = PaymentMerchantDetail.TYPE_IOS_YUE;
    		}else if(paytype.equalsIgnoreCase(PaymentConstant.PAYMENT_ANDROID)){
    			bt = PaymentMerchantDetail.TYPE_AND_YUE;
    		}
    		
        	for (PaymentMerchantDetail paymentMerchantDetail : merchantDtl) {
    			if((paytype.equalsIgnoreCase(PaymentConstant.PAYMENT_IOS) && (PaymentMerchantDetail.TYPE_TTB_IOS.equals(paymentMerchantDetail.getType()) || PaymentMerchantDetail.TYPE_TTB.equals(paymentMerchantDetail.getType()))) ){
    				paymentList.add(paymentMerchantDetail);
    			}else if((paytype.equalsIgnoreCase(PaymentConstant.PAYMENT_ANDROID) && (PaymentMerchantDetail.TYPE_TTB_ANDROID.equals(paymentMerchantDetail.getType()) || PaymentMerchantDetail.TYPE_TTB.equals(paymentMerchantDetail.getType())) )){
    				paymentList.add(paymentMerchantDetail);
    			} else if( (paytype.equalsIgnoreCase(PaymentConstant.PAYMENT_IOS))
    					&& (PaymentConstant.PAYMENT_TYPE_IOS.equals(paymentMerchantDetail.getType()) 
    							|| PaymentConstant.I_MOBILE_CARD.equals(paymentMerchantDetail.getType())
    							|| PaymentConstant.I_WNMOBILE_CARD.equals(paymentMerchantDetail.getType()))){
    				//app  ios的支付方式
    				//蜗牛移动充值卡
    				if(PaymentConstant.I_WNMOBILE_CARD.equals(paymentMerchantDetail.getType()) && StringUtils.isNotBlank(paymentMerchantDetail.getContent()) && paymentMerchantDetail.getContent().equals("WM")){
    					paymentMerchantDetail.setName("蜗牛移动充值卡");
    				}
    				//蜗牛全能充值卡
    				if(PaymentConstant.I_WNMOBILE_CARD.equals(paymentMerchantDetail.getType()) && StringUtils.isNotBlank(paymentMerchantDetail.getContent()) && paymentMerchantDetail.getContent().equals("SM")){
    					paymentMerchantDetail.setName("蜗牛全能充值卡");
    				}
    				
    				paymentList.add(paymentMerchantDetail);
    			} else if( (paytype.equalsIgnoreCase(PaymentConstant.PAYMENT_ANDROID)) 
    					&& (PaymentConstant.PAYMENT_TYPE_ANDROID.equalsIgnoreCase(paymentMerchantDetail.getType())
    							|| PaymentConstant.A_MOBILE_CARD.equalsIgnoreCase(paymentMerchantDetail.getType())
    							|| PaymentConstant.A_WNMOBILE_CARD.equalsIgnoreCase(paymentMerchantDetail.getType()))){
    				//app  android的支付方式
    				//蜗牛移动充值卡
    				if(PaymentConstant.A_WNMOBILE_CARD.equals(paymentMerchantDetail.getType()) && StringUtils.isNotBlank(paymentMerchantDetail.getContent()) && paymentMerchantDetail.getContent().equals("WM")){
    					paymentMerchantDetail.setName("蜗牛移动充值卡");
    				}
    				//蜗牛全能充值卡
    				if(PaymentConstant.A_WNMOBILE_CARD.equals(paymentMerchantDetail.getType()) && StringUtils.isNotBlank(paymentMerchantDetail.getContent()) && paymentMerchantDetail.getContent().equals("SM")){
    					paymentMerchantDetail.setName("蜗牛全能充值卡");
    				}
    				paymentList.add(paymentMerchantDetail);
    			}
    			
    			//余额
    			if(bt.equalsIgnoreCase(paymentMerchantDetail.getType())){
    				//如果含有余额支付，则要进行身份验证
    				String currencyId = paymentMerchantDetail.getContent();
    				GamePropsCurrency currency = fcbService.queryById(Long.valueOf(currencyId));
    				//验证身份
    				Map<String, Object> tokenMap = fcbService.validateToken(ext);
    				if(tokenMap == null)
    					continue;
    				String tokenCode = ObjectUtils.toString(tokenMap.get("code"));
    				if(!"0".equals(tokenCode)){
    					//身份认证失败
    					logger.info("翡翠币身份认证失败,token:" + ext);
    					continue;
    				}
    				Map<String, Object> dataMap =  (Map<String, Object>)tokenMap.get("data");
    				String fcbAccount = ObjectUtils.toString(dataMap.get("account"));
    				String fcbPhone = ObjectUtils.toString(dataMap.get("phone"));
    				//super.registFcbIdentity(request, fcbAccount, fcbPhone);
    				fcbService.registFcbIdentity(fcbAccount, fcbPhone);
    				
    				//查询余额
    				Map<String, Object> fcbMap = null;
    				try {
    					fcbMap = this.queryFcbAmount(fcbAccount, currency.getGameId(), IpUtils.getRemoteAddr(request));
    				} catch (Exception e) {
    					logger.error("查询翡翠币余额异常：" + e.getMessage());
    					continue;
    				}
    				retMap.put("fcbInfo", fcbMap.get("fcbInfo"));
    				retMap.put("fcbPhone", fcbPhone);
    				retMap.put("fcbAccount", fcbAccount);
    				fcbList.add(paymentMerchantDetail);
    			}
    		}

        	if(paymentList.isEmpty() && fcbList.isEmpty()){
        		writeJsonp(callback, response, new ResultResponse(ResultResponse.FAIL,"未获取到支付方式",null));
        		return;
        	}
        	    	
        	retMap.put("payments", paymentList);
        	retMap.put("fcbList",fcbList);
    	}
    	PaymentOrder order = paymentOrderService.queryOrderByPartnerOrderNo(orderNo,merchantId);
    	retMap.put("order", order);
    	
    	writeJsonp(callback, response, new ResultResponse(ResultResponse.SUCCESS,"获取支付方式成功",retMap));
    }
    
    //测试方法
    @SuppressWarnings("unchecked")
	@RequestMapping(value="/callback/create")
	public void callbackCreate(HttpServletRequest request, HttpServletResponse response) {
    	PrintWriter writer = null;
    	try {
    		String orderNo = request.getParameter("orderNo");
    		String merchantId = request.getParameter("merchantId");
    		String sign = request.getParameter("sign");
    		
    		Map<String, String> treeMap = new TreeMap<String, String>();
    		StringBuffer logStr = new StringBuffer();
    		logStr.append("\n++++++参数 开始++++++\n");
    		logStr.append("requestIp=" + IpUtils.getRemoteAddr(request));
    		logStr.append("\n");
    		
//    		Map<String, Object> requestParams = request.getParameterMap();
    		Enumeration<String> requestParams = request.getParameterNames();
    		while(requestParams.hasMoreElements()){
    			String key = requestParams.nextElement();
    			String value = request.getParameter(key);
    			treeMap.put(key, value);
    			logStr.append(key + "=" + value);
    			logStr.append("\n");
    		}
//    		for (Iterator<Entry<String, Object>> keyValuePairs = requestParams
//    				.entrySet().iterator(); keyValuePairs.hasNext();) {
//    			Map.Entry<String, Object> entry = keyValuePairs.next();
//    			String key = entry.getKey();
//    			String value = request.getParameter(key);
//    			
//    			treeMap.put(key, value);
//    			logStr.append(key + "=" + value);
//    			logStr.append("\n");
//    		}
    		
    		logStr.append("++++++参数 结束++++++");
    		logger.info(logStr.toString());
    		
    		
    		//获取订单信息
    		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
    		//获取商户信息
    		PaymentMerchant payemntMerchnt = paymentMerchantService.queryPayemntMerchnt(Long.valueOf(merchantId));

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
    			request.setAttribute("retCode", "1");
    			request.setAttribute("retMsg", "操作成功");
    		} else {
    			logger.info("签名验证失败");
    			logger.error("原串:"+sb.toString()+",local sign:"+localSign + ",sign:"+sign);
    			request.setAttribute("retCode", "0");
    			request.setAttribute("retMsg", "操作失败");
    		}
    		
    		if(PaymentConstant.PAYMENT_STATE_PAYED.equals(paymentOrder.getPayState())){
    			String result = paymentOrderService.createCallbackSyncTask(paymentOrder, payemntMerchnt, paymentOrder.getOtherOrderNo(), "174");
    			message = result;
    			if (logger.isInfoEnabled())
    			logger.info("返回给支付平台的信息：" + result);
    			logger.info("队列创建结果：" + result);
    		}
    		logger.info("订单未支付");
			writer = response.getWriter();
			writer.print(message);
			writer.flush();
		} catch (Exception ex) {
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
}
