package com.woniu.sncp.pay.core.service.payment.platform.oversea.rixty;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.net.NetServiceException;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.rixty.utils.NVPClient;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.rixty.utils.NVPCodec;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.rixty.utils.NVPCodec.Field;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.rixty.utils.NVPException;
import com.woniu.sncp.pojo.payment.PaymentOrder;

@Service
public class RixtyPayment extends AbstractPayment {

	private final static Logger logger = LoggerFactory.getLogger(RixtyPayment.class);


	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		return "";
	}


	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams) throws ValidationException {
		
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		Validate.notNull(paymentOrder, "order is null");
		Validate.notEmpty(paymentOrder.getCurrency(), "order.currency is required");
		Validate.notEmpty(paymentOrder.getOrderNo(), "order.orderNo is required");
		
		Validate.isTrue(paymentOrder.getAmount() > 0, "order.amount is required or incorrect.");
		
		String productName = MapUtils.getString(inParams, "productName", "");
		paymentOrder.setImprestCardName(productName);
		
		
		Map<String,Object> map = new LinkedHashMap<String, Object>();
//				String gameId = paymentOrder.getGameId() + "";
		String shopName = paymentOrder.getProductname();
		String currency = paymentOrder.getMoneyCurrency();
		double money = paymentOrder.getMoney();
		String transactionId = paymentOrder.getOrderNo();
		long userId = paymentOrder.getAid();
	  
		NVPClient client = new NVPClient(platform.getPayUrl(), platform.getMerchantNo(),platform.getPayKey(), platform.getBackendKey());
		try {
			NVPCodec results = client.setRixtyCheckout(NVPCodec.Field.AMT.param(money),
					NVPCodec.Field.DESC.param(shopName),
					NVPCodec.Field.USERID.param(userId+""),
					NVPCodec.Field.CUSTOM.param(transactionId),
					NVPCodec.Field.CURRENCYCODE.param(currency));
			logger.info("get iframe Url：" + results);
			String iframeUrl = results.get(NVPCodec.Field.IFRAMEURL);
	 		map.put("payUrl", iframeUrl);
		} catch (NVPException e) {
			logger.error("setRixtyCheckout error", e);
			throw new NetServiceException("Rixty connection error", e);
		}
//		map.put("payUrl", platform.getPayUrl());
		return map;
	}


	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request, Platform platform)
			throws ValidationException, DataAccessException, PaymentRedirectException {
		
		Map<String, Object> returned = new LinkedHashMap<>();
		
		String token = request.getParameter("token");
		String payerID = request.getParameter("PayerID");
		NVPClient client = new NVPClient(platform.getPayUrl(),platform.getMerchantNo(),platform.getPayKey(), platform.getBackendKey());
		
		NVPCodec details = null;
		NVPCodec results = null;
		/*details = new NVPCodec();
		details.fromString("COUNTRYCODE=ES&PAYERID=1DRSFO4NUL&AMT=9.00&DESC=30+Gold&ACK=Success&CUSTOM=" + 
				"20171129-1054-007-0000003726&CURRENCYCODE=USD&TOKEN=1CVF2HRZ3QHUK1BRJDMS&USERID=28811048");
		String nvps = "PAYMENTSTATUS=Completed&PAYMENTTYPE=instant&SETTLEAMT=6.8400" +
				"&TRANSACTIONID=1Q79BHMGXJ70&AMT=3.00&FEEAMT=2.1600&ACK=Success" + 
				"&ORDERTIME=2017-11-27T16%3A15%3A58Z&CURRENCYCODE=USD&TRANSACTIONTYPE=EXPRESS_CHECKOUT";
		results = new NVPCodec();
		results.fromString(nvps);*/
 		try {
	 		details = client.getRixtyCheckoutDetails(token);
	 		logger.info("detail info : " + details);
	 		results = client.doRixtyCheckoutPayment(token, payerID);
	 		logger.info("payement info: "+ results);
 		} catch (NVPException nvpe) {
 			logger.error("transaction failed:{}", nvpe.getMessage());
 			throw new NetServiceException("Rixty connection error", nvpe);
 		}
 		if(!"Completed".equals(results.get(NVPCodec.Field.PAYMENTSTATUS))
 		        ||!"Success".equals(results.get(NVPCodec.Field.ACK))){
 			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
 			logger.error("verity PaymentStatus not Completed or success");
			throw new NetServiceException("Rixty connection error");
 		}
 		
 		String orderNo = details.get(NVPCodec.Field.CUSTOM);
 		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "微信APP支付订单查询为空,orderNo:" + orderNo);
		
 		//Validate merchantTrackingID amountValue currencyCode
 		if (paymentOrder == null) {
 			logger.error("Billing result notification: parameter {} is empty or incorrect!", "merchantTrackingID");
 			throw new ValidationException("Billing result notification: parameter merchantTrackingID is empty or incorrect!");
 		}
		
		// 设置充值类型 - 不传则默认1-网银支付
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, results.get(NVPCodec.Field.TRANSACTIONID));
		returned.put(PaymentConstant.OPPOSITE_CURRENCY, results.get(NVPCodec.Field.CURRENCYCODE));
		returned.put(PaymentConstant.OPPOSITE_MONEY, Double.parseDouble(results.get(NVPCodec.Field.AMT)) * 100 + "");
		returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		
		return returned;
	}


	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		return null;
	}


	@Override
	public void paymentReturn(Map<String, Object> inParams, HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess){
			response.setStatus(HttpServletResponse.SC_OK);
			super.responseAndWrite(response, "200 OK");
		} else {
			//http code 非200 继续回调
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return null;
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request, Platform platform) {

		String token = request.getParameter("token");
		if (StringUtils.isEmpty(token)) {
			return null;
		}
		String orderNo = null;
		NVPClient client = new NVPClient(platform.getPayUrl(), platform.getMerchantNo(), platform.getPayKey(), platform.getBackendKey());
		try {
			NVPCodec details = client.getRixtyCheckoutDetails(token);
			logger.info("client detail info :" + details);
			
			orderNo = details.get(Field.CUSTOM);
			if (!platform.getMerchantNo().equals(details.get(Field.USER)) || !platform.getPayKey().equals(details.get(Field.PWD))
					|| !platform.getBackendKey().equals(details.get(Field.SIGNATURE))) {
				logger.info("verify failed!");
				request.setAttribute(PaymentConstant.ORDER_FRONT_CALLBACK_STATUS, "failed");
				return orderNo;
			}
		} catch (NVPException nvpe) {
			logger.error("get client detail failed:" + nvpe.getMessage(), nvpe);
		}
		return orderNo;
	}
	
	
}
