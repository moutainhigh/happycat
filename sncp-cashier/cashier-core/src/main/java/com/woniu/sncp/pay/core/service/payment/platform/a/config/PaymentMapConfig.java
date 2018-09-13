package com.woniu.sncp.pay.core.service.payment.platform.a.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.woniu.sncp.pay.core.service.payment.platform.bluepay.BluepayBankPayment;
import com.woniu.sncp.pay.core.service.payment.platform.bluepay.BluepayCardPayment;
import com.woniu.sncp.pay.core.service.payment.platform.bluepay.BluepaySmsPayment;
import com.woniu.sncp.pay.core.service.payment.platform.codapay.CodapayPayment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.CallPayPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.AlipayDPPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.AlipayPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.app.AlipayAppPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.app.AlipayIosPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.qr.AlipayQRCodePayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.qr.AlipayQRCodeUrlPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.wap.AlipayWapAppPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.wap.AlipayWapPayment;
import com.woniu.sncp.pay.core.service.payment.platform.chinabank.ChinabankPayment;
import com.woniu.sncp.pay.core.service.payment.platform.chinapay.ChinaPayPayment;
import com.woniu.sncp.pay.core.service.payment.platform.chinapay.ChinaPayPaymentNew;
import com.woniu.sncp.pay.core.service.payment.platform.cmpay.CmPayPayment;
import com.woniu.sncp.pay.core.service.payment.platform.fcb.FcbPayment;
import com.woniu.sncp.pay.core.service.payment.platform.huifubao.HuifubaoPayment;
import com.woniu.sncp.pay.core.service.payment.platform.icbc.ICBCINBSEPayment;
import com.woniu.sncp.pay.core.service.payment.platform.jd.JdDPPayment;
import com.woniu.sncp.pay.core.service.payment.platform.kuaiqian.KuaiqianQuickPayment;
import com.woniu.sncp.pay.core.service.payment.platform.kuaiqian.www.KuaiqianPCQuickPayment;
import com.woniu.sncp.pay.core.service.payment.platform.moneybookers.MoneyBookersPayment;
import com.woniu.sncp.pay.core.service.payment.platform.nbcb.NbcbDPPayment;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.OpenbucksPayment;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.rixty.RixtyPayment;
import com.woniu.sncp.pay.core.service.payment.platform.oversea.xsolla.XsollaPayment;
import com.woniu.sncp.pay.core.service.payment.platform.paypal.PaypalPayment;
import com.woniu.sncp.pay.core.service.payment.platform.shengpay.ShengftpayBankDirectPayment;
import com.woniu.sncp.pay.core.service.payment.platform.shenzpay.ShenzpayDPPayment;
import com.woniu.sncp.pay.core.service.payment.platform.tenpay.TenpayPayment;
import com.woniu.sncp.pay.core.service.payment.platform.tenpay.app.WeixinAppPayment;
import com.woniu.sncp.pay.core.service.payment.platform.tenpay.app.WeixinIosPayment;
import com.woniu.sncp.pay.core.service.payment.platform.tenpay.qr.TenpayWxPayment;
import com.woniu.sncp.pay.core.service.payment.platform.tenpay.qr.WeixinPaymentForQrCode;
import com.woniu.sncp.pay.core.service.payment.platform.tenpay.wap.TenpayWapPayment;
import com.woniu.sncp.pay.core.service.payment.platform.tenpay.weixin.WeixinPublicPayment;
import com.woniu.sncp.pay.core.service.payment.platform.ttb.TtbPayment;
import com.woniu.sncp.pay.core.service.payment.platform.unionpay.UnionPayAppPayment;
import com.woniu.sncp.pay.core.service.payment.platform.unionpay.UnionPayIosPayment;
import com.woniu.sncp.pay.core.service.payment.platform.unionpay.UnionPayPayment;
import com.woniu.sncp.pay.core.service.payment.platform.unionpay.UnionWapPayPayment;
import com.woniu.sncp.pay.core.service.payment.platform.unionpay.new1.UnionPayPaymentNew_1;
import com.woniu.sncp.pay.core.service.payment.platform.woniu.WnMobileCardPayment;

