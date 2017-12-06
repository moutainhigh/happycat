package com.woniu.sncp.pay.core.service.payment.platform.oversea.xsolla;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.helpers.JAXBHelper;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.xsolla.dto.XsollaResponseCancel;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.xsolla.dto.XsollaResponseCheck;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.xsolla.dto.XsollaResponsePay;
import com.woniu.sncp.pojo.payment.PaymentOrder;

@Service
public class XsollaPayment extends AbstractPayment {

	private final static Logger logger = LoggerFactory.getLogger(XsollaPayment.class);

	
	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams) throws ValidationException {
		
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

//		Map<String, Object> extendsparam = JSON.parseObject(paymentOrder.getInfo(), new TypeReference<Map<String, Object>>(){});
//		Map<String, Object> platformExt = JSON.parseObject(platform.getPlatformExt(), new TypeReference<Map<String, Object>>(){});
		
//		int point = MapUtils.getIntValue(extendsparam, "points");
//		String email = MapUtils.getString(extendsparam, "email");
//		String serverTeamId = MapUtils.getString(extendsparam, "teamId");
//		String projectId = MapUtils.getString(extendsparam, "projectId");
//		String securityKey = MapUtils.getString(extendsparam, "securityKey");
		
//		if (StringUtils.isEmpty(serverTeamId)) {
//			throw new ValidationException("Xsolla teamId v3 is required!");
//		}
		
//		String theme = MapUtils.getString(platformExt, "theme", "10005");
		
		String payUrl = platform.getPayUrl();
		Map<String, Object> map = new LinkedHashMap<>();
		
//		map.put("theme", theme);
		map.put("local", "en");
//		map.put("project", projectId);
		
		map.put("v1", paymentOrder.getAid());
		map.put("orderNo", paymentOrder.getOrderNo());
//		map.put("v3", serverTeamId);
//		if (point > 0) {
//			map.put("out", point);
//		}
//		String siganature = generateMd5Key(projectId, "10005", userId 
//				+ "", IPUtils.ipToLong(ip) + "", teamId, project.getSecurtyKey());
//		String sign = DigestUtils.md5Hex(projectId + theme + paymentOrder.getAid() + paymentOrder.getOrderNo() + serverTeamId + securityKey);
//		map.put("sign", sign);
		
		//https://secure.xsolla.com/paystation2/index.php
		//?theme=10005&local=en&project=${projectId}&v1=${sessionScope.userId}&v2=${v2}&v3=${teamId}&sign=${sign}
//		map.put("signature", generateSignature(map, platform.getMerchantNo(), platform.getPayKey()));
//		payUrl += "?" + toQueryString(map);
		map.put("payUrl", payUrl);
		return map;
	}


	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request, Platform platform)
			throws ValidationException, DataAccessException, PaymentRedirectException {
		
		Map<String, Object> result = new LinkedHashMap<>();
		
//		Map<String, Object> platformExt = JSON.parseObject(platform.getPlatformExt(), new TypeReference<Map<String, Object>>(){});

		String orderNo = request.getParameter("orderNo");
		String amount = request.getParameter("amount");
		String currency = request.getParameter("currency");
		String txid = request.getParameter("txid");
		String sign = request.getParameter("sign");
		
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "Validate merchantTrackingID not empty,orderNo:" + orderNo);
		
 		if (paymentOrder == null) {
 			logger.error("Billing result notification: parameter {} is empty or incorrect!", "orderNo");
 			throw new ValidationException("Xsolla validate v1 is not found!");
 		}
			
		String data = orderNo + amount + currency + txid + platform.getPayKey();
		if (!sign.toLowerCase().equals(DigestUtils.md5Hex(data))) {
			logger.error("Billing result notification: parameter {} is empty or incorrect!", "sign");
			throw new ValidationException("Xsolla validate hash is incorrect!");
		}
		
		//--------------------------
		
		result.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		result.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		result.put(PaymentConstant.OPPOSITE_ORDERNO, txid);
		result.put(PaymentConstant.OPPOSITE_CURRENCY, currency);
		result.put(PaymentConstant.OPPOSITE_MONEY, NumberUtils.createBigDecimal(amount).multiply(NumberUtils.createBigDecimal("100")).doubleValue() + "");
		
		return result;
	}
	
	public Map<String, Object> validateBackParams1(HttpServletRequest request, Platform platform)
			throws ValidationException, DataAccessException, PaymentRedirectException {
		
		Map<String, Object> result = new LinkedHashMap<>();
		
		Map<String, Object> platformExt = JSON.parseObject(platform.getPlatformExt(), new TypeReference<Map<String, Object>>(){});

		////////v1 aid v2 orderNo v3 teamId
		String command = request.getParameter("command");
		String md5 = request.getParameter("md5");
		String v1 = request.getParameter("v1");
		//ip
//		String v2 = request.getParameter("v2");
//		String v3 = request.getParameter("v3");
		
		
		result.put("command", command);
		// 订单查询
		String orderNo = v1;
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "Validate merchantTrackingID not empty,orderNo:" + orderNo);
		
 		if (paymentOrder == null) {
 			logger.error("Billing result notification: parameter {} is empty or incorrect!", "v1");
 			throw new ValidationException("Xsolla validate v1 is not found!");
 		}
		
 		//检查处理
		if ("check".equals(command)) {
			//验证签名
			if (!checkSignature(command, md5, platform.getPayKey(),v1)) {
				logger.error("check signature failed!");
				result.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_FAILED);
				result.put("result", "7");
				return result;
			}
			//
			
		}
		//支付处理
		if ("pay".equals(command)) {
			String id = request.getParameter("id");
			/** sum 就是点数 **/
			String sum = request.getParameter("sum");
			
			if (md5 == null || command == null || v1 == null || id == null || sum == null) {
				// 参数错误
	 			logger.error("Billing result notification: parameters is empty or incorrect!");
	 			throw new ValidationException("Xsolla parameters is empty or incorrect!");
			}
			String data = command + v1 + id + platform.getPayKey();
			if (!md5.equalsIgnoreCase(DigestUtils.md5Hex(data))) {
				logger.error("Billing result notification: parameter {} is empty or incorrect!", "md5");
				throw new ValidationException("Xsolla validate hash is incorrect!");
			}
			
			//--------------------------
			
			
			String currency = MapUtils.getString(platformExt, "currency", "USD");
			Double price = MapUtils.getDouble(platformExt, "price");
			
			result.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
			result.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
			result.put(PaymentConstant.OPPOSITE_ORDERNO, id);
			result.put(PaymentConstant.OPPOSITE_CURRENCY, currency);
			result.put(PaymentConstant.OPPOSITE_MONEY, NumberUtils.createBigDecimal(price + "")
					.multiply(NumberUtils.createBigDecimal(sum))
					.multiply(NumberUtils.createBigDecimal("100")));
			
			return result;
		}
		
		//取消处理
		if ("cancel".equals(command)) {
			return result;
		} else {
			result.put("result", "7");
			return result;
		}
	}


	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String encode(Map<String, Object> inParams) throws ValidationException {
		return null;
	}


	@Override
	public void paymentReturn(Map<String, Object> inParams, HttpServletResponse response, boolean isImprestedSuccess) {
		
		String command = MapUtils.getString(inParams, "command");
		
		if ("check".equals(command)) {
			XsollaResponseCheck checkResp = new XsollaResponseCheck();
			if (isImprestedSuccess) {
				checkResp.setResult("0");
			} else {
				checkResp.setResult("7");
			}
			super.responseAndWrite(response, JAXBHelper.getInstance().toXML(checkResp, "windows-1251", false, false));
		} else if ("pay".equals(command)) {
			XsollaResponsePay payResp = new XsollaResponsePay();
			if (isImprestedSuccess) {
				payResp.setResult("0");
			}
			super.responseAndWrite(response, JAXBHelper.getInstance().toXML(payResp, "windows-1251", false, false));
		} else if ("cancel".equals(command)) {
			XsollaResponseCancel cancelResp = new XsollaResponseCancel();
			if (isImprestedSuccess) {
				cancelResp.setResult("0");
			}
			super.responseAndWrite(response, JAXBHelper.getInstance().toXML(cancelResp, "windows-1251", false, false));
		}
		
	}


	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		
		return request.getParameter("v1");
	}
	
	/**
	 * 
	 * 检查 校验码
	 * 
	 * @param command 方法名
	 * @param args 需要加密的参数
	 * 
	 * */
	private boolean checkSignature(String command, String md5, String screct, String ...args){
		if(md5 == null){
			return false;
		}
		
		StringBuilder sb = new StringBuilder("");
		sb.append(command);
		for(String arg: args){
			sb.append(arg);
		}
		sb.append(screct);
		String text = sb.toString();
		String md5Str = DigestUtils.md5Hex(text);
		logger.debug(text + "-----" + md5Str);
		return md5Str.equals(md5.toLowerCase());
		
	}
	
	protected static String toQueryString(Map<String, Object> map) {
		if (map == null || map.isEmpty()) {
			return "";
		}
		StringBuilder strBuilder = new StringBuilder();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			strBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		strBuilder.deleteCharAt(strBuilder.length() - 1);
		return strBuilder.toString();
	}
	
}
