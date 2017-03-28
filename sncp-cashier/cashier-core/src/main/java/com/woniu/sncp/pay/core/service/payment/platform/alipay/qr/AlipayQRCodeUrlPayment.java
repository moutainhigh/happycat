package com.woniu.sncp.pay.core.service.payment.platform.alipay.qr;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.tools.AlipayHelper;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.wap.AlipayWapAppPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;

/**
 * 支付宝 扫码支付 其他同即时支付
 * 
 * @author luzz
 *
 */
@Service("alipayQRCodeUrlPayment")
public class AlipayQRCodeUrlPayment extends AlipayWapAppPayment {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams) {
		// 1.拼装参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		String out_trade_no = paymentOrder.getOrderNo();
		String priKey = AlipayHelper.readText(platform.getPrivateUrl());
		String pubKey = AlipayHelper.readText(platform.getPublicUrl());

		String ext = ObjectUtils.toString(platform.getExtend());
		String appId = "";
		if (StringUtils.isNotBlank(ext)) {
			JSONObject extend = JSONObject.parseObject(ext);
			appId = extend.getString("appId");
		}

		// 实例化客户端
		AlipayClient alipayClient = new DefaultAlipayClient(platform.getPayUrl(), appId, priKey, "json", "utf-8", pubKey, "RSA");
		// 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.open.public.template.message.industry.modify
		AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
		request.setApiVersion("1.0");
		// SDK已经封装掉了公共参数，这里只需要传入业务参数
		// 此次只是参数展示，未进行字符串转义，实际情况下请转义
		request.setNotifyUrl(platform.getBehindUrl(paymentOrder.getMerchantId()));
		// request.setReturnUrl(platform.getFrontUrl(paymentOrder.getMerchantId()));
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("out_trade_no", out_trade_no);// 商户订单号,64个字符以内、只能包含字母、数字、下划线；需保证在商户端不重复
		params.put("total_amount", ObjectUtils.toString(paymentOrder.getMoney()));// 订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]如果同时传入了【打折金额】，【不可打折金额】，【订单总金额】三者，则必须满足如下条件：【订单总金额】=【打折金额】+【不可打折金额】
		params.put("subject", StringUtils.trim((String) inParams.get("productName")));// 订单标题

		if (null != platform.getTransTimeout()) {
			String payTimeout = "";
			if (platform.getTransTimeout() > 0) {
				payTimeout = platform.getTransTimeout() + "m";
				// 该笔订单允许的最晚付款时间，逾期将关闭交易。取值范围：1m～15d。m-分钟，h-小时，d-天，1c-当天（1c-当天的情况下，无论交易何时创建，都在0点关闭）。
				// 该参数数值不接受小数点， 如 1.5h，可转换为 90m。
			} else if (platform.getTransTimeout() == 0) {
				payTimeout = "1c";
			} else {
				payTimeout = platform.getTransTimeout() + "d";
			}
			params.put("timeout_express", payTimeout);
		}

		params.put("body", StringUtils.trim((String) inParams.get("productName")));// 对交易或商品的描述
		request.setBizContent(JSON.toJSONString(params));
		AlipayTradePrecreateResponse response = null;
		try {
			response = alipayClient.execute(request);
			// 调用成功，则处理业务逻辑
			if (response.isSuccess()) {
				params.put("code", "success");
				params.put("code_url", response.getQrCode());
				params.put("msgcode", response.getSubCode());
				params.put("msg", response.getSubMsg());
				return params;
			} else {
				params.put("code", "fail");
				params.put("msgcode", response.getSubCode());
				params.put("msg", response.getSubMsg());
			}
		} catch (AlipayApiException e) {
			logger.error("out_trade_no:" + out_trade_no, e);
			params.put("code", "fail");
			params.put("msgcode", "10002");
			params.put("msg", e.getMessage());
		} finally {
			logger.error(String.format("PayUrl:%s,appId:%s,out_trade_no:%s,in_params:%s,response:%s", platform.getPayUrl(), appId, out_trade_no,
					JSON.toJSONString(request), response == null ? "响应为空" : JSON.toJSONString(response)));
		}
		return params;
	}
}
