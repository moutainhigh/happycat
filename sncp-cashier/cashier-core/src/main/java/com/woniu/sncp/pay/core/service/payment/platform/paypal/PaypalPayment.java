package com.woniu.sncp.pay.core.service.payment.platform.paypal;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.paypal.ipn.IPNMessage;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;

@Component("paypalPayment")
public class PaypalPayment extends AbstractPayment {

	/** paypal通知订单状态消息 撤销支付 */
	private static final String TRADE_FLAG_REVERSED = "Reversed";

	/** paypal通知订单状态消息 支付完成 */
	private static final String TRADE_FLAG_COMPLETED = "Completed";
	
	/*@Autowired
	@Qualifier("paypalExecutor")*/
	@Resource(name="paypalExecutor")
	private ThreadPoolTaskExecutor poolExecutor;
	
//	private ExpressCheckoutService ecService;
	
	@Resource(name="paypalConfigurationMap")
	private Map<String, String> configurationMap;
	
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
		
		Validate.notNull(paymentOrder, "order is null");
		Validate.notEmpty(paymentOrder.getCurrency(), "order.currency is required");
		Validate.notEmpty(paymentOrder.getOrderNo(), "order.orderNo is required");
		
		Validate.isTrue(paymentOrder.getAmount() > 0, "order.amount is required or incorrect.");
		
		String productName = MapUtils.getString(inParams, "productName", "");
		paymentOrder.setImprestCardName(productName);
		
