package com.woniu.sncp.pay.core.service.payment.platform.alipay.qr;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptStringUtils;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.AlipayPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.tools.AlipayHelper;
import com.woniu.sncp.pojo.payment.PaymentOrder;

/**
 * 支付宝 扫码支付
 * 其他同即时支付
 * 
 * @author luzz
 *
 */
@Service("alipayQRCodePayment")
public class AlipayQRCodePayment extends AlipayPayment {
	
	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams) {
		// 1.拼装参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		String out_trade_no = paymentOrder.getOrderNo();
		String priKey = AlipayHelper.readText(platform.getPrivateUrl());

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("service", "create_direct_pay_by_user");
		params.put("partner", platform.getMerchantNo());
		params.put("notify_url", platform.getBehindUrl(paymentOrder.getMerchantId()));
		params.put("return_url", platform.getFrontUrl(paymentOrder.getMerchantId()));
		params.put("body", StringUtils.trim((String) inParams.get("productName")));// 商品具体描述
		params.put("out_trade_no", out_trade_no);
		params.put("payment_type", "1");
		params.put("seller_email", platform.getManageUser()); // 我方在支付宝的email
		params.put("subject", StringUtils.trim((String) inParams.get("productName")));
		params.put("total_fee", ObjectUtils.toString(paymentOrder.getMoney()));
		params.put("qr_pay_mode", "0");//0=订单码-简约前置模式;1=订单码-前置模式 ;  2=订单码-跳转模式
		params.put("it_b_pay", "24h");//超时时间24h
		params.put("_input_charset", _charset_encode);

		LinkedHashMap<String, Object> linkedHashMap = AlipayHelper.sortMap(params);

		String source = EncryptStringUtils.linkedHashMapToStringWithKey(linkedHashMap, true);
		// 2.加密
		Map<String, Object> encryptParams = new HashMap<String, Object>();
		encryptParams.put("source", source);
		encryptParams.put("priKey", priKey);
		String sign = this.encode(encryptParams);

		// 3.剩余需要传递参数
		params.put("sign", sign);
		params.put("sign_type", "DSA");
		params.put("payUrl", platform.getPayUrl()); // 提交给对方的支付地址
		params.put("acceptCharset", _charset_encode); // 提交给对方的支付编码
		
		// 4.风险检测
		alipaySecurityRiskDetect.riskDetect(inParams,alipaySecurityRiskDetect.TERMINAL_TYPE_WEB,_charset_encode);

		return params;
	}
}
