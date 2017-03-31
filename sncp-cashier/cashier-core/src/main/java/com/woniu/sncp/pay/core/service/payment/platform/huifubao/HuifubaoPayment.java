package com.woniu.sncp.pay.core.service.payment.platform.huifubao;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.date.DateUtils;
import com.woniu.sncp.pay.common.utils.encrypt.ThreeDES;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;

@Service("huihubaoPayment")
public class HuifubaoPayment extends AbstractPayment {
	
	private final static String MIDDLECHAR = "&";

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		Map<String, Object> mapResult = new HashMap<String, Object>();
		Map<String, Object> linkedParams = new LinkedHashMap<String, Object>();
		linkedParams.put("agent_id", platform.getMerchantNo());
		linkedParams.put("bill_id", paymentOrder.getOrderNo());
		linkedParams.put("bill_time",DateUtils.format(paymentOrder.getCreate(), "yyyyMMddHHmmss"));
		
		//卡号，密码
		String cardInfo = String.valueOf(inParams.get("cardNo"))+"="+String.valueOf(inParams.get("cardPwd"));
		String cardInfoDes = new String(Base64.encodeBase64(ThreeDES.encryptMode(platform.getPayKey().getBytes(),
				cardInfo.getBytes())));
		linkedParams.put("card_data", cardInfoDes);
		int paypoint = (new BigDecimal(paymentOrder.getMoney().toString())).multiply(new BigDecimal(100)).intValue();
		linkedParams.put("pay_jpoint", paypoint);
		String time_stamp = DateUtils.format(new Date(), "yyyyMMddHHmmss");
		linkedParams.put("time_stamp", time_stamp);
		
		// 1.参数加密
		//md5String=MD5(agent_id + "&" + bill_id + "&" +...+"|||" + privateKey)
		String md5SourceString = getMd5Source(linkedParams,platform.getBackendKey());
		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", md5SourceString);
		String encrypted = this.encode(encodeParams);
		
