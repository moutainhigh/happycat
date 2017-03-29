package com.woniu.sncp.pay.core.service.payment.platform.moneybookers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.lang.StringUtil;
import com.woniu.sncp.net.HttpclientUtil;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.security.codec.CodecUtil;

@Component("moneyBookersPayment")
public class MoneyBookersPayment extends AbstractPayment {

	
	/** 
	 * 支付订单状态 成功 
	 */
	private final static String TRADE_FLAG_SUCCESS = "2";

	/** 
	 * 支付订单状态 返回失败 
	 */
	private final static  String TRADE_FLAG_FAIL = "-2";
	
	private final static String ACTION_STATUS_TRN = "status_trn";
	
	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		return "";
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		
        Map<String, Object> params = new HashMap<String, Object>();
        //支付地址
        params.put("payUrl", platform.getPayUrl());
        params.put("method", "POST");
        
        params.put("pay_to_email", platform.getManageUser());
        params.put("transaction_id", paymentOrder.getOrderNo());
        params.put("detail1_description", paymentOrder.getImprestCardName());
        params.put("detail1_text", paymentOrder.getImprestCardName());
        params.put("currency", paymentOrder.getMoneyCurrency());
        params.put("amount", paymentOrder.getMoney());
        
        //在moneybookers上自定义的校验参数名
		if (StringUtils.isNotEmpty(platform.getOperatorType())) {
			params.put(platform.getOperatorType(), 
					getValiateShareValue(platform.getMerchantNo(), platform.getPayKey(),
							paymentOrder.getOrderNo(), paymentOrder.getMoneyCurrency()));
		}

		params.put("recipient_description", "Snail Games");

		params.put("status_url", platform.getBehindUrl(paymentOrder.getMerchantId()));
		
		String paymentMethod = (String)inParams.get("defaultbank");
		if (StringUtils.isNotEmpty(paymentMethod)) {
			params.put("payment_methods", paymentMethod);
		} 
		
		params.put("return_url", platform.getFrontUrl(paymentOrder.getMerchantId()));
		
		
		params.put("language", "en");
		//params.put("logo_url", "");

		params.put("cancel_url", paymentOrder.getPartnerFrontUrl());
		return params;
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException, DataAccessException,
			PaymentRedirectException {
		
		String payToEmail = request.getParameter("pay_to_email");
		String payFromEmail = request.getParameter("pay_from_email");
	    String merchantId = request.getParameter("merchant_id");
	    String mbTransactionId = request.getParameter("mb_transaction_id");
	    String transactionId = request.getParameter("transaction_id");
	    String mbAmount = request.getParameter("mb_amount");
	    String mbCurrency = request.getParameter("mb_currency");
	    String status = request.getParameter("status");
	    String amount = request.getParameter("amount");
	    String currency = request.getParameter("currency");
	    String md5sig = request.getParameter("md5sig");
	    
	    //valid md5 encode
  		String encodeData = merchantId + transactionId + platform.getBackendKey() + mbAmount + mbCurrency + status;
  		String localSign = MD5Encrypt.encrypt(encodeData);
  		if( !StringUtil.equalsIgnoreCase(localSign, md5sig) ){
  			if (logger.isInfoEnabled()) {
  				logger.info("==============Moneybookers后台回调加密处理失败=================");
  				logger.info("我方加密串：" + localSign + ",对方加密串：" + md5sig);
  				logger.info("==============Moneybookers后台回调加密处理结束=================\n");
  			}
  			throw new ValidationException("Moneybookers支付平台加密校验失败");
  		}
  		
  		if (logger.isInfoEnabled()) {
  			logger.info("amount:{},currency:{},mb_amount:{},mb_currency:{}",
  					new Object[]{amount,currency,mbAmount, mbCurrency});
  		}
	    
  		
  		/* 蜗牛自定义参数 */
		/*String validateValue = request.getParameter(platform.getOperatorType());
		String vShareValue = getValiateShareValue(platform.getMerchantNo(), platform.getPayKey(), transactionId, currency);
		if (StringUtils.isEmpty(validateValue)
				|| !validateValue.equals(vShareValue)) {
			logger.info("Moneybookers支付平台我方自定义字段验证失败校验失败");
			throw new ValidationException("Moneybookers支付平台我方自定义字段验证失败校验失败");
		}*/
	    
		Map<String, Object> returneMap = new HashMap<String, Object>();
		
		if (StringUtils.isEmpty(payToEmail) || !StringUtils.equalsIgnoreCase(payToEmail, platform.getManageUser())) {
			logger.info("Parameter pay_to_email error, value:" + payToEmail);
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return returneMap;
		}
		
		if (StringUtils.isEmpty(merchantId) || !StringUtils.equalsIgnoreCase(merchantId, platform.getMerchantNo())) {
			logger.info("Parameter merchant_id error, value:" + merchantId);
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return returneMap;
		}
		
		if (StringUtils.isEmpty(merchantId) || !StringUtils.equalsIgnoreCase(merchantId, platform.getMerchantNo())) {
			logger.info("Parameter merchant_id error, value:" + merchantId);
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return returneMap;
		}
		
		if (TRADE_FLAG_FAIL.equals(status)) {
			logger.info("Parameter status error, value:" + status);
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_FAILED);
			return returneMap;
		}