		/*if(order.getExtraMap() == null || order.getExtraMap().get(RAAS_PARAM) == null){
			throwException("parameter raas is required.");
		}*/
		ExpressCheckoutService ecService = ConfigAndServiceCacheManager.getECService(platform.getMerchantNo());
		if (ecService == null) {
			ConfigAndServiceCacheManager.putECService(platform.getMerchantNo(), configurationMap);
			ecService = ConfigAndServiceCacheManager.getECService(platform.getMerchantNo());
		}
		//SetExpressCheckout
		Map<String, String> result = ecService.setExpressCheckout(paymentOrder, platform);
		if (result != null && result.containsKey(ExpressCheckoutService.PAY_URL)) {
			// 发起STC请求，抛送用户数据 可改为异步
			//STC请求失败，业务上为不影响交易流程。所以在此捕获不抛出
			final ExpressCheckoutService tcService = ecService;
			poolExecutor.execute(new Runnable() {
				@Override
				public void run() {
					JSONObject extend = JSONObject.parseObject(paymentOrder.getInfo());
					String raasParam = extend.getString("raas_param");
					if (StringUtils.isNotEmpty(raasParam)) {
						String token = result.get(ExpressCheckoutService.TOKEN);
						result.remove(ExpressCheckoutService.TOKEN);
						String merchantId  = platform.getManageUser();
						//RAAS
						try {
							String resp = tcService.setTransactionContextToPaypal(merchantId, token, raasParam);
							logger.info("SetTransactionContext success:{}", resp);
						} catch (Exception e) {
							logger.warn("SetTransactionContext failed!{}", e.getMessage());
						}
					}
				}
			});
			return new HashMap<String, Object>(result);
		} else {
			throw new PaypalPaymentException("SetExpressCheckout failed");
		}
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException, DataAccessException,
			PaymentRedirectException {
		
		IPNMessage ipnlistener = new IPNMessage(request, configurationMap);
		
		boolean isIpnVerified = ipnlistener.validate();
		String transactionType = ipnlistener.getTransactionType();
		Map<String, String> map = ipnlistener.getIpnMap();
		
		logger.info("******* IPN (name:value) pair : " + map + "  " +
				"######### TransactionType : " + transactionType + 
				"  ======== IPN verified : "+ isIpnVerified);
		
		logger.info("***************** Begin to process Paypal IPN Message ******************");
		
		if(!isIpnVerified) {
			throw new ValidationException("verify IPN message failed.");
		}
		return parseRequestParameterMap(map, platform);
	}

	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		return null;
	}
	
	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess){
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			//http code 非200 继续回调
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		
		Platform platform = (Platform) request.getAttribute(PaymentConstant.PAYMENT_PLATFORM);
		Long merchantId = (Long) request.getAttribute(PaymentConstant.MERCHANT_ID);
		
		String token = request.getParameter("token");
		String payerID = request.getParameter("PayerID");
		
		if(StringUtils.isBlank(token)){
			throw new ValidationException("token is required.");
		}
		
		ExpressCheckoutService ecService = ConfigAndServiceCacheManager.getECService(platform.getMerchantNo());
		if (ecService == null) {
			ConfigAndServiceCacheManager.putECService(platform.getMerchantNo(), configurationMap);
			ecService = ConfigAndServiceCacheManager.getECService(platform.getMerchantNo());
		}
		
		//发送GetExpressCheckoutDetails请求结束
		Map<String, String> resuleMap = null;
		Map<String, String> result = ecService.getExpressCheckoutDetails(token);
		logger.info(this.getClass().getSimpleName()+",result-->{}",result);
		if (result != null) {
			if (!token.equals(result.get(ExpressCheckoutService.TOKEN)) 
					|| !payerID.equals(result.get(ExpressCheckoutService.PAYER_ID))) {
				throw new PaypalPaymentException("check GetExpressCheckoutDetails resp error");
			}
			Map<String, Object> item = new HashMap<String, Object>();
			item.putAll(result);
			//发送doEc请求开始
			String notifyURL = platform.getBehindUrl(merchantId);
			resuleMap = ecService.doExpressCheckoutPayment(token, payerID, ExpressCheckoutService.PAYMENTACTION,
					item, notifyURL);
			logger.info(this.getClass().getSimpleName()+",resuleMap-->{}",resuleMap);
		}
		if (resuleMap == null) {
			throw new PaypalPaymentException("check DoExpressCheckout resp error");
		}
		
		/*String payAmount = resuleMap.get(ExpressCheckoutService.TOTAL_AMT);
		
		Map<String, Object> outParams = new HashMap<String, Object>();
		String paymentStatus = resuleMap.get(ExpressCheckoutService.PAYMENT_STATUS);
		if (TRADE_FLAG_COMPLETED.equals(paymentStatus)) {
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED); // 支付状态
		} else {
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED); // 支付状态
		}
		
		outParams.put(PaymentConstant.OPPOSITE_ORDERNO, resuleMap.get(ExpressCheckoutService.TRANSTRATION_ID)); // 对方订单号
		outParams.put(PaymentConstant.ORDER_NO, resuleMap.get(ExpressCheckoutService.INVOICE_ID)); // 蜗牛订单号
		outParams.put(PaymentConstant.OPPOSITE_MONEY, payAmount);
		outParams.put(PaymentConstant.OPPOSITE_CURRENCY, resuleMap.get(ExpressCheckoutService.CURRENCY));*/
		return StringUtils.trim(result.get(ExpressCheckoutService.INVOICE_ID));
	}
	
	
	/**
	 * 处理 {@link #asynchIPNToSnail(HttpServletRequest)} 执行后返回的Map
	 * 包装成 PaymentOrder
	 * @param paramMap
	 * @param Ip
	 * @return
	 */
	private Map<String, Object> parseRequestParameterMap(Map<String, String> paramMap, final Platform platform) {

		Map<String, Object> returneMap = new HashMap<String, Object>();
		
		logger.info("PAYPAL===========IPN========begin");
		
		if (paramMap.get("invoice") == null
				|| paramMap.get("txn_id") == null
				|| paramMap.get("receiver_email") == null
				|| paramMap.get("mc_currency") == null
				|| paramMap.get("mc_gross") == null
				|| paramMap.get("payment_status") == null) {
			
			logger.error("paypal ipn parameters is missing!");
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return returneMap;
		}
		
		String paymentStatus = paramMap.get("payment_status");

		if (TRADE_FLAG_REVERSED.equals(paymentStatus)) {
			logger.error("paypal_ipn: payment is reversed!" + paymentStatus);
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_REIMBURSE);
			return returneMap;
		}

		if (!TRADE_FLAG_COMPLETED.equals(paymentStatus)) {
			logger.error("paypal_ipn: payment is not successful!" + paymentStatus);
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_FAILED);
			return returneMap;
		}
		
		// 订单号
		String snailOrderNo = paramMap.get("invoice");
		if (StringUtils.isEmpty(snailOrderNo)) {
			logger.error("paypal_ipn parameter invoice is illegal!");
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return returneMap;
		}
		// paypal订单号  订单在paypal的标识 
		String paymentOrderNo = paramMap.get("txn_id");
		if (StringUtils.isEmpty(paymentOrderNo)) {
			logger.error("paypal_ipn parameter txn_id is illegal!");
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return returneMap;
		}
		// 蜗牛在paypal上企业帐号 
		/*String businessAccount = paramMap.get("receiver_email");
		if (StringUtils.isEmpty(businessAccount)
				|| !platform.getMerchantNo().equals(businessAccount)) {
			logger.error("paypal_ipn parameter receiver_email is illegal!");
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return returneMap;
		}*/
		String receiverId = paramMap.get("receiver_id");
		if (StringUtils.isEmpty(receiverId)
				|| !platform.getManageUser().equals(receiverId)) {
			logger.error("paypal_ipn parameter receiver_id is illegal!");
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return returneMap;
		}
		// 订单交易金额 
		String _amount = paramMap.get("mc_gross");
		// 转换成数字
		double amount = NumberUtils.toDouble(_amount);
		
		if (amount < 0) {
			logger.error("paypal_ipn parameter mc_gross is illegal!");
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return returneMap;
		}
		// 订单交易币种 
		String currency = paramMap.get("mc_currency");
		if (StringUtils.isEmpty(currency)) {
			logger.error("paypal_ipn parameter mc_currency is illegal!");
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return returneMap;
		}

		double payAmount = amount;
		String payCurrency = StringUtils.defaultString(paramMap.get("payment_currency"), currency);

		
		if (NumberUtils.isDigits(paramMap.get("payment_gross"))) {
			payAmount = Double.parseDouble(paramMap.get("payment_gross"));
		}

		PaymentOrder paymentOrder = paymentOrderService.queryOrder(snailOrderNo);
		if (paymentOrder == null) {
			logger.error("order not found by " + snailOrderNo);
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return returneMap;
		}
		
		if (paramMap.get("payer_email") != null) {
			paymentOrder.setUserName(paramMap.get("payer_email"));
		}

		/*if (paramMap.get("payer_id") != null) {
		}
		if (paramMap.get("payment_type") != null) {
		}
		if (paramMap.get("mc_fee") != null && NumberUtils.isNumber(paramMap.get("mc_fee"))) {
		}*/
		
		returneMap.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returneMap.put(PaymentConstant.OPPOSITE_ORDERNO, paymentOrderNo);
		returneMap.put(PaymentConstant.OPPOSITE_MONEY, payAmount * 100 + "");
		returneMap.put(PaymentConstant.OPPOSITE_CURRENCY, payCurrency);
		
		//不验证imprestMode,直接取订单中imprestMode
		returneMap.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED); // 支付状态
		
		return returneMap;
	}
	
	@Override
	public String getMoneyCurrency() {
		return "USD";
	}
	
	protected static RuntimeException unchecked(Throwable ex) {
		if (ex instanceof RuntimeException) {
			return (RuntimeException) ex;
		} else {
			return new RuntimeException(ex);
		}
	}

}
