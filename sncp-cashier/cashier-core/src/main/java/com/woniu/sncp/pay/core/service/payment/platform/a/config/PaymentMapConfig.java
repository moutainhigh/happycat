package com.woniu.sncp.pay.core.service.payment.platform.a.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.AlipayDPPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.AlipayPayment;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月23日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Configuration
public class PaymentMapConfig {

	@Resource
	AlipayDPPayment alipayDPPayment;
	@Resource
	AlipayPayment alipayPayment;
	
	@Bean(name={"paymentMap"})
	public Map<String, AbstractPayment> getPaymentMap(){
		Map<String,AbstractPayment> paymentMap = new HashMap<String,AbstractPayment>();
		paymentMap.put("PAYMENT_1001", alipayDPPayment);
//		paymentMap.put("PAYMENT_1002", nbcbDPPayment);
//		paymentMap.put("PAYMENT_1003", jdDPPayment);
		paymentMap.put("PAYMENT_1004", alipayPayment);
		/**
		 * 
				<!-- 财付通即时支付 -->
				<entry key="PAYMENT_1005" value-ref="tenpayPayment" />
				<!-- 银联在线支付 -->
				<entry key="PAYMENT_1006" value-ref="unionPayPayment" />
				<!-- 银联电子支付 -->
				<entry key="PAYMENT_1007" value-ref="chinaPayPayment" />
				<!-- 支付宝扫码支付 -->
				<entry key="PAYMENT_1008" value-ref="alipayQRCodePayment" />
				<!-- 微信扫码支付 -->
				<entry key="PAYMENT_1009" value-ref="tenpayWxPayment" />
				<!-- 移动手机支付 -->
				<entry key="PAYMENT_1010" value-ref="cmPayPayment" />
				<!-- 中国银行信用卡分期 支付-->
				<entry key="PAYMENT_1012" value-ref="chinabankPayment" />
				<!-- 神州付手机卡 支付-->
				<entry key="PAYMENT_1013" value-ref="shenzpayDPPayment" />
				<!-- 汇付宝骏网卡支付 -->
				<entry key="PAYMENT_1014" value-ref="huihubaoPayment" />
				<!-- 中国工商银行信用卡支付 -->
                <entry key="PAYMENT_1018" value-ref="icbcINBSEPayment" />
                <!-- 盛付通网银直连 -->
                <entry key="PAYMENT_1020" value-ref="shengpayBankDirectPayment" />
                <!-- PC快钱快捷支付 -->
                <entry key="PAYMENT_1021" value-ref="kuaiqianPCQuickPayment" />
                <!-- 新版银联电子支付 -->
				<entry key="PAYMENT_1032" value-ref="chinaPayPaymentNew" />
				<!-- 蜗牛移动充值卡 -->
				<entry key="PAYMENT_1036" value-ref="wnMobileCardPayment" />
				<!-- 银联在线支付新版pc -->
				<entry key="PAYMENT_1038" value-ref="unionPayPaymentNew_1" />
				
				
				<!-- 2xxx  app 端 -->
				<entry key="PAYMENT_2002" value-ref="alipayAppPayment" />
				<entry key="PAYMENT_2003" value-ref="weixinAppPayment" />
				<entry key="PAYMENT_2004" value-ref="alipayIosPayment" />
				<entry key="PAYMENT_2005" value-ref="unionPayAppPayment" />
				<entry key="PAYMENT_2006" value-ref="weixinIosPayment" />
				<entry key="PAYMENT_2007" value-ref="unionPayIosPayment" />
				
				<!-- 3xxx wap 端  -->
				<entry key="PAYMENT_3001" value-ref="alipayWapPayment" />
				<entry key="PAYMENT_3002" value-ref="tenpayWapPayment" />
				<entry key="PAYMENT_3005" value-ref="unionWapPayPayment" />
				<!-- 微信公众号支付 -->
				<entry key="PAYMENT_3003" value-ref="weixinPublicPayment" />
				
				<!-- app-android蜗牛移动充值卡 -->
				<entry key="PAYMENT_2008" value-ref="wnMobileCardPayment" />
				<!-- app-android蜗牛手机充值卡 -->
				<entry key="PAYMENT_2009" value-ref="shenzpayDPPayment" />
				
				<!-- app-ios蜗牛移动充值卡 -->
				<entry key="PAYMENT_2010" value-ref="wnMobileCardPayment" />
				<!-- app-ios蜗牛手机充值卡 -->
				<entry key="PAYMENT_2011" value-ref="shenzpayDPPayment" />
				
				<!-- 快钱快捷支付 -->
				<entry key="PAYMENT_3004" value-ref="kuaiqianQuickPayment" />
				<!-- wap蜗牛手机充值卡 -->
				<entry key="PAYMENT_3006" value-ref="shenzpayDPPayment" />
				<!-- wap蜗牛移动充值卡 -->
				<entry key="PAYMENT_3007" value-ref="wnMobileCardPayment" />
				<!-- 银联在线支付新版wap -->
				<entry key="PAYMENT_3010" value-ref="unionPayPaymentNew_1" />
				
				<!-- 新版本支付宝20160907 -->
				<entry key="PAYMENT_3009" value-ref="alipayWapAppPayment" />
				
				<!-- 兔兔币 -->
				<entry key="PAYMENT_4001" value-ref="ttbPayment" />
				<!-- 翡翠币web -->
				<entry key="PAYMENT_4002" value-ref="fcbPayment" />
				<!-- 翡翠币wap -->
				<entry key="PAYMENT_4003" value-ref="fcbPayment" />
				<!-- 翡翠币ios -->
				<entry key="PAYMENT_4004" value-ref="fcbPayment" />
				<!-- 翡翠币android -->
				<entry key="PAYMENT_4005" value-ref="fcbPayment" />
				
				<!-- 远程调用 -->
				<entry key="PAYMENT_9999" value-ref="callPayPayment" />
		 */
		return paymentMap;
	}
}