		// 2.向汇付宝平台发送请求
		HttpClient httpclient = new DefaultHttpClient();
		//httpclient = WebClientDevWrapper.wrapClient(httpclient);//支持https
		HttpPost httpPost = new HttpPost(platform.getPayUrl());
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("agent_id", platform.getMerchantNo()));
		params.add(new BasicNameValuePair("bill_id", paymentOrder.getOrderNo()));
		params.add(new BasicNameValuePair("bill_time", DateUtils.format(paymentOrder.getCreate(), "yyyyMMddHHmmss")));
		params.add(new BasicNameValuePair("card_data",cardInfoDes));
		params.add(new BasicNameValuePair("pay_jpoint", ObjectUtils.toString(paypoint)));
		params.add(new BasicNameValuePair("client_ip",ObjectUtils.toString(inParams.get(PaymentConstant.CLIENT_IP))));
		params.add(new BasicNameValuePair("notify_url", platform.getBehindUrl(paymentOrder.getMerchantId())));
		params.add(new BasicNameValuePair("time_stamp", time_stamp));
		params.add(new BasicNameValuePair("sign", encrypted));
		
		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(params, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		httpPost.setEntity(entity);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		try {
			responseBody = httpclient.execute(httpPost, responseHandler);
			
			if (StringUtils.isBlank(responseBody)) {
				throw new ValidationException("汇付宝支付请求返回responseBody为空");
			}

			if (logger.isInfoEnabled()) {
				logger.info("汇付宝支付请求返回结果：" + responseBody);
			}
			//ret_code=4&agent_id=1872446&bill_id=20141023-227-007-0000366318&jnet_bill_no=&bill_status=&card_real_amt=&card_settle_amt=&card_detail_data=&ret_msg=签名验证错误&ext_param=0&sign=9c48ea0f3c357ecc9ca3085f691e728f
			Map<String, Object> map = new LinkedHashMap<String, Object>();
			String[] retArray = responseBody.split("&");
			for(String str : retArray){
				String[] ky = str.split("=");
				if(ky.length ==2){
					map.put(ky[0], ky[1]);
				}
			}
			//Map<String, Object> map = JsonUtils.jsonToMap(responseBody);
			if (map != null) {
				String ret_code = String.valueOf(map.get("ret_code"));
				//String bill_status = String.valueOf(map.get("bill_status"));
				String ret_msg = String.valueOf(map.get("ret_msg"));
				mapResult.put("msgcode", ret_code);
				//mapResult.put("bill_status", bill_status);
				mapResult.put("message", ret_msg);
			}
				
		} catch (Exception e) {
			logger.error("汇付宝请求异常：" + e.getMessage());
			throw new ValidationException("汇付宝请求订单返回失败", e);
		} finally{
			httpclient.getConnectionManager().shutdown();
		}

		
		return mapResult;
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException, DataAccessException,
			PaymentRedirectException {
		// 得到汇付宝支付返回的数据
		String agent_id = StringUtils.trim(request.getParameter("agent_id"));
		String bill_id = StringUtils.trim(request.getParameter("bill_id"));
		String jnet_bill_no = StringUtils.trim(request.getParameter("jnet_bill_no"));//对方订单号
		String bill_status = StringUtils.trim(request.getParameter("bill_status"));//单据状态：0=未知；1=成功；-1=失败
		String real_amt = StringUtils.trim(request.getParameter("real_amt"));//实际支付金额（单位：元）
		String ext_param = StringUtils.trim(request.getParameter("ext_param"));
		String sign = StringUtils.trim(request.getParameter("sign"));

		// 1.我方加密数据
		LinkedHashMap<String, Object> encryParams = new LinkedHashMap<String, Object>();
		encryParams.put("agent_id", agent_id);
		encryParams.put("bill_id", bill_id);
		encryParams.put("jnet_bill_no", jnet_bill_no);
		encryParams.put("bill_status", bill_status);
		encryParams.put("real_amt", real_amt);
		String source = getMd5Source(encryParams,platform.getBackendKey());
		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", source);
		String encrypted = this.encode(encodeParams);

		// 2.比较是否相等
		if (!encrypted.equalsIgnoreCase(sign)) {
			if (logger.isInfoEnabled()) {
				logger.info("==============汇付宝后台加密处理失败=================");
				logger.info("我方加密串：" + encrypted);
				logger.info("对方加密串：" + sign);
				logger.info("==============汇付宝后台加密处理结束=================\n");
			}
			throw new ValidationException("支付平台加密校验失败");
		}

		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(bill_id);
		Assert.notNull(paymentOrder, "支付订单查询为空,orderId:" + bill_id);

		Map<String, Object> returned = new HashMap<String, Object>();
		if ("1".equals(bill_status)) { // 支付成功
			logger.info("汇付宝返回支付成功");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else { // 支付失败
			logger.error("汇付宝返回支付失败,bill_status：" +bill_status);
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_FAILED);
		}
		
		// 支付模式-从privateField中判断是什么，网银支付传1
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.ORDER_NO, bill_id);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, jnet_bill_no);
		// 金额单位：分
		returned.put(PaymentConstant.OPPOSITE_MONEY, ObjectUtils.toString((new BigDecimal(real_amt)).multiply(new BigDecimal(100)).intValue()));
		
		return returned;
	}

	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		String merId = platform.getMerchantNo();
		String orderId = paymentOrder.getOrderNo();

		// 1.加密
		LinkedHashMap<String, Object> encryParams = new LinkedHashMap<String, Object>();
		encryParams.put("agent_id", merId);
		encryParams.put("bill_id", orderId);
		String time_stamp = DateUtils.format(new Date(), "yyyyMMddHHmmss");
		encryParams.put("time_stamp", time_stamp);

		String source = getMd5Source(encryParams,platform.getBackendKey());
		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", source);
		String encrypted = this.encode(encodeParams);

		// 2.向汇付宝请求参数
		HttpClient httpclient = new DefaultHttpClient();
		//httpclient = WebClientDevWrapper.wrapClient(httpclient);//支持https
		HttpPost httpPost = new HttpPost(platform.getPayCheckUrl());
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("agent_id", merId));
		params.add(new BasicNameValuePair("bill_id", orderId));
		params.add(new BasicNameValuePair("time_stamp", time_stamp));
		params.add(new BasicNameValuePair("sign", encrypted.toLowerCase()));
		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(params, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		httpPost.setEntity(entity);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		try {
			responseBody = httpclient.execute(httpPost, responseHandler);
		} catch (Exception e) {
			logger.error("汇付宝订单校验异常：" + e.getMessage());
			throw new ValidationException("汇付宝订单校验返回失败", e);
		} finally{
			httpclient.getConnectionManager().shutdown();
		}
		

		Map<String, Object> outParams = new HashMap<String, Object>();

		if (StringUtils.isBlank(responseBody)) {
			throw new ValidationException("汇付宝订单验证返回responseBody为空");
		}

		// 3.请求返回
		if (logger.isInfoEnabled()) {
			logger.info("汇付宝订单校验返回结果：" + responseBody);
		}
		
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		String[] retArray = responseBody.split("&");
		for(String str : retArray){
			String[] ky = str.split("=");
			if(ky.length ==2){
				map.put(ky[0], ky[1]);
			}
		}
		//Map<String, Object> map = JsonUtils.jsonToMap(responseBody);

		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		if (map != null) {
			
			String ret_code = String.valueOf(map.get("ret_code"));
			String ret_msg = String.valueOf(map.get("ret_msg"));
			
			if ("0".equals(ret_code)) {
				payState = PaymentConstant.PAYMENT_STATE_PAYED;// 支付成功
				outParams.put(PaymentConstant.OPPOSITE_ORDERNO, String.valueOf(map.get("jnet_bill_no")));// 对方订单号
				Integer payMy = new BigDecimal(String.valueOf(map.get("real_jpoint"))).multiply(new BigDecimal(1)).intValue();
				outParams.put(PaymentConstant.OPPOSITE_MONEY, payMy);// 订单金额,分
			} else {
				logger.info("汇付宝订单查询返回,ret_code:[" + ret_code +"],ret_msg:[" +ret_msg+"]");
				throw new ValidationException("汇付宝订单验证返回:["+ret_code+","+ret_msg+"]");
			} 
		}

		outParams.put(PaymentConstant.PAYMENT_STATE, payState);
		return outParams;
	}

	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		String encrypted = "";
		try {
			encrypted = MD5Encrypt.encrypt((String) inParams.get("source"), "utf-8").toLowerCase();
		} catch (RuntimeException e) {
			logger.error("汇付宝支付加密异常", e);
			throw new ValidationException("汇付宝支付加密异常", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("=========汇付宝支付加密开始=========");
			logger.info("source：" + inParams.get("source"));
			logger.info("encrypted：" + encrypted);
			logger.info("=========汇付宝支付加密结束=========\n");
		}
		return encrypted;
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess)
			super.responseAndWrite(response, "ok");
		else
			super.responseAndWrite(response, "huifubao pay fail.");

	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("bill_id");
	}
	
	private String getMd5Source(Map<String, Object> map,String md5Key) {
		StringBuffer sb = new StringBuffer();
		for (Iterator<Entry<String, Object>> keyValue = map.entrySet().iterator(); keyValue.hasNext();) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) keyValue.next();
			String value = ObjectUtils.toString(entry.getValue());
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(value);
			sb.append(MIDDLECHAR);
		}
		String source = sb.substring(0,sb.length()-1);
		source = source + "|||"+md5Key;
		return source;
	}
	
	/**
     * 验证骏网卡是否正确
     * @param cardNo
     * @param cardPassword
     * @return
     */
    public static boolean isValidCard(String cardNo,String cardPassword){
    	int cardNoLen = cardNo.length();
    	int cardPwdLen = cardPassword.length();
    	
    	if(cardNoLen == 16 && cardPwdLen == 16){
			return true;
		}
    	return false;
    }

}
