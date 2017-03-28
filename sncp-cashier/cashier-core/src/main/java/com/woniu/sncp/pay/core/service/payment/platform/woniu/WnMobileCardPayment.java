package com.woniu.sncp.pay.core.service.payment.platform.woniu;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.http.PayCheckUtils;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;

/**
 * <p>
 * descrption: 蜗牛移动充值卡支付
 * </p>
 * 
 * @author fuzl
 * @date 2016年8月17日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@Service("wnMobileCardPayment")
public class WnMobileCardPayment extends AbstractPayment {

	private final static String PAY_MODE = "V";
	
	@Value("${pay.platform.id}")
	private String PAY_PLATFORM_ID;
	
	@Value("${pay.platform.pwd}")
	private String PAY_PLATFORM_PWD;
	
	@Value("${pay.platform.authkey}")
	private String PAY_PLATFORM_AUTHKEY;
	
	@Value("${deduct.accessId}")
	private String ACCESS_ID;
	
	@Value("${deduct.accessType}")
	private String ACCESS_TYPE;
	
	@Value("${deduct.accessPassword}")
	private String ACCESS_PWD;
	
	@Value("${deduct.key}")
	private String ACCESS_KEY;
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.woniu.pay.core.payment.platform.Payment#orderedParams(java.util.Map)
	 */
	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		// 1. 获取参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		// 2. 构建请求蜗牛移动一卡通支付
		Map<String, Object> dataInfoMap = new HashMap<String, Object>();
		dataInfoMap.put("accessId", ACCESS_ID);
		dataInfoMap.put("aid", (0==paymentOrder.getAid()||null==paymentOrder.getAid()?"-1":paymentOrder.getAid()));//通行证ID，当平台ID payPlatFormId>100时不验证通行证，表示用户不再我方
		dataInfoMap.put("cardPwd", ObjectUtils.toString(inParams.get("cardPwd")));/*卡密明文*/
		dataInfoMap.put("payPlatFormId", PAY_PLATFORM_ID);/*平台ID，计费分配*/
		dataInfoMap.put("clientIp", ObjectUtils.toString(inParams.get(PaymentConstant.CLIENT_IP)));/*充值人IP*/
		dataInfoMap.put("payMode", StringUtils.isBlank(paymentOrder.getImprestMode())?PAY_MODE:paymentOrder.getImprestMode());/*充值默认，代理直扣固定S，全能卡入口使用V*/
		dataInfoMap.put("imprestSource", "");/*扩展数据，用户记录用*/
		dataInfoMap.put("cardMoney", new BigDecimal(paymentOrder.getMoney().toString()).intValue());/*金额，元，不支持小数*/
		dataInfoMap.put("callbackurl", platform.getBehindUrl(paymentOrder.getMerchantId()));/*回调地址,后台异步通知用*/
		dataInfoMap.put("payPlatFormPwd", PAY_PLATFORM_PWD);/*平台密码，计费分配*/
		dataInfoMap.put("payPlatFormOrderNo", paymentOrder.getOrderNo());/*代理商直扣请求订单，代理商需唯一可寻*/
		//buff.append(payPlatFormId).append(payPlatFormPwd).append(dataInfos.containsKey("cardNum")?cardNo:"").append(cardPwd).append(uuid).append(clientIp)
		//.append(payMode).append(imprestSource).append(otherNo).append(platform.getAuthKey());
		String payPlatFormSign = PAY_PLATFORM_ID + PAY_PLATFORM_PWD + ObjectUtils.toString(inParams.get("cardPwd")) + (0==paymentOrder.getAid()||null==paymentOrder.getAid()?"-1":paymentOrder.getAid()) 
				+ ObjectUtils.toString(inParams.get(PaymentConstant.CLIENT_IP)) 
				+ (StringUtils.isBlank(paymentOrder.getImprestMode()) ? PAY_MODE : paymentOrder.getImprestMode()) + "" + new BigDecimal(paymentOrder.getMoney().toString()).intValue() + paymentOrder.getOrderNo() + PAY_PLATFORM_AUTHKEY;
		Map<String, Object> encryptParams = new HashMap<String, Object>();
		encryptParams.put("source", payPlatFormSign.toString());
		dataInfoMap.put("payPlatFormSign", this.encode(encryptParams));/*upper(md5(payPlatFormId+payPlatFormPwd+cardPwd+aid+clientIp+payMode+imprestSource+otherNo+由计费提供的平台校验KEY))*/
		
		
		String dataInfo = JsonUtils.toJson(dataInfoMap);
		Map<String, Object> securityInfoMap = new HashMap<String, Object>();
		securityInfoMap.put("accessId", ACCESS_ID);
		securityInfoMap.put("accessPasswd", ACCESS_PWD);
		securityInfoMap.put("accessType", ACCESS_TYPE);
		securityInfoMap.put("returnType", "json");
		String source = ACCESS_ID + ACCESS_PWD + ACCESS_TYPE + dataInfo + "json" + ACCESS_KEY;

		encryptParams.clear();
		encryptParams.put("source", source.toString());
		String sign = this.encode(encryptParams);
		securityInfoMap.put("verifyStr", sign);
		String securityInfo = JsonUtils.toJson(securityInfoMap);

		//2.请求扣卡
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(platform.getPayUrl());
		List<NameValuePair> reqParams = new ArrayList<NameValuePair>();
		reqParams.add(new BasicNameValuePair("securityInfo", securityInfo));
		reqParams.add(new BasicNameValuePair("dataInfo", dataInfo));
		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(reqParams, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		httpPost.setEntity(entity);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		try {
			responseBody = httpclient.execute(httpPost, responseHandler);
			logger.info("请求蜗牛移动充值卡支付，返回：" + responseBody);
			//如果返回msgcode=1 表示请求成功
		} catch (Exception e) {
			throw new ValidationException("蜗牛移动充值卡请求订单返回失败", e);
		} finally{
			httpclient.getConnectionManager().shutdown();
		}
		Map<String, Object> ret = JsonUtils.jsonToMap(responseBody);
		ret.put("ProductName", StringUtils.trim((String) inParams.get("productName")));
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.woniu.pay.core.payment.platform.Payment#validateBackParams(javax.
	 * servlet.http.HttpServletRequest, com.woniu.pay.pojo.Platform)
	 */
	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException, DataAccessException,
			PaymentRedirectException {
		
		String wnOrderNo = request.getParameter("wnorderno");//支付方订单号
		String orderNo = request.getParameter("orderNo");//我方订单号
		String paystate = request.getParameter("paystate");//状态
		String money = request.getParameter("money");//订单号金额
		String sign = request.getParameter("verify");//签名串
		
		StringBuffer sb = new StringBuffer();
		Map<String, Object> encryptParams = new HashMap<String, Object>();
		sb.append(orderNo).append(money).append(paystate).append(wnOrderNo).append(platform.getBackendKey());
		encryptParams.put("source", sb.toString());
		String localSign = encode(encryptParams);// 数字签名 (32位的md5加密,加密后转换成大写)
		// 校验参数
		Map<String, Object> returned = new HashMap<String, Object>();
		if (!sign.equals(localSign)) {
			logger.info("==============蜗牛移动充值卡支付后台加密处理失败=================");
			logger.info("我方加密串：" + sign);
			logger.info("对方加密串：" + localSign);
			logger.info("==============蜗牛移动充值卡支付后台加密处理结束=================\n");
			throw new ValidationException("蜗牛移动充值卡支付平台加密校验失败");
		}
		
		if ("1".equals(paystate)) { // 支付成功
			logger.info("蜗牛移动充值卡返回支付成功");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else { // 支付失败
			logger.info("蜗牛移动充值卡返回支付失败，订单号： "+orderNo+",paystate：" +  paystate);
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_FAILED);
		}
		
		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "支付订单查询为空,orderNo:" + orderNo);
		
		returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, wnOrderNo);//支付方订单交易号
		returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf(new BigDecimal(money).multiply(new BigDecimal(100)).intValue()));
		//不验证paymentMode,直接取订单中paymentMode
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		return returned;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.woniu.pay.core.payment.platform.Payment#checkOrderIsPayed(java.util
	 * .Map)
	 * 查询验证
	 */
	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		// 1.请求查询扣卡结果
		Map<String, Object> outParams = new HashMap<String, Object>();
		// 封装请求数据
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("version", "1.0.0");	
		params.put("charset", _charset_encode);
		params.put("signMethod", "MD5");
		params.put("merId", platform.getMerchantNo());//商户代码
		params.put("orderNumber", paymentOrder.getOrderNo());
		params.put("merReserved", "");
		
		params.put("priKey", platform.getPayKey());
		params.put("signature", this.encode(params));
		
		// 查询交易
		String response = PayCheckUtils.postRequst(platform.getPayCheckUrl(), params, 5000, _charset_encode, "银联在线支付订单查询接口");
		
		
		// 解析数据
		if(StringUtils.isEmpty(response)){
			logger.info("消费交易应答返回为空，请求参数:{}",params);
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return outParams;
		}
		
		Map<String, Object> respMap = new HashMap<String, Object>();
		
		// 验证状态
		respMap.put("priKey", platform.getPayKey());
		String woniuSign = this.encode(respMap);
		String uniPaySign = String.valueOf(respMap.get("signature"));
		if(!woniuSign.equalsIgnoreCase(uniPaySign)){
			logger.info("消费交易应答签名错误 orderNo:{},woniuSign:{},unionPaySign:{},resp:{}",
					new Object[]{paymentOrder.getOrderNo(),woniuSign,uniPaySign,response});
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return outParams;
		}
		
		String queryResult = String.valueOf(respMap.get("queryResult"));
		String respCode = String.valueOf(respMap.get("respCode"));
		
		if("0".equals(queryResult) && "00".equals(respCode)){
			String oppositeOrderNo = String.valueOf(respMap.get("qid"));
			String orderAmount = String.valueOf(respMap.get("settleAmount"));
			outParams.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo); // 对方订单号
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
			outParams.put(PaymentConstant.OPPOSITE_MONEY, orderAmount); // 总金额，对方传回的单位已经是分
		} else {
			logger.info("消费交易应答未支付 返回:{}", response);
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}
		
		// 2.响应扣卡结果
		return outParams;
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.woniu.pay.core.payment.platform.Payment#encode(java.util.Map)
	 */
	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		String encrypted = "";
		try {
			encrypted = MD5Encrypt.encrypt((String) inParams.get("source"),
					"utf-8", true);
		} catch (RuntimeException e) {
			logger.error("蜗牛移动充值卡支付加密异常", e);
			throw new ValidationException("蜗牛移动充值卡支付加密异常", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("=========蜗牛移动充值卡支付加密开始=========");
			logger.info("source：" + inParams.get("source"));
			logger.info("encrypted：" + encrypted.toUpperCase());
			logger.info("=========蜗牛移动充值卡支付加密结束=========\n");
		}
		return encrypted;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.woniu.pay.core.payment.platform.Payment#paymentReturn(java.util.Map,
	 * javax.servlet.http.HttpServletResponse, boolean)
	 */
	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		JSONObject result = new JSONObject();
		if (isImprestedSuccess){
			result.put(ErrorCode.TIP_CODE, "1");
		}else{
			result.put(ErrorCode.TIP_CODE, "-1");
		}
		logger.info("返回给支付平台,result:"+result);
		super.responseAndWrite(response, ObjectUtils.toString(result));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.woniu.pay.core.payment.platform.Payment#getOrderNoFromRequest(javax
	 * .servlet.http.HttpServletRequest)
	 */
	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("orderId");
	}

}
