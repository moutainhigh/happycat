package com.woniu.sncp.pay.core.service.payment.platform.bluepay;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.encrypt.MD5Encrypt;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.codapay.schema.PaymentResult;
import com.woniu.sncp.pojo.payment.PaymentOrder;


@Service("BluapayBankPayment")
public class BluepayBankPayment extends BluepaySmsPayment {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * CodaPay支付编码
	 */
	protected final String _charset_encode = "utf-8";

	protected BluepayConfig getConfig(Platform platform) {

		String ext = ObjectUtils.toString(platform.getExtend());
		if (StringUtils.isNotBlank(ext)) {


			return JSONObject.parseObject(ext).toJavaObject(BluepayConfig.class);

		}
		return null;
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams) {
		// 1.拼装参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		BluepayConfig config = getConfig( platform);

		String currencyStr = paymentOrder.getMoneyCurrency();
		logger.info("paymentOrder currency:{}", currencyStr);
		if (StringUtils.isBlank(currencyStr)) {
			currencyStr = paymentOrder.getCurrency();
			logger.info("paymentOrder currency:{}", currencyStr);
		}
		Map<String, String> urls = config.getBankPayUrl();
		String url = MapUtils.getString(urls, currencyStr);
		String productId = platform.getMerchantNo();

		Assert.hasLength(url, "没有打到币种" + currencyStr + "对应的支付地址");
		String orderNo = paymentOrder.getOrderNo();
		String price = paymentOrder.getMoney().toString();

		Map<String, Object> params = new HashMap<String, Object>();
//?status=509&price=&transactionId=123456465&code=509&description=Card NO(pin) and Serial NO does not match.Please check and try later. (Code:509)&cardNo=38178468546812736089&provider=bluecoins&customerId=1536648455&productId=1&operatorId=undefined
		String frontUrl=platform.getFrontUrl(paymentOrder.getMerchantId());
		String payUrl =null;
		if(StringUtils.contains(url,"?")){
			payUrl=		 url +"&"+ String.format("productId=%s&transactionId=%s&price=%s", productId, orderNo, price);

		}else{
			payUrl=		 url +"?"+ String.format("productId=%s&transactionId=%s&price=%s", productId, orderNo, price);

		}
		params.put("payUrl", payUrl);

		return params;
	}




//	@Override
//	public Map<String, Object> validateBackParams(HttpServletRequest request, Platform platform)
//			throws ValidationException, DataAccessException, PaymentRedirectException {
//		logger.info("validateBackParams params:{}", JsonUtils.toJson(toParameterMap(request)));
//
//
//
//		String queryString=request.getQueryString();
//		String signStr=StringUtils.substringBefore(queryString,"&encrypt=")+platform.getBackendKey();
//		String sign=MD5Encrypt.encrypt(signStr,"utf-8");
//		logger.info("签名字符串：{},签名{}",signStr,sign);
//		if(StringUtils.equalsIgnoreCase(sign,request.getParameter("encrypt"))){
//			logger.info("签名字符串：{},签名{}",signStr,sign);
//			  throw new ValidationException("支付平台加密校验失败");
//
//		}
//
//
//		String orderNo =  request.getParameter("t_id");;
//		// 订单查询
//		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
//		Assert.notNull(paymentOrder, "支付订单查询为空,orderNo:" + orderNo);
//
// 		String oppositeOrderNo = request.getParameter("bt_id");
//
//
//
//		String payResult = StringUtils.trim(request.getParameter("status"));
//
//		Map<String, Object> returned = new HashMap<String, Object>();
//		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
//		if (StringUtils.equals(payResult, "200")) { // 支付成功
//			logger.info("CodaPay返回支付成功");
//			String paymentMoney = StringUtils.trim(request.getParameter("price"));
//			BigDecimal totalPrice = new BigDecimal(paymentMoney).setScale(0);
//			BigDecimal money = new BigDecimal(paymentOrder.getMoney()).multiply(new BigDecimal("100")).setScale(0);
//			if (money.compareTo(totalPrice) == 0) {
//				logger.info("支付成功");
//				returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf(money.intValue()));
//
//				payState = PaymentConstant.PAYMENT_STATE_PAYED;
//			} else {
//				logger.info("支付失败，返回金额不一致");
//			}
//		} else { // 未支付
//			logger.info("CodaPay返回未支付");
//		}
//
//		returned.put(PaymentConstant.PAYMENT_STATE, payState);
//
//		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
//		returned.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);
//
//		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
//		return returned;
//	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		// 1.拼装地址
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		BluepayConfig config = getConfig( platform);
		String currencyStr = paymentOrder.getMoneyCurrency();
		logger.info("paymentOrder currency:{}", currencyStr);
		if (StringUtils.isBlank(currencyStr)) {
			currencyStr = paymentOrder.getCurrency();
			logger.info("paymentOrder currency:{}", currencyStr);
		}

  

		String oppositeOrderNo = paymentOrder.getOtherOrderNo();

 
		//http://test.api.bluepay.tech/thaiCharge/service/queryTrans?operatorId=9&productid=1&t_id=20161223truemoney&encrypt=b198435f20fd032dab782b7a4d341678


 		PaymentResult result = null;

		logger.info("codapay 支付验证返回，resultCode:" + result.getResultCode() + ",retmsg:" + result.getResultDesc());

		Map<String, Object> outParams = new HashMap<String, Object>();
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		if (0 == result.getResultCode()) { // 支付不成功
			payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
			BigDecimal totalPrice = new BigDecimal(ObjectUtils.toString(result.getTotalPrice())).setScale(0);
			BigDecimal money = new BigDecimal(paymentOrder.getMoney()).multiply(new BigDecimal("100")).setScale(0);
			if (money.compareTo(totalPrice) == 0) {
				logger.info("支付成功");
				outParams.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf(money.intValue()));

				payState = PaymentConstant.PAYMENT_STATE_PAYED;
			} else {
				logger.info("支付失败，返回金额不一致");
			}
			// 设置充值类型 - 不传则默认1-网银支付

		} else {
			logger.info("支付失败");
		}
		outParams.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo); // 对方订单号
		outParams.put(PaymentConstant.PAYMENT_STATE, payState);
		outParams.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());

		return outParams;

	}




}