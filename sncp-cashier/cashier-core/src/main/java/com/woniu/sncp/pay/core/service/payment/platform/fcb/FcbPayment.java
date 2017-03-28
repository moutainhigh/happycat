package com.woniu.sncp.pay.core.service.payment.platform.fcb;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.service.ocp.OcpAccountService;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;

/**
 * 翡翠币支付平台
 * @author sungs
 *
 */
@Service("fcbPayment")
public class FcbPayment extends AbstractPayment {
	
	private static final String CHARSET	= "UTF-8";
	
	@Autowired
	private OcpAccountService ocpAccountService;

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		String clientIp = ObjectUtils.toString(inParams.get("clientIp"));
		Long aid = paymentOrder.getAid();
		Float money = paymentOrder.getMoney();
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", aid);
		params.put("appId", "74");
		params.put("spId", "7");
		params.put("areaId", "-1");
		params.put("clientIp", clientIp);
		params.put("orderNo", paymentOrder.getOrderNo());
		//组装payInfo
		List<Map<String,Object>> listPayInfo = new ArrayList<Map<String,Object>>();
		if(money > 0){
			Map<String,Object> payInfo = new HashMap<String,Object>();
			payInfo.put("payTypeId", "74");//翡翠币
			payInfo.put("amount", money);
			listPayInfo.add(payInfo);
		}
		params.put("payInfo", listPayInfo);
		//组装listItemInfo
		List<Map<String,Object>> listItemInfo = new ArrayList<Map<String,Object>>();
		Map<String,Object> itemInfo = new HashMap<String,Object>();
		itemInfo.put("counterid", "0");
		itemInfo.put("itemid", "-1");
		itemInfo.put("appid", "-1");
		itemInfo.put("areaid", "-1");
		itemInfo.put("num", "1");
		itemInfo.put("paytypeid", "-1");
		itemInfo.put("amt", "0");
		listItemInfo.add(itemInfo);
		params.put("listItemInfo", listItemInfo);
		//执行扣费
		Map<String, Object> retMap = ocpAccountService.deductAmount(params);
		return retMap;
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException, DataAccessException,
			PaymentRedirectException {
		String orderNo = ObjectUtils.toString(request.getParameter("orderNo")).trim();
		String payState = ObjectUtils.toString(request.getParameter("payState")).trim();
		String payMoney = ObjectUtils.toString(request.getParameter("payMoney")).trim();
		String oppositeOrderNo = ObjectUtils.toString(request.getParameter("oppositeOrderNo")).trim();
		String sign = ObjectUtils.toString(request.getParameter("sign")).trim();
		
		String key = platform.getBackendKey();
		Map<String, Object> params = new TreeMap<String, Object>();
		params.put("orderNo", orderNo);
		params.put("payState", payState);
		params.put("payMoney", payMoney);
		params.put("oppositeOrderNo", oppositeOrderNo);
		params.put("key", key);
		
		String md5 = this.encode(params);
		if(!md5.equalsIgnoreCase(sign)){
			logger.info("我方加密：" + md5 + ",对方加密：" +sign);
			throw new ValidationException("翡翠币支付回调加密失败");
		}
		
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "翡翠币支付订单查询为空,orderNo:" + orderNo);
		
		Map<String, Object> returned = new HashMap<String, Object>();
		if("1".equals(payState)){
			logger.info("翡翠币支付返回支付成功，订单号："+orderNo);
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		}else{
			logger.info("翡翠币支付返回支付失败，订单号："+orderNo);
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.ORDER_NO, orderNo);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);
		returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(payMoney)).multiply(new BigDecimal(100)).intValue()));
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		
		return returned;
	}

	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Map<String, Object> outParams = new HashMap<String, Object>();
		outParams.put(PaymentConstant.OPPOSITE_ORDERNO, paymentOrder.getPayPlatformOrderId());// 订单号
		outParams.put(PaymentConstant.OPPOSITE_MONEY, paymentOrder.getMoney());// 订单金额
		outParams.put(PaymentConstant.PAYMENT_STATE, paymentOrder.getImprestState());
		return outParams;
	}

	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		String md5key = ObjectUtils.toString(inParams.get("key"));
		inParams.remove("key");
		StringBuilder sb = new StringBuilder();
		for (Iterator<Entry<String, Object>> keyValue = inParams.entrySet().iterator(); keyValue.hasNext();) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) keyValue.next();
			String key = entry.getKey();
			String value = ObjectUtils.toString(entry.getValue());
			sb.append(key).append(value);
		}
		String source = sb.toString()+md5key;
		String sign = MD5Encrypt.encrypt(source,CHARSET);
		logger.info("翡翠币加密原文：" + source+",密文：" + sign);
		return sign;
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		if(isImprestedSuccess){
			super.responseAndWrite(response, "success");
		}else{
			super.responseAndWrite(response, "fail");
		}

	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("orderNo");
	}

}
