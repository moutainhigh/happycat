package com.woniu.sncp.pay.core.service.payment.platform.shengpay;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.sdo.mas.common.api.common.entity.Extension;
import com.sdo.mas.common.api.common.entity.Header;
import com.sdo.mas.common.api.common.entity.Sender;
import com.sdo.mas.common.api.common.entity.Signature;
import com.sdo.mas.common.api.query.order.entity.syn.single.OrderQueryRequest;
import com.sdo.mas.common.api.query.order.entity.syn.single.OrderQueryResponse;
import com.sdo.mas.common.api.query.order.service.QueryOrderAPI;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;

/**
 * 
 * <p>descrption: 盛付通支付</p>
 * 
 * @author fuzl
 * @date   2015年10月26日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@Service("shengftpayPayment")
public class ShengftpayPayment extends AbstractPayment {
	
	private final static String STATUS_PAYED="01"; 
	
	@Override
	public String encode(Map<String, Object> inParams) throws ValidationException {
		String encrypted = "";
		try {
			encrypted = MD5Encrypt.encrypt((String) inParams.get("source"), "utf-8");
		} catch (RuntimeException e) {
			logger.error("盛付通支付加密异常", e);
			throw new ValidationException("盛付通支付加密异常", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("=========盛付通支付加密开始=========");
			logger.info("source：" + inParams.get("source"));
			logger.info("encrypted：" + encrypted.toUpperCase());
			logger.info("=========盛付通支付加密结束=========\n");
		}
		return encrypted.toUpperCase();
	}
	
	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		// 1.获取参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("Name", "B2CPayment");
		params.put("Version", "V4.1.1.1.1");
		params.put("Charset", "UTF-8");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		params.put("MsgSender", platform.getMerchantNo());//商户号
		params.put("SendTime", sdf.format(new Date()));
		params.put("OrderNo", paymentOrder.getOrderNo());
		params.put("OrderAmount", paymentOrder.getMoney());
		params.put("OrderTime", sdf.format(paymentOrder.getCreate()));
		params.put("PayType", "PT018,PT020");//支付类型: PT018 盛大一卡通,PT020 娱乐一卡通
//		params.put("PayChannel", "");//支付渠道
//		params.put("InstCode", "");//银行编码
		params.put("PageUrl", platform.getFrontUrl(paymentOrder.getMerchantId()));//支付成功后客户端浏览器回调地址
		params.put("NotifyUrl", platform.getBehindUrl(paymentOrder.getMerchantId()));//服务端通知发货地址
		params.put("ProductName", StringUtils.trim((String) inParams.get("productName")));
		params.put("BuyerIp", inParams.get("clientIp"));//ip
		params.put("SignType", "MD5");
		
		// 2.加密
		StringBuffer source = new StringBuffer();//加密所需的字符串
		source.append(params.get("Name"));
		source.append(params.get("Version"));
		source.append(params.get("Charset"));
		source.append(params.get("MsgSender"));
		source.append(params.get("SendTime"));
		source.append(params.get("OrderNo"));
		source.append(params.get("OrderAmount"));
		source.append(params.get("OrderTime"));
		source.append(params.get("PayType"));
//		source.append(params.get("PayChannel"));
//		source.append(params.get("InstCode"));
		source.append(params.get("PageUrl"));
		source.append(params.get("NotifyUrl"));
		source.append(params.get("ProductName"));
		source.append(params.get("BuyerIp"));
		source.append(params.get("SignType"));
		source.append(platform.getPayKey());//支付密钥
		
		Map<String, Object> encryptParams = new HashMap<String, Object>();
		encryptParams.put("source", source.toString());
		
		String sign = encode(encryptParams);// 数字签名（32位的md5加密,加密后转换成大写）
		
		params.put("SignMsg", sign);
		params.put("payUrl", platform.getPayUrl()); // 提交给对方的支付地址
		params.put("urlcode", "UTF-8"); // 提交给对方的支付编码
		
		return params;
	}
	
	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("OrderNo");
	}
	
	@Override
	public void paymentReturn(Map<String, Object> inParams, HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess)
			super.responseAndWrite(response, "OK");
		else
			super.responseAndWrite(response, "fail");
	}
	

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request, Platform platform) 
			throws ValidationException,DataAccessException, PaymentRedirectException {
		//接受服务器url post参数                            
		String name=request.getParameter("Name");   //版本名称
		String version = request.getParameter("Version");   //版本号
		String charset = request.getParameter("Charset"); //字符集 默认utf-8
		String traceNo = request.getParameter("TraceNo"); //请求序列号
		String msgSender = request.getParameter("MsgSender");//发送方标识
		String sendTime = request.getParameter("SendTime");//发送请求时间
		String instCode = request.getParameter("InstCode");//银行编码
		String orderNo = request.getParameter("OrderNo");//我方订单号
		String orderAmount = request.getParameter("OrderAmount");//订单号金额
		String transNo = request.getParameter("TransNo");//盛付通订单号
		String transAmount = request.getParameter("TransAmount");//盛付通实际支付金额
		String transStatus = request.getParameter("TransStatus");//支付状态
		String transType = request.getParameter("TransType");//盛付通交易类型
		String transTime = request.getParameter("TransTime");//订单创建时间
		String errorCode = request.getParameter("ErrorCode");//错误代码
		String errorMsg = request.getParameter("ErrorMsg");//错误消息
		String ext1 = request.getParameter("Ext1");//扩展1
		String signType = request.getParameter("SignType");//签名类型
		String signMsg = request.getParameter("SignMsg");//签名串

		StringBuffer sb = new StringBuffer(); //加密字符串
		sb.append(name);
		sb.append(version);
		sb.append(charset);
		sb.append(traceNo);
		sb.append(msgSender);
		sb.append(sendTime);
		if(!StringUtils.isEmpty(instCode)){
			sb.append(instCode);
		}
		sb.append(orderNo);
		sb.append(orderAmount);
		sb.append(transNo);
		sb.append(transAmount);
		sb.append(transStatus);
		sb.append(transType);
		sb.append(transTime);
		sb.append(platform.getMerchantNo());
		
		if(!StringUtils.isEmpty(errorCode)){
			sb.append(errorCode);
		}
		if(!StringUtils.isEmpty(errorMsg)){
			sb.append(errorMsg);
		}
		if(!StringUtils.isEmpty(ext1)){
			sb.append(ext1);
		}
		
		sb.append(signType);
		sb.append(platform.getPayKey());

		Map<String, Object> encryptParams = new HashMap<String, Object>();
		encryptParams.put("source", sb.toString());
		String sign =encode(encryptParams);// 数字签名（32位的md5加密,加密后转换成小写）
		
		logger.debug("盛付通支付参数 - name:{},version:{},charset:{}," +
				"traceNo:{},msgSender:{},sendTime:{}" +
				",instCode:{},orderNo:{},orderAmount:{},transNo:{}" +
				",transAmount:{},transStatus:{},transType:{},transTime:{}" +
				",errorCode:{},errorMsg:{},ext1:{},signType:{},signMsg:{}",
				new String[]{name,version,charset,traceNo,msgSender,sendTime
				,instCode,orderNo,orderAmount,transNo,transAmount,transStatus
				,transType,transTime,errorCode,errorMsg,ext1,signType,signMsg});
		
		// 校验参数
		Map<String, Object> returned = new HashMap<String, Object>();
		if (!sign.equals(signMsg)) {
			logger.info("==============盛付通支付后台加密处理失败=================");
			logger.info("我方加密串：" + sign);
			logger.info("对方加密串：" + signMsg);
			logger.info("==============盛付通支付后台加密处理结束=================\n");
			throw new ValidationException("盛付通支付平台加密校验失败");
		}
		
		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "支付订单查询为空,orderNo:" + orderNo);
		
		returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, transNo);//支付方订单交易号
//		returned.put(ImprestConstant.IMPREST_OPPOSITE_MONEY, String.valueOf(NumberUtils.toFloat(orderAmount)*100));
		returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(orderAmount)).multiply(new BigDecimal(100)).intValue()));
		//不验证paymentMode,直接取订单中paymentMode
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());

		return returned;
	}
	
	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		// 1.获取订单和平台信息
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		// 2.封装参数并到盛付通去校验
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String sendTime = sdf.format(new Date());
		StringBuffer sb = new StringBuffer(); //加密字符串
		sb.append("QUERY_ORDER_REQUEST");
		sb.append("V4.3.1.1.1");
		sb.append("UTF-8");
		sb.append(platform.getMerchantNo());//商户号
		sb.append(sendTime);
		sb.append(platform.getMerchantNo()+paymentOrder.getOrderNo());
		sb.append(paymentOrder.getOrderNo());
//		sb.append(imprestOrder.getPayPlatformOrderId());
		sb.append("MD5");
		sb.append(platform.getPayKey());

		Map<String, Object> encryptParams = new HashMap<String, Object>();
		encryptParams.put("source", sb.toString());
		String sign =encode(encryptParams);// 数字签名（32位的md5加密,加密后转换成小写）
		
		OrderQueryRequest request = new OrderQueryRequest();
		Header head = new Header();
		request.setHeader(head);
		
		Sender sender = new Sender();
		head.setSender(sender);		
		com.sdo.mas.common.api.common.entity.Service service = new com.sdo.mas.common.api.common.entity.Service();
		head.setService(service);	
		
		Extension ext = new Extension();
		request.setExtension(ext);
		
		Signature signature = new Signature();
		request.setSignature(signature);
		
		head.getService().setServiceCode("QUERY_ORDER_REQUEST");
		head.getService().setVersion("V4.3.1.1.1");
		head.setCharset("UTF-8");
		head.getSender().setSenderId(platform.getMerchantNo());
		head.setSendTime(sendTime);
		request.setMerchantNo(platform.getMerchantNo()+paymentOrder.getOrderNo());
		request.setOrderNo(paymentOrder.getOrderNo());
//		request.setTransNo(imprestOrder.getPayPlatformOrderId());
		signature.setSignType("MD5");
		signature.setSignMsg(sign);

		OrderQueryResponse response = this.doQueryOrder(request,platform.getPayCheckUrl());
		
		// 3.获取盛付通验证返回参数并校验
		String return_serviceCode = response.getHeader().getService().getServiceCode();
		String return_version = response.getHeader().getService().getVersion();
		String return_charset = response.getHeader().getCharset();
		String return_traceNo = response.getHeader().getTraceNo();
		String return_senderId = response.getHeader().getSender().getSenderId();
		String return_sendTime = response.getHeader().getSendTime();
		String return_orderNo = response.getOrderNo();
		String return_orderAmount = response.getOrderAmount();
		String return_transNo = response.getTransNo();
		String return_transAmoumt = response.getTransAmoumt();
		String return_transStatus = response.getTransStatus();
		String return_transTime = response.getTransTime();
		String return_signType = response.getSignature().getSignType();
		String return_signMsg = response.getSignature().getSignMsg();
//		String return_ext1 = response.getExtension().getExt1();
		String return_errorCode = response.getReturnInfo().getErrorCode();
		String return_errorMsg = response.getReturnInfo().getErrorMsg();
		
		StringBuffer return_sb = new StringBuffer(); //加密字符串
		return_sb.append(return_serviceCode);
		return_sb.append(return_version);
		return_sb.append(return_charset);
		if(!StringUtils.isEmpty(return_traceNo)){
			return_sb.append(return_traceNo);
		}
		return_sb.append(return_senderId);
		if(!StringUtils.isEmpty(return_sendTime)){
			return_sb.append(return_sendTime);
		}
		if(!StringUtils.isEmpty(return_orderNo)){
			return_sb.append(return_orderNo);
		}
		if(!StringUtils.isEmpty(return_orderAmount)){
			return_sb.append(return_orderAmount);
		}
		if(!StringUtils.isEmpty(return_transNo)){
			return_sb.append(return_transNo);
		}
		if(!StringUtils.isEmpty(return_transAmoumt)){
			return_sb.append(return_transAmoumt);
		}
		if(!StringUtils.isEmpty(return_transStatus)){
			return_sb.append(return_transStatus);
		}
		if(!StringUtils.isEmpty(return_transTime)){
			return_sb.append(return_transTime);
		}
		if(!StringUtils.isEmpty(return_errorCode)){
			return_sb.append(return_errorCode);
		}
		if(!StringUtils.isEmpty(return_errorMsg)){
			return_sb.append(return_errorMsg);
			logger.error("盛付通订单查询返回:code="+return_errorCode+",errorMsg="+return_errorMsg);
		}
		//return_sb.append(return_ext1);
		return_sb.append(return_signType);
		return_sb.append(platform.getPayKey());

		Map<String, Object> return_encryptParams = new HashMap<String, Object>();
		return_encryptParams.put("source", return_sb.toString());
		String return_sign =encode(return_encryptParams);// 数字签名（32位的md5加密,加密后转换成小写）
		
		Map<String, Object> outParams = new HashMap<String, Object>();
		if (!return_sign.equals(return_signMsg)) {
			logger.info("==============盛付通订单查询后台加密处理失败=================");
			logger.info("我方加密串：" + return_sign);
			logger.info("对方加密串：" + return_signMsg);
			logger.info("==============盛付通订单查询后台加密处理结束=================\n");
			//throw new ValidationException("盛付通订单查询平台加密校验失败");
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return outParams;
		}
		
		// 4.验证订单支付状态
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		if(STATUS_PAYED.equals(return_transStatus)){
			payState = PaymentConstant.PAYMENT_STATE_PAYED;
		}
		
		outParams.put(PaymentConstant.OPPOSITE_ORDERNO, return_transNo); // 对方订单号
		outParams.put(PaymentConstant.PAYMENT_STATE, payState); // 支付状态
//		outParams.put(ImprestConstant.IMPREST_OPPOSITE_MONEY, String.valueOf(NumberUtils.toFloat(return_orderAmount) * 100));
		outParams.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(return_orderAmount)).multiply(new BigDecimal(100)).intValue()));
		return outParams;
	}
	

	private OrderQueryResponse doQueryOrder(OrderQueryRequest request,String queryUrl){
		try{
			JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
			factory.setServiceClass(QueryOrderAPI.class);
			factory.setAddress(queryUrl);
			QueryOrderAPI service = (QueryOrderAPI) factory.create();
			
			OrderQueryResponse response = service.queryOrder(request);
			return response;
		}catch(Exception e){		
			logger.error(e.getMessage(),e);
			throw new ValidationException("订单校验:访问盛付通平台异常");
		}		
	}

}