/**
 * <p>descrption: 平台映射配置类</p>
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
	@Resource
	NbcbDPPayment nbcbDPPayment;
	@Resource
	JdDPPayment jdDPPayment;
	@Resource
	TenpayPayment tenpayPayment;
	@Resource
	UnionPayPayment unionPayPayment;
	@Resource
	AlipayQRCodePayment alipayQRCodePayment;
	@Resource
	ChinaPayPayment chinaPayPayment;
	@Resource
	TenpayWxPayment tenpayWxPayment;
	@Resource
	TtbPayment ttbPayment;
	@Resource
	CmPayPayment cmPayPayment;
	@Resource
	ChinabankPayment chinabankPayment;
	@Resource
	ShenzpayDPPayment shenzpayDPPayment;
	@Resource
	HuifubaoPayment huifubaoPayment;
	@Resource
	ICBCINBSEPayment icbcINBSEPayment;
	@Resource
	ShengftpayBankDirectPayment shengpayBankDirectPayment;
	@Resource
	KuaiqianPCQuickPayment kuaiqianPCQuickPayment;
	@Resource
	ChinaPayPaymentNew chinaPayPaymentNew;
	@Resource
	WnMobileCardPayment wnMobileCardPayment;
	@Resource
	UnionPayPaymentNew_1 unionPayPaymentNew_1;
	@Resource
	AlipayQRCodeUrlPayment alipayQRCodeUrlPayment;
	@Resource
	WeixinPaymentForQrCode weixinPaymentForQrCode;
	@Resource
	PaypalPayment paypalPayment;
	@Resource
	MoneyBookersPayment moneyBookersPayment;
	
	@Resource
	private RixtyPayment rixtyPayment;
	
	@Resource
	private OpenbucksPayment openbucksPayment;
	
	@Resource
	private XsollaPayment xsollaPayment;



	@Resource(name="BluepaySmsPayment")
	private BluepaySmsPayment bluepaySmsPayment;

	@Resource(name="BluapayBankPayment")
	BluepayBankPayment bluepayBankPayment;

	@Resource(name="BluepayCardPayment")
	BluepayCardPayment  bluepayCardPayment;

	@Resource(name="codapayPayment")
	CodapayPayment codapayPayment;



	@Resource
	AlipayAppPayment alipayAppPayment;
	
	
	@Resource
	WeixinAppPayment weixinAppPayment;
	@Resource
	AlipayIosPayment alipayIosPayment;
	@Resource
	UnionPayAppPayment unionPayAppPayment;
	@Resource
	WeixinIosPayment weixinIosPayment;
	@Resource
	UnionPayIosPayment unionPayIosPayment;
	
	@Resource
	AlipayWapPayment alipayWapPayment;
	@Resource
	TenpayWapPayment tenpayWapPayment;
	@Resource
	WeixinPublicPayment weixinPublicPayment;
	@Resource
	KuaiqianQuickPayment kuaiqianQuickPayment;
	@Resource
	UnionWapPayPayment unionWapPayPayment;
	@Resource
	AlipayWapAppPayment alipayWapAppPayment;
	
	
	@Resource
	FcbPayment fcbPayment;
	
	@Resource
	CallPayPayment callPayPayment;
	
	@Bean(name={"paymentMap"})
	public Map<String, AbstractPayment> getPaymentMap(){
		Map<String,AbstractPayment> paymentMap = new HashMap<String,AbstractPayment>();
		//PC接入
		paymentMap.put("PAYMENT_1001", alipayDPPayment);
		paymentMap.put("PAYMENT_1002", nbcbDPPayment);
		paymentMap.put("PAYMENT_1003", jdDPPayment);
		paymentMap.put("PAYMENT_1004", alipayPayment);
		paymentMap.put("PAYMENT_1005", tenpayPayment);//<!-- 财付通即时支付 -->
		paymentMap.put("PAYMENT_1006", unionPayPayment);//<!-- 银联在线支付 -->
		paymentMap.put("PAYMENT_1007", chinaPayPayment);//<!-- 银联电子支付 -->
		paymentMap.put("PAYMENT_1008", alipayQRCodePayment);//<!-- 支付宝扫码支付 -->
		paymentMap.put("PAYMENT_1009", tenpayWxPayment);//<!-- 微信扫码支付 -->
		paymentMap.put("PAYMENT_1010", cmPayPayment);//<!-- 移动手机支付 -->
		paymentMap.put("PAYMENT_1012", chinabankPayment);//<!-- 中国银行信用卡分期 支付-->
		paymentMap.put("PAYMENT_1013", shenzpayDPPayment);//<!-- 神州付手机卡 支付-->
		paymentMap.put("PAYMENT_1014", huifubaoPayment);//<!-- 汇付宝骏网卡支付 -->
		paymentMap.put("PAYMENT_1018", icbcINBSEPayment);//<!-- 中国工商银行信用卡支付 -->
		paymentMap.put("PAYMENT_1020", shengpayBankDirectPayment);//<!-- 盛付通网银直连 -->
		paymentMap.put("PAYMENT_1021", kuaiqianPCQuickPayment);//<!-- PC快钱快捷支付 -->
		paymentMap.put("PAYMENT_1026", paypalPayment);//<!-- Paypal -->
		paymentMap.put("PAYMENT_1027", moneyBookersPayment);//<!-- MoneyBookers -->
		
		paymentMap.put("PAYMENT_1032", chinaPayPaymentNew);//<!-- 新版银联电子支付 -->
		paymentMap.put("PAYMENT_1036", wnMobileCardPayment);//<!-- 蜗牛移动充值卡 -->
		paymentMap.put("PAYMENT_1038", unionPayPaymentNew_1);//<!-- 银联在线支付新版pc -->
		paymentMap.put("PAYMENT_1039", alipayQRCodeUrlPayment);//<!-- 支付宝扫码新 -->
		paymentMap.put("PAYMENT_1040", weixinPaymentForQrCode);//<!-- 微信扫码新 -->
		
		paymentMap.put("PAYMENT_1053", openbucksPayment);//<!-- Openbucks -->
		paymentMap.put("PAYMENT_1054", rixtyPayment);//<!-- Rixty -->
		paymentMap.put("PAYMENT_1055", xsollaPayment);//<!-- Xsolla -->

		paymentMap.put("PAYMENT_1056", bluepaySmsPayment);//
		paymentMap.put("PAYMENT_1057", bluepayCardPayment);//
		paymentMap.put("PAYMENT_1058", bluepayBankPayment);//
		paymentMap.put("PAYMENT_1059", codapayPayment);//

		
		//app
		paymentMap.put("PAYMENT_2002", alipayAppPayment);//
		paymentMap.put("PAYMENT_2003", weixinAppPayment);//
		paymentMap.put("PAYMENT_2004", alipayIosPayment);//
		paymentMap.put("PAYMENT_2005", unionPayAppPayment);//
		paymentMap.put("PAYMENT_2006", weixinIosPayment);//
		paymentMap.put("PAYMENT_2007", unionPayIosPayment);//
		paymentMap.put("PAYMENT_2008", wnMobileCardPayment);//<!-- app-android蜗牛移动充值卡 -->
		paymentMap.put("PAYMENT_2009", shenzpayDPPayment);//<!-- app-android蜗牛手机充值卡 -->
		paymentMap.put("PAYMENT_2010", wnMobileCardPayment);//<!-- app-ios蜗牛移动充值卡 -->
		paymentMap.put("PAYMENT_2011", shenzpayDPPayment);//<!-- app-ios蜗牛手机充值卡 -->
		
		//wap
		paymentMap.put("PAYMENT_3001", alipayWapPayment);//
		paymentMap.put("PAYMENT_3002", tenpayWapPayment);//
		paymentMap.put("PAYMENT_3003", weixinPublicPayment);//<!-- 微信公众号支付 -->
		paymentMap.put("PAYMENT_3004", kuaiqianQuickPayment);//<!-- 快钱快捷支付 -->
		paymentMap.put("PAYMENT_3005", unionWapPayPayment);//
		paymentMap.put("PAYMENT_3006", shenzpayDPPayment);//<!-- wap蜗牛手机充值卡 -->
		paymentMap.put("PAYMENT_3007", wnMobileCardPayment);//<!-- wap蜗牛移动充值卡 -->
		paymentMap.put("PAYMENT_3009", alipayWapAppPayment);//<!-- 新版本支付宝20160907 -->
		paymentMap.put("PAYMENT_3010", unionPayPaymentNew_1);//<!-- 银联在线支付新版wap -->


		paymentMap.put("PAYMENT_3021", bluepaySmsPayment);//<!-- 银联在线支付新版wap -->
		paymentMap.put("PAYMENT_3023", bluepayCardPayment);//<!-- 银联在线支付新版wap -->

		paymentMap.put("PAYMENT_3018", codapayPayment);//<!-- 银联在线支付新版wap -->




		
		//兔兔币、余额支付
		paymentMap.put("PAYMENT_4001", ttbPayment);//<!-- 兔兔币 -->
		paymentMap.put("PAYMENT_4002", fcbPayment);//<!-- 翡翠币web -->
		paymentMap.put("PAYMENT_4003", fcbPayment);//<!-- 翡翠币wap -->
		paymentMap.put("PAYMENT_4004", fcbPayment);//<!-- 翡翠币ios -->
		paymentMap.put("PAYMENT_4005", fcbPayment);//<!-- 翡翠币android -->
		paymentMap.put("PAYMENT_4011", ttbPayment);//<!-- PC兔兔币 -->
		paymentMap.put("PAYMENT_4012", ttbPayment);//<!-- wap兔兔币 -->
		paymentMap.put("PAYMENT_4013", ttbPayment);//<!-- android兔兔币 -->
		paymentMap.put("PAYMENT_4014", ttbPayment);//<!-- ios兔兔币 -->
		paymentMap.put("PAYMENT_9999", callPayPayment);//<!-- 远程调用 -->
		
		return paymentMap;
	}
	
}
