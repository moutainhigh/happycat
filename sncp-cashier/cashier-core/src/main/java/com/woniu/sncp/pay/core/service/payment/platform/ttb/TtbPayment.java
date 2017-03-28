package com.woniu.sncp.pay.core.service.payment.platform.ttb;

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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.security.SecuritySSOAuth;
import com.woniu.sncp.pay.core.service.ocp.OcpAccountService;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;

@Service("ttbPayment")
public class TtbPayment extends AbstractPayment {
	
	private static final String CHARSET	= "UTF-8";
	
	@Autowired
	private OcpAccountService ocpAccountService;

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		PaymentOrder imprestOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		//Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		Long aid = SecuritySSOAuth.getLoginId();
		if(aid == null){
			aid = Long.valueOf(ObjectUtils.toString(inParams.get("aid")));
		}
		String clientIp = ObjectUtils.toString(inParams.get("clientIp"));
		String djj = ObjectUtils.toString(inParams.get("ttbDjjMoney"));
		Float money = imprestOrder.getMoney();
		BigDecimal bigOrderMoney = new BigDecimal(money.toString());
		BigDecimal orderMoney = bigOrderMoney.setScale(2, BigDecimal.ROUND_HALF_UP);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", aid);
		params.put("clientIp", clientIp);
		params.put("orderNo", imprestOrder.getOrderNo());
		//组装payInfo
		List<Map<String,Object>> listPayInfo = new ArrayList<Map<String,Object>>();
		if(StringUtils.isNotBlank(djj)){
			if(StringUtils.isNotBlank(djj)){
				String arr[] = djj.split("#");
				if(arr !=null&& arr.length >0){
					for(int i=0;i<arr.length;i++){
						String s = arr[i];
						String a[] = s.split(",");
						if(a!=null && a.length>1){
							String paytypeId = a[0];
							String amount = a[1];
							if(StringUtils.isNotBlank(amount) && NumberUtils.isNumber(amount)){
//								Float amountF = Float.valueOf(amount);
								BigDecimal bigCurrencyMoney = new BigDecimal(amount.toString());
								BigDecimal currencyMoney = bigCurrencyMoney.setScale(2, BigDecimal.ROUND_HALF_UP);
								if(currencyMoney.doubleValue() > 0){
									Map<String,Object> payInfo = new HashMap<String,Object>();
									payInfo.put("payTypeId", paytypeId);
									payInfo.put("amount", amount);
									orderMoney = orderMoney.subtract(currencyMoney);
									listPayInfo.add(payInfo);
								}
							}
						}
					}
				}
			}
		}
		if(orderMoney.doubleValue() < 0){
			throw new ValidationException("输入的代金卷金额大于应支付的金额");
		}
		//money=0则输入的代金卷金额等于应支付金额
		if(orderMoney.doubleValue() > 0){
			boolean isCurrencyO = false;
			for(Map<String,Object> payMap :listPayInfo){
				if(payMap.get("payTypeId").equals("o")){
					isCurrencyO = true;
					break;
				}
			}
			if(!isCurrencyO){
				//没有币种o
				Map<String,Object> payInfo = new HashMap<String,Object>();
				payInfo.put("payTypeId", "o");//兔兔币
				payInfo.put("amount", orderMoney.doubleValue());
				listPayInfo.add(payInfo);
			}
			
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
		Map<String, Object> retMap = ocpAccountService.chargeAmount(params);
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
			throw new ValidationException("兔兔币支付回调加密失败");
		}
		
		PaymentOrder imprestOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(imprestOrder, "兔兔币支付订单查询为空,orderNo:" + orderNo);
		
		Map<String, Object> returned = new HashMap<String, Object>();
		if("1".equals(payState)){
			logger.info("兔兔币支付返回支付成功，订单号："+orderNo);
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		}else{
			logger.info("兔兔币支付返回支付失败，订单号："+orderNo);
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}
		returned.put(PaymentConstant.PAYMENT_ORDER, imprestOrder);
		returned.put(PaymentConstant.ORDER_NO, orderNo);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);
		returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(payMoney)).multiply(new BigDecimal(100)).intValue()));
		returned.put(PaymentConstant.PAYMENT_MODE, imprestOrder.getImprestMode());
		
		return returned;
	}

	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		return null;
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
		logger.info("兔兔币加密原文：" + source+",密文：" + sign);
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
