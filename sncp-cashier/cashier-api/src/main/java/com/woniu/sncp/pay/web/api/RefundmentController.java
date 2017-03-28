package com.woniu.sncp.pay.web.api;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.core.service.PaymentOrderService;
import com.woniu.sncp.pay.core.service.RefundmentOrderService;
import com.woniu.sncp.pay.core.service.refundment.RefundmentFacade;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.web.response.ResultResponse;

/**
 * 退款中心对外退款接口
 *  
 *  
 *  /api/refundment/order/dp
 * 
 *  /api/refundment/order/verify 	退款订单验证接口，用来处理掉单 
 *  /api/refundment/orderno/refund 	第三方订单退款
 * @author fuzl
 * 
 */
@Controller
@RequestMapping("/api/refundment")
public class RefundmentController extends ApiBaseController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private RefundmentFacade refundmentFacade;
	@Resource
	private RefundmentOrderService refundmentOrderService;
	@Resource
	private PaymentOrderService paymentOrderService;
	/**
	 * 1.构造退款请求
	 */
	@RequestMapping("/order/alipayrefund")
	public @ResponseBody ResultResponse refundOrder(
			@RequestParam(value = "isbatch") String isbatch,
			@RequestParam(value = "partnerbatchno") String batchno,
			@RequestParam(value="batchnum") String batchnum,
			@RequestParam(value="datadetails") String datadetails,
			@RequestParam(value="merchantid") String partnerid,
			@RequestParam(value="platformid") String platformId,
			@RequestParam(value="refunddate") String refunddate,
			@RequestParam(value="clientip") String clientIp,
			@RequestParam(value="backendurl") String backendurl,
			@RequestParam(value="sign") String sign,
			@RequestParam(value="verifyurl") String verifyurl,
			HttpServletRequest request) {
		Map<String,Object> retMap = new HashMap<String, Object>();
		//1. 商户权限分配控制逻辑代码
		String account = request.getParameter("account");
		String gameId = request.getParameter("gameId");
		String refundmode = request.getParameter("refundmode");
		String ext = request.getParameter("ext");
		try {
			refundmentFacade.validateRefundmentPlatform(partnerid,platformId);
        } catch (IllegalArgumentException e) {
			logger.error("validation异常", e.getMessage());
			retMap = ErrorCode.getErrorCode(53201);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			return new ResultResponse(ResultResponse.FAIL,"订单退款失败",retMap);
		} catch (DataAccessException e) {
        	logger.error("商户权限没有配置,account:"+account);
        	retMap = ErrorCode.getErrorCode(53207);
        	request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return new ResultResponse(ResultResponse.FAIL,"订单退款失败",retMap);
        }
		
		//2.参数校验
		if(StringUtils.isBlank(batchno) || batchno.length() >60){
    		logger.error("批次号为空或长度超出,account:"+account);
    		retMap.put("message", "批次号为空或长度超出");
    		retMap = ErrorCode.getErrorCode(53209);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return new ResultResponse(ResultResponse.FAIL,"订单退款失败",retMap);
    	}
		if(StringUtils.isBlank(batchnum) || "0".equals(StringUtils.trim(batchnum))){
    		logger.error("订单数量不可以为空或零,account:"+account);
    		retMap.put("message", "batchnum为空或等于零");
    		retMap = ErrorCode.getErrorCode(53209);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return new ResultResponse(ResultResponse.FAIL,"订单退款失败",retMap);
    	}
    	if(StringUtils.isBlank(backendurl) || backendurl.indexOf("http") != 0){
    		logger.error("回调地址不可以为空,account:"+account);
    		retMap = ErrorCode.getErrorCode(53209);
    		retMap.put("message", "backendurl为空或格式错误");
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return new ResultResponse(ResultResponse.FAIL,"订单退款失败",retMap);
    	}
    	
    	try {
    		if(!StringUtils.isBlank(datadetails)){
    			//支付宝退款交易结果集
    			//原付款支付宝交易号^退款总金额^退款理由
        		JSONArray details = JSONArray.parseArray(datadetails);
        		for(Object detail :details){
        			
        			JSONObject jsonDetail = JSONObject.parseObject(detail.toString());
        			if(StringUtils.isBlank(jsonDetail.getString("money")) || "0".equals(StringUtils.trim(jsonDetail.getString("money")))){
        	    		logger.error("退款订单金额不可以为空或零,account:"+account);
        	    		retMap.put("message", "money为空或等于零");
        	    		retMap = ErrorCode.getErrorCode(53209);
        	    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
        	    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
        	    		return new ResultResponse(ResultResponse.FAIL,"订单退款失败",retMap);
        	    	}
        			
        			/*判断订单以及金额*/
        			PaymentOrder paymentOrder = paymentOrderService.queryOrderByPartnerOrderNo(ObjectUtils.toString(jsonDetail.get("orderno")));
        			if(null!=paymentOrder){
//        				jsonDetail.remove("orderno");
//        				jsonDetail.put("orderno", paymentOrder.getOrderNo());//业务方订单号转为计费侧订单号
        				if(Float.parseFloat(StringUtils.trim(jsonDetail.getString("money"))) > paymentOrder.getMoney()){
            	    		logger.error("退款订单金额大于原订单金额,account:"+account+",porderno:"+paymentOrder.getPartnerOrderNo());
            	    		retMap.put("message", "退款订单金额大于原订单金额,porderno:"+paymentOrder.getPartnerOrderNo());
            	    		retMap = ErrorCode.getErrorCode(53209);
            	    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
            	    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
            	    		return new ResultResponse(ResultResponse.FAIL,"订单退款失败",retMap);
            	    	}
        				if(!paymentOrder.getPaymentState().equals(PaymentConstant.PAYMENT_STATE_PAYED)){
        					retMap = ErrorCode.getErrorCode(53202);
            	    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
            	    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
        					return new ResultResponse(ResultResponse.FAIL,"订单退款失败",retMap);
        				}
        			}else{
        				logger.error("退款交易号找不到订单,porderno:"+ObjectUtils.toString(jsonDetail.get("orderno")));
                		retMap.put("message", "退款交易号找不到订单,porderno:"+ObjectUtils.toString(jsonDetail.get("orderno")));
                		retMap = ErrorCode.getErrorCode(53203);
                		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
        	    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
                		return new ResultResponse(ResultResponse.FAIL,"订单退款失败",retMap);
        			}
        		}
        		
        	}else{
        		logger.error("退款交易信息不可以为空,account:"+account);
        		retMap.put("message", "datadetails为空或格式错误");
        		retMap = ErrorCode.getErrorCode(53209);
        		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
	    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
	    		return new ResultResponse(ResultResponse.FAIL,"订单退款失败",retMap);
        	}
        	
		} catch (Exception e) {
			retMap = ErrorCode.getErrorCode(53210);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return new ResultResponse(ResultResponse.FAIL,"订单退款失败",retMap);
		}
		
    	logger.info("请求订单退款信息,"+retMap);
    	//3. 实际业务处理
    	try{
    		/*退款请求*/
    		retMap = refundmentFacade.refundOrder(isbatch,batchno,batchnum,datadetails.toString(),NumberUtils.toLong(partnerid),NumberUtils.toLong(platformId),refunddate
    				,clientIp,backendurl,account,sign,verifyurl,ext,request);
		} catch (Exception e){
			retMap = ErrorCode.getErrorCode(53210);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			return new ResultResponse(ResultResponse.FAIL,"创建退款申请单失败",retMap);
		}
    	
    	if (retMap != null && StringUtils.equalsIgnoreCase(ObjectUtils.toString(retMap.get(ErrorCode.TIP_CODE)), "1")) {
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return new ResultResponse(ResultResponse.SUCCESS,"创建退款申请单成功",retMap);
		}
    	request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    	return new ResultResponse(ResultResponse.FAIL,"创建退款申请单失败",retMap);
	}
	

	
	/**
	 * 
	 * 1.构造退款请求
	 * @param isbatch
	 * @param batchno
	 * @param batchnum
	 * @param datadetails
	 * @param partnerid
	 * @param platformId
	 * @param refunddate
	 * @param clientIp
	 * @param backendurl
	 * @param sign
	 * @param verifyurl
	 * @param request
	 * @return
	 */
	@RequestMapping("/refund/order/dp")
	public @ResponseBody ResultResponse refundOrderDp(
			@RequestParam(value = "isbatch") String isbatch,
			@RequestParam(value = "partnerbatchno") String batchno,
			@RequestParam(value="batchnum") String batchnum,
			@RequestParam(value="datadetails") String datadetails,
			@RequestParam(value="merchantid") String merchantId,
			@RequestParam(value="platformid",required=false) String platformId,
			@RequestParam(value="refunddate") String refunddate,
			@RequestParam(value="clientip") String clientIp,
			@RequestParam(value="backendurl") String backendurl,
			@RequestParam(value="sign") String sign,
			@RequestParam(value="verifyurl") String verifyurl,
			HttpServletRequest request) {
		Map<String,Object> retMap = new HashMap<String, Object>();
		//1. 商户权限分配控制逻辑代码
		String account = request.getParameter("account");
		String gameId = request.getParameter("gameId");
		String refundmode = request.getParameter("refundmode");
		String ext = request.getParameter("ext");
//		try {
//			refundmentFacade.validateRefundmentPlatform(merchantId,platformId);
//        } catch (IllegalArgumentException e) {
//			logger.error("validation异常", e.getMessage());
//			retMap = ErrorCode.getErrorCode(53201);
//			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
//    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
//			return new ResultResponse(ResultResponse.FAIL,"订单退款失败",retMap);
//		} catch (DataAccessException e) {
//        	logger.error("商户权限没有配置,account:"+account);
//        	retMap = ErrorCode.getErrorCode(53207);
//        	request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
//    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
//    		return new ResultResponse(ResultResponse.FAIL,"订单退款失败",retMap);
//        }
		
		//2.参数校验
		if(StringUtils.isBlank(batchno) || batchno.length() >60){
    		logger.error("批次号为空或长度超出,account:"+account);
    		retMap.put("message", "批次号为空或长度超出");
    		retMap = ErrorCode.getErrorCode(53209);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return new ResultResponse(ResultResponse.FAIL,"订单退款失败",retMap);
    	}
		if(StringUtils.isBlank(batchnum) || "0".equals(StringUtils.trim(batchnum))){
    		logger.error("订单数量不可以为空或零,account:"+account);
    		retMap.put("message", "batchnum为空或等于零");
    		retMap = ErrorCode.getErrorCode(53209);
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return new ResultResponse(ResultResponse.FAIL,"订单退款失败",retMap);
    	}
    	if(StringUtils.isBlank(backendurl) || backendurl.indexOf("http") != 0){
    		logger.error("回调地址不可以为空,account:"+account);
    		retMap = ErrorCode.getErrorCode(53209);
    		retMap.put("message", "backendurl为空或格式错误");
    		request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    		return new ResultResponse(ResultResponse.FAIL,"订单退款失败",retMap);
    	}
    	
    	//3. 实际业务处理
    	try{
    		/*退款请求*/
    		retMap = refundmentFacade.refundOrder(isbatch,batchno,batchnum,datadetails.toString(),NumberUtils.toLong(merchantId),NumberUtils.toLong(platformId),refunddate
    				,clientIp,backendurl,account,sign,verifyurl,ext,request);
    		logger.info("请求订单退款信息,"+retMap);
		} catch (Exception e){
			retMap = ErrorCode.getErrorCode(53210);
			request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
    		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
			return new ResultResponse(ResultResponse.FAIL,"创建退款申请单失败",retMap);
		}
    	
    	request.setAttribute("retCode", retMap.get(ErrorCode.TIP_CODE));
		request.setAttribute("retMsg", retMap.get(ErrorCode.TIP_INFO));
    	if (retMap != null && StringUtils.equalsIgnoreCase(ObjectUtils.toString(retMap.get(ErrorCode.TIP_CODE)), "1")) {
			return new ResultResponse(ResultResponse.SUCCESS,"创建退款申请单成功",retMap);
		}
    	return new ResultResponse(ResultResponse.FAIL,"创建退款申请单失败",retMap);
	}
	
	
	
	
	
	
	@RequestMapping(value="/test",method = RequestMethod.POST)
	public @ResponseBody ResultResponse refundmentTest(
			@RequestParam(value = "partnerid") String partnerid,
			@RequestParam(value = "orderno") String orderno,
			@RequestParam(value = "account") String account,
			@RequestParam(value = "productid") String productid,
			@RequestParam(value = "amount") String amount,
			@RequestParam(value = "action") String action,
			@RequestParam(value = "sign") String sign,
			HttpServletRequest request) {
		
		Map<String,Object> retMap = new HashMap<String, Object>();
		String key = "123456";
		String signStr =  partnerid + orderno + productid + amount + action + key;
		logger.info("signStr:"+signStr);
		
		retMap.put("status", 200);
		retMap.put("orderno", orderno);
		return new ResultResponse("200","退款请求",retMap);
	}
	
	/**
	 * 运维监控这个地址，勿删
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/test2")
	public @ResponseBody ResultResponse testFront(
			@RequestParam(value = "orderno") String orderno,
			HttpServletRequest request) {
		Map<String,Object> retMap = new HashMap<String, Object>();
		String orderNo = request.getParameter("orderNo");
		request.setAttribute("orderNo", orderNo);
		logger.info("orderNo:"+orderNo);
		logger.info("orderno:"+orderno);
//		return "/payment/test/front";
		retMap.put("orderNo", orderNo);
		return new ResultResponse(ResultResponse.FAIL,"退款请求",retMap);
	}
}
