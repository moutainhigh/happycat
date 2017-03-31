package com.woniu.sncp.pay.core.service.payment.platform.kuaiqian.www;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com._99bill.www.gatewayapi.services.gatewayOrderQuery.GatewayOrderQueryServiceLocator;
import com.bill99.seashell.domain.dto.gatewayquery.GatewayOrderDetail;
import com.bill99.seashell.domain.dto.gatewayquery.GatewayOrderQueryRequest;
import com.bill99.seashell.domain.dto.gatewayquery.GatewayOrderQueryResponse;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.lang.ObjectUtil;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.service.PaymentOrderService;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.kuaiqian.encrypt.Pkipair;
import com.woniu.sncp.pojo.payment.PaymentOrder;
/**
 * 
 * <p>descrption: PC收银台快钱快捷支付</p>
 * 
 * @author fuzl
 * @date   2015年11月2日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@Service("kuaiqianPCQuickPayment")
public class KuaiqianPCQuickPayment extends AbstractPayment {
	@Resource
	private PaymentOrderService paymentOrderService;
	/**
	 * Bill支付平台通讯RSA加密
	 * 
	 * @param signMsg 源字符串
	 * @param priKeyFilePath 私钥仓库文件路径
	 * @param pfxPwd 私钥文件密码
	 * @return
	 * @throws ValidationException
	 */
	public String signMsg(Map<String, Object> inParams) throws ValidationException{
		String prikeyPath = (String)inParams.get("prikeyPath");//证书路径
		String source = (String)inParams.get("source");
		String key = (String)inParams.get("key");
		String encrypted = "";
		try {
			encrypted = Pkipair.signMsg(source, prikeyPath, key);
			//encrypted = Pkipair.signMsg(source, "F:\\data\\woniu_www_rsa_prikey.pfx", "123456");
			logger.info("=========快钱人民币支付=========");
		} catch (RuntimeException e) {
			logger.error("快钱人民币RSA加密异常", e);
			throw new ValidationException("快钱人民币RSA加密异常", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("=========快钱人民币支付RSA加密开始=========");
			logger.info("source：" + inParams.get("source"));
			logger.info("encrypted：" + encrypted);
			logger.info("=========快钱人民币支付RSA加密结束=========\n");
		}
		return encrypted;
	}
	
	/**
	 * 接受响应验签方法
	 * 
	 * @param val 拼接响应参数 
	 * @param msg 加密串
	 * @param pubKeyFilePath 公钥文件路径
	 * @return
	 * @throws ValidationException
	 */
	public boolean enCodeByCer(Map<String, Object> encryParams, String msg,String pubKeyFilePath) throws ValidationException{
		return Pkipair.enCodeByCer((String) encryParams.get("source"), msg, pubKeyFilePath);
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		LinkedHashMap<String, Object> linkedParams = new LinkedHashMap<String, Object>();
		//协议参数
		linkedParams.put("inputCharset", "1");//1 代表 UTF-8; 2 代表 GBK; 3 代表 GB2312
		linkedParams.put("pageUrl", platform.getFrontUrl(paymentOrder.getMerchantId()));
		linkedParams.put("bgUrl", platform.getBehindUrl(paymentOrder.getMerchantId()));
		linkedParams.put("version", "v2.0");
		linkedParams.put("language", "1");//1 代表中文显示
		linkedParams.put("signType", "4");//4代表PKI加密,DSA或者RSA签名方式
		//买卖双方信息参数
		linkedParams.put("merchantAcctId", platform.getMerchantNo());
		//业务参数
		linkedParams.put("orderId", paymentOrder.getOrderNo());//商户订单号
		linkedParams.put("orderAmount", ObjectUtil.toString((new BigDecimal(paymentOrder.getMoney().toString())).multiply(new BigDecimal(100)).intValue()));//商户订单金额,以分为单位
		linkedParams.put("orderTime", DateFormatUtils.format(paymentOrder.getCreate(), "yyyyMMddHHmmss"));//商户订单提交时间
		linkedParams.put("productName", ObjectUtil.toString(inParams.get("productName")));
		linkedParams.put("productNum", ObjectUtil.toString(paymentOrder.getAmount())); // 商品数量
		
		String cardType = ObjectUtils.toString(inParams.get("cardtype"));
		
		
		//银行类型取bankCardType,收银台js改版,类型改为0储蓄 1信用		
		String bankCardType = String.valueOf(inParams.get("bankCardType"));//收银台传递的银行卡类型,0储蓄 1信用
		//银行编码取defaultbank
		String bankId = ObjectUtils.toString(inParams.get("defaultbank"));
		//判断是否属于快钱平台需要转换银行对应编码
		bankId = paymentOrderService.getKqBankCode(bankId);
		if(null!=bankCardType ){
			if("0".equals(bankCardType)){
				linkedParams.put("payType", "21-1");//21是快捷,21-1 代表储蓄卡快捷
				linkedParams.put("bankId",bankId); //银行支付时使用
			}else if("1".equals(bankCardType)){
				linkedParams.put("payType", "21-2");//21是快捷,21-2 代表信用卡快捷
				linkedParams.put("bankId",bankId); //银行支付时使用
			}else{
				linkedParams.put("payType", "21");//210是支持两种卡类型
			}
		}else{
			//获取不到默认银行编码
			logger.info("===========快钱人民币支付获取不到默认银行编码==========");
		}
		
//		if(null!= defaultbank){
//			//增加异常判断，是否包含"-"
//			if(defaultbank.contains("-")){
//				bankId = defaultbank.substring(0, defaultbank.indexOf("-"));
//			}
//			if(defaultbank.contains("210")){
//				linkedParams.put("payType", "21");//210是支持两种卡类型
//			}else if(defaultbank.contains("211")){
//				linkedParams.put("payType", "21-1");//21是快捷,21-1 代表储蓄卡快捷
//				linkedParams.put("bankId",bankId); //银行支付时使用
//			}else if(defaultbank.contains("212")){
//				linkedParams.put("payType", "21-2");//21是快捷,21-2 代表信用卡快捷
//				linkedParams.put("bankId",bankId); //银行支付时使用
//			}
//		}else{
//			//获取不到默认银行编码
//			logger.info("===========快钱人民币支付获取不到默认银行编码==========");
//		}
		
		String source = super.linkedHashMapToStringWithKey(linkedParams, true);
		// 2.参数加密
		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", source);
		encodeParams.put("key", platform.getPayKey());//通讯密码
		encodeParams.put("prikeyPath", platform.getPrivateUrl());//证书路径
		String encrypted = this.signMsg(encodeParams);// RSA加密
		linkedParams.put("signMsg", encrypted);
		
		// 3.剩余需要传递参数
		linkedParams.put("payUrl", platform.getPayUrl()); // 提交给对方的支付地址

		return linkedParams;
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException, DataAccessException,
			PaymentRedirectException {
		LinkedHashMap<String, Object> encryParams = new LinkedHashMap<String, Object>();
		String orderNo = StringUtils.trim(request.getParameter("orderId"));
		String oppositeOrderNo = StringUtils.trim(request.getParameter("dealId"));
		//String extendInfo = StringUtils.trim(request.getParameter("ext1"));
		String payResult = StringUtils.trim(request.getParameter("payResult"));
		int orderAmount = NumberUtils.toInt(StringUtils.trim(request.getParameter("orderAmount")));
		String oppositeEncrypted = StringUtils.trim(request.getParameter("signMsg"));

		encryParams.put("merchantAcctId", StringUtils.trim(request.getParameter("merchantAcctId")));
		encryParams.put("version", StringUtils.trim(request.getParameter("version")));
		encryParams.put("language", StringUtils.trim(request.getParameter("language")));
		encryParams.put("signType", StringUtils.trim(request.getParameter("signType")));
		encryParams.put("payType", StringUtils.trim(request.getParameter("payType")));
		encryParams.put("bankId", StringUtils.trim(request.getParameter("bankId")));
		encryParams.put("orderId", orderNo);
		encryParams.put("orderTime", StringUtils.trim(request.getParameter("orderTime")));
		encryParams.put("orderAmount", orderAmount);
		encryParams.put("dealId", oppositeOrderNo);
		encryParams.put("bankDealId", StringUtils.trim(request.getParameter("bankDealId")));
		encryParams.put("dealTime", StringUtils.trim(request.getParameter("dealTime")));
		encryParams.put("payAmount", StringUtils.trim(request.getParameter("payAmount")));
		encryParams.put("fee", StringUtils.trim(request.getParameter("fee")));
		//encryParams.put("ext1", extendInfo);
		encryParams.put("ext2", StringUtils.trim(request.getParameter("ext2")));
		encryParams.put("payResult", payResult);
		encryParams.put("errCode", StringUtils.trim(request.getParameter("errCode")));
		//更换加密方式，不需要key

		String source = super.linkedHashMapToStringWithKey(encryParams, true);
		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", source);
		//String encrypted = this.encode(encodeParams);
		//更换加密方式
		boolean isCer = this.enCodeByCer(encodeParams, oppositeEncrypted, platform.getPublicUrl());
		
		if (!isCer) {
			if (logger.isInfoEnabled()) {
				logger.info("==============快钱人民币后台加密处理失败=================");
				logger.info("对方加密串：" + oppositeEncrypted);
				logger.info("==============快钱人民币后台加密处理结束=================\n");
			}
			throw new ValidationException("支付平台加密校验失败");
		}

		// 我们提交的参数原样返回校验 (orderNo + key MD5值)
//		if (!super.encode(orderNo, paymentPlatform.getAuthKey()).equals(extendInfo)) {
//			if (logger.isInfoEnabled()) {
//				logger.info("==============快钱人民币支付平台订单orderNo + key校验失败=================");
//				logger.info("我方原文：" + orderNo + paymentPlatform.getAuthKey());
//				logger.info("对方传回扩展信息：" + extendInfo);
//				logger.info("==============快钱人民币支付平台订单orderNo + key校验结束=================\n");
//			}
//			throw new ValidationException("快钱人民币支付平台订单orderNo + key校验失败");
//		}

		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "订单查询为空,orderNo:" + orderNo);

		Map<String, Object> returned = new HashMap<String, Object>();
		if ("10".equals(payResult)) { // 支付成功
			logger.info("快钱人民币返回支付成功");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else if ("11".equals(payResult)) { // 支付失败
			logger.info("快钱人民币返回支付失败");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_FAILED);
		} else {
			logger.info("快钱人民币返回未支付");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}

		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);
		returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf(orderAmount));
		//不验证imprestMode,直接取订单中imprestMode
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		return returned;
	}

	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		// 1.加密
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		String inputCharset = "1";
		String version = "v2.0";
		String signType = "1";
		String merchantAcctId = platform.getMerchantNo();
		String queryType = "0";// 0 按商户订单号单笔查询（返回该订单信息）
		String queryMode = "1";// 1 代表简单查询（返回基本订单信息)
		String orderId = paymentOrder.getOrderNo();
		String key = platform.getQueryKey(); // 对方平台的订单查询密钥，快钱人民币特殊，查询和支付密钥不一致

		LinkedHashMap<String, Object> linkedParams = new LinkedHashMap<String, Object>();
		linkedParams.put("inputCharset", inputCharset);
		linkedParams.put("version", version);
		linkedParams.put("signType", signType);
		linkedParams.put("merchantAcctId", merchantAcctId);
		linkedParams.put("queryType", queryType);
		linkedParams.put("queryMode", queryMode);
		linkedParams.put("orderId", orderId);
		linkedParams.put("key", key); // 对方平台我方密码

		String source = super.linkedHashMapToStringWithKey(linkedParams, true);
		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", source);
		String encrypted = this.encode(encodeParams); // 加密

		// 2.向快钱充值卡请求参数
		GatewayOrderQueryRequest queryRequest = new GatewayOrderQueryRequest();
		queryRequest.setInputCharset(inputCharset);
		queryRequest.setVersion(version);
		queryRequest.setSignType(Integer.parseInt(signType));
		queryRequest.setMerchantAcctId(merchantAcctId);
		queryRequest.setQueryType(Integer.parseInt(queryType));
		queryRequest.setQueryMode(Integer.parseInt(queryMode));
		queryRequest.setOrderId(orderId);
		queryRequest.setSignMsg(encrypted);

		GatewayOrderQueryServiceLocator locator = new GatewayOrderQueryServiceLocator();
		if(StringUtils.isNotBlank(platform.getPayCheckUrl())){
			locator.setgatewayOrderQueryEndpointAddress(platform.getPayCheckUrl());
		}
		GatewayOrderQueryResponse queryResponse = null;
		Map<String, Object> outParams = new HashMap<String, Object>();
		try {
			queryResponse = locator.getgatewayOrderQuery().gatewayOrderQuery(queryRequest);
		} catch (Exception e) {
			throw new ValidationException("订单校验:访问快钱人民币平台异常", e);
		}

		// 3.请求返回
		String errorCode = queryResponse.getErrCode();
		if (StringUtils.isNotBlank(errorCode)) {
			logger.error("订单校验:快钱人民币,具体查看平台文档,errorCode:" + errorCode);
			if ("31003".equals(errorCode))
				throw new ValidationException("订单校验:快钱人民币,对方返回[31003]商户订单号不存在");
			else if ("31005".equals(errorCode))
				throw new ValidationException("订单校验:快钱人民币,对方返回[31005]订单号对应的交易支付未成功");
			else 
				throw new ValidationException("订单校验:快钱人民币,具体查看平台文档,errorCode:" + errorCode);
		}
		GatewayOrderDetail[] gatewayOrderDetails = queryResponse.getOrders();

		for (GatewayOrderDetail detail : gatewayOrderDetails) {
			if (orderId.equals(detail.getOrderId())) {
				if (logger.isInfoEnabled())
					logger.info("快钱人民币验证返回：" + JsonUtils.toJson(detail));

				if ("10".equals(detail.getPayResult())) {
					logger.info("快钱人民币校验结果：支付成功,orderNo:" + orderId);
					outParams.put(PaymentConstant.OPPOSITE_ORDERNO, detail.getDealId());
					outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
					outParams.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf(detail.getPayAmount())); // 快钱人民币实际支付金额
					return outParams;
				} else {
					logger.error("快钱人民币校验结果：未支付,orderNo:" + orderId);
					outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
					return outParams;
				}
			}
		}
		outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		return outParams;
	}

	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		String encrypted = "";
		try {
			encrypted = MD5Encrypt.encrypt((String) inParams.get("source"), "utf-8");
		} catch (RuntimeException e) {
			logger.error("快钱人民币加密异常", e);
			throw new ValidationException("快钱人民币加密异常", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("=========快钱人民币支付加密开始=========");
			logger.info("source：" + inParams.get("source"));
			logger.info("encrypted：" + encrypted);
			logger.info("=========快钱人民币支付加密结束=========\n");
		}
		return encrypted;
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		HttpServletRequest request = (HttpServletRequest) inParams.get("request");
		String orderNo = this.getOrderNoFromRequest(request);
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		long platformId = paymentOrder.getPayPlatformId();
		long merchantId = paymentOrder.getMerchantId();
		Platform platform = platformService.queryPlatform(merchantId, platformId);
		Assert.notNull(platform);
		
		String returned = null;
		String code = "1";
		if(isImprestedSuccess){
			logger.info("www证书验证成功...code:"+code);
		}else{
			code = "-1";
		}
		returned = "<result>"+code+"</result><redirecturl>" + platform.getFrontUrl(paymentOrder.getMerchantId()) + "?orderId=" + orderNo
				+ "</redirecturl>";

		if (logger.isInfoEnabled())
			logger.info("返回给快钱人民币网关：" + returned);
		super.responseAndWrite(response, returned);
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return StringUtils.trim(request.getParameter("orderId"));
	}

}