		if (!TRADE_FLAG_SUCCESS.equals(status)) {
			logger.info("Parameter status error, value:" + status);
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_FAILED);
			return returneMap;
		}
		
		returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(transactionId);
		Assert.notNull(paymentOrder, "Moneybookers支付平台支付订单查询为空,orderNo:" + transactionId);
		paymentOrder.setUserName(payFromEmail);
		
		returneMap.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returneMap.put(PaymentConstant.OPPOSITE_ORDERNO, mbTransactionId);
		returneMap.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(mbAmount)).multiply(new BigDecimal(100)).intValue()));
		
		//不验证imprestMode,直接取订单中imprestMode
		returneMap.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		return returneMap;
	}


	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		String orderNo = paymentOrder.getOrderNo();							//订单号
		String merchantId = platform.getMerchantNo();				        //商户号
		String requestUrl = platform.getPayCheckUrl();						//接收返回的url地址
		String backendUrl = platform.getBehindUrl(paymentOrder.getMerchantId());
		
		Map<String, String> responseResult = validateQueryToPlatform(requestUrl, ACTION_STATUS_TRN, platform.getManageUser(),
				platform.getQueryKey(), orderNo, merchantId, backendUrl);
		
		String payAmount = responseResult.get("mb_amount");
		logger.info("Moneybookers验证订单接口数据返回:{}", responseResult.toString());
		
		Map<String, Object> outParams = new HashMap<String, Object>();
		String paymentStatus = responseResult.get("status");
		if (TRADE_FLAG_SUCCESS.equals(paymentStatus)) {
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED); // 支付状态
		} else {
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED); // 支付状态
		}
		
		outParams.put(PaymentConstant.OPPOSITE_ORDERNO, responseResult.get("mb_transaction_id")); // 对方订单号
		outParams.put(PaymentConstant.ORDER_NO, responseResult.get("transaction_id")); // 蜗牛订单号
		outParams.put(PaymentConstant.OPPOSITE_MONEY, 
				String.valueOf((new BigDecimal(payAmount)).multiply(new BigDecimal(100)).intValue()));
		return outParams;
	}
	
	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess){
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//http code 非200 继续回调
		}
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return StringUtils.trim(request.getParameter("transaction_id"));
	}
	
	
	/**
	 * @description 建立一个与moneybookers的后台连接，把返回的参数封装成一个map 这里读取的是最后一行
	 * @param mb_transaction_id
	 *            moneybookers平台的交易号
	 * @param transaction_id
	 *            蜗牛平台的交易号
	 * @param actionName
	 *            判断是何请求，处理repost 和 status_trn
	 */
	private Map<String, String> validateQueryToPlatform(String queryUrl, String actionName,
			String payToEmail, String password, String orderNo, String paymentOrderNo, String status_url) {
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("action=").append(actionName).append("&email=").append(payToEmail);
		queryStr.append("&password=").append(MD5Encrypt.encrypt(password)).append("&trn_id=").append(orderNo);
		if (!StringUtils.isNotEmpty(paymentOrderNo)) {
			queryStr.append("&mb_trn_id=").append(paymentOrderNo);
		}
		
		if (StringUtils.isNotEmpty(status_url)) {
			queryStr.append("&status_url=").append(status_url);
		}
		
		
		String response = HttpclientUtil.get(queryUrl + "?" + queryStr.toString());
		
		if (StringUtils.isEmpty(response)) {
			return null;
		}
		
		try {
			response = new String(CodecUtil.decodeUrl(response.getBytes()));
		} catch (DecoderException e) {
			throw unchecked(e);
		}
		Map<String, String> returnMap = new HashMap<String, String>();
		//convertStringToMap
		String[] params = StringUtils.split(response, '&');

		for(String param : params) {
			String[] pair = StringUtils.split(param, '=');
			if (pair != null) {
				if (pair.length != 2) {
					throw new IllegalArgumentException("parse queryString error");
				}
				returnMap.put(pair[0], pair[1]);
			}
		}
		return returnMap;

	}
	
	@Override
	public String getMoneyCurrency() {
		return "USD";
	}
	
	/**
	 * 生成校验参数的值
	 * @param snailOrderNo
	 * @param currency
	 * @return
	 */
	private String getValiateShareValue(String merchantId, String validateShareKey, String orderNo, String currency) {
		StringBuffer encryptSB = new StringBuffer();
		encryptSB.append(merchantId).append(orderNo).append(validateShareKey).append(currency);
		return MD5Encrypt.encrypt(encryptSB.toString());
	}
	
	protected static RuntimeException unchecked(Throwable ex) {
		if (ex instanceof RuntimeException) {
			return (RuntimeException) ex;
		} else {
			return new RuntimeException(ex);
		}
	}

}
