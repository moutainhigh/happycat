package com.woniu.sncp.pay.core.service.payment.platform.a.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.DelegatingFilterProxy;

import com.woniu.sncp.pay.common.threadpool.ThreadPool;
import com.woniu.sncp.pay.core.filter.AuthenticationCommonFilter;
import com.woniu.sncp.pay.core.filter.LogMonitorFilter;
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
		
		//兔兔币、余额支付
		paymentMap.put("PAYMENT_4001", ttbPayment);//<!-- 兔兔币 -->
		paymentMap.put("PAYMENT_4002", fcbPayment);//<!-- 翡翠币web -->
		paymentMap.put("PAYMENT_4003", fcbPayment);//<!-- 翡翠币wap -->
		paymentMap.put("PAYMENT_4004", fcbPayment);//<!-- 翡翠币ios -->
		paymentMap.put("PAYMENT_4005", fcbPayment);//<!-- 翡翠币android -->
		paymentMap.put("PAYMENT_9999", callPayPayment);//<!-- 远程调用 -->
		
		return paymentMap;
	}
	
	
	@Bean(name={"moneyBookersMethodMap"})
	public Map<String, String> getMoneyBookersMethodMap(){
		Map<String,String> moneyBookersMethodMap = new HashMap<String,String>();
		moneyBookersMethodMap.put("WLT", "WLT");
		moneyBookersMethodMap.put("VSA", "VSA");
		moneyBookersMethodMap.put("MSC", "MSC");
		moneyBookersMethodMap.put("AMX", "AMX");
		moneyBookersMethodMap.put("JCB", "JCB");
		moneyBookersMethodMap.put("MAE", "MAE");
		moneyBookersMethodMap.put("DIN", "DIN");
		return moneyBookersMethodMap;
	}
	
	
	@Value("${paypal.mode}")
	private String paypalMode;
	@Value("${paypal.clientId}")
	private String paypalClientId;
	@Value("${paypal.clientSecret}")
	private String clientSecret;
	
	@Value("${paypal.acct1.UserName}")
	private String paypalUserName;
	
	@Value("${paypal.acct1.Password}")
	private String paypalPassword;
	
	@Value("${paypal.acct1.Signature}")
	private String paypalSignature;
	
	@Bean(name={"paypalConfigurationMap"})
	public Map<String, String> getPaypalConfigurationMap(){
		Map<String,String> paypalConfigurationMap = new HashMap<String,String>();
		paypalConfigurationMap.put("mode", paypalMode);//<!-- Endpoints are varied depending on whether sandbox OR live is chosen for mode -->
		paypalConfigurationMap.put("sandbox.EmailAddress", "paypalsnail@snailgame.net");
		paypalConfigurationMap.put("clientId", paypalClientId);//<!-- Credentials -->
		paypalConfigurationMap.put("clientSecret", clientSecret);
		paypalConfigurationMap.put("http.ConnectionTimeOut", "5000");//<!-- Connection Information -->
		paypalConfigurationMap.put("http.Retry", "2");
		paypalConfigurationMap.put("http.ReadTimeOut", "30000");
		paypalConfigurationMap.put("http.MaxConnection", "100");
		paypalConfigurationMap.put("http.GoogleAppEngine", "false");//<!-- Set this property to true if you are using the PayPal SDK within a Google App Engine java app -->
		paypalConfigurationMap.put("acct1.UserName", paypalUserName);//<!-- Account Credential -->
		paypalConfigurationMap.put("acct1.Password", paypalPassword);
		paypalConfigurationMap.put("acct1.Signature", paypalSignature);
		return paypalConfigurationMap;
	}
}
