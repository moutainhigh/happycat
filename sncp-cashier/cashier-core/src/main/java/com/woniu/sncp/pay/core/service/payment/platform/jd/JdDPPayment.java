/**
 * <p>Copyright (c) Snail Game 2014</p>
 */
package com.woniu.sncp.pay.core.service.payment.platform.jd;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.lang.StringUtil;
import com.woniu.sncp.net.NetServiceException;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.http.PayCheckUtils;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;


/**
 * 
 *
 */
@Service("jdDPPayment")
public class JdDPPayment extends AbstractPayment {
	
	@Resource
	private PaymentConstant paymentConstant;
	
	private static final String CHARSET	= "UTF-8";
	
	/**
	 * 支付成功状态
	 */
	private static final String PAY_SUCCESS = "20";
	
	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		String data = inParams.get("data").toString();
		String key = inParams.get("key").toString();
		String encodeData = MD5Encrypt.encrypt(data+key);
		logger.info("原始数据data:"+data+"加密后数据encodeData.toUpper:"+encodeData.toUpperCase());
		
		return encodeData.toUpperCase();
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		String defaultbank = inParams.get("defaultbank").toString();		//支付银行名称
		String cardType = inParams.get("cardtype").toString();
		
		String key = platform.getPayKey();							//key
		String merchantId = platform.getMerchantNo();				//商户编号
		String orderNo = paymentOrder.getOrderNo();							//订单号
		String totalMoney = paymentOrder.getMoney().toString();				//订单总金额
		totalMoney = new BigDecimal(totalMoney).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString();
		
		String moneyType;													//币种
		if(StringUtil.equalsIgnoreCase(paymentOrder.getMoneyCurrency(), "R")){
			moneyType = "CNY";
		}else{
			moneyType = "CNY";
		}
		String myUrl = platform.getFrontUrl(paymentOrder.getMerchantId());						
		String payUrl = platform.getPayUrl();					
		
		Map<String,Object> dataMap = new HashMap<String, Object>();
		dataMap.put("data", totalMoney + moneyType + orderNo + merchantId + myUrl);
		dataMap.put("key", key);
		String md5info = encode(dataMap);
		logger.info("请求京东网银充值MD5加密串:"+md5info);
		
		Map<String, Object> bankMap = paymentConstant.getJdCyberBankMap();
		String bankCode = String.valueOf(bankMap.get(defaultbank+"_"+cardType));				//支付银行名称对应的银行编码
		
		Map<String,Object> returnedMap = new HashMap<String, Object>();
		returnedMap.put("v_mid", merchantId);										//商户编号merchantId
		returnedMap.put("v_oid", orderNo);											//订单编号
		returnedMap.put("v_amount", totalMoney);									//订单总金额
		returnedMap.put("v_moneytype", moneyType);									//币种
		returnedMap.put("v_url", myUrl);											//消费者完成购物后页面返回的我方页面
		returnedMap.put("v_md5info", md5info);										//MD5校验码
		returnedMap.put("pmode_id", bankCode);										//银行编码
		returnedMap.put("remark2", "[url:="+platform.getBehindUrl(paymentOrder.getMerchantId())+"]");	//异步通知地址
		returnedMap.put("payUrl", payUrl);											//提交订单至对方URL
		returnedMap.put("acceptCharset", CHARSET);									//字符集
		logger.info("京东网银充值请求参数:v_mid"+merchantId+",v_oid:"+orderNo+",v_amount"+totalMoney+","
				+ "v_moneytype:"+moneyType+",v_url:"+myUrl+",v_md5info:"+md5info+","
				+ "pmode_id:"+bankCode+",remark2:"+"[url:="+platform.getBehindUrl(paymentOrder.getMerchantId())+"]"+",payUrl:"+payUrl);
		
		return returnedMap;
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException,
			DataAccessException, PaymentRedirectException {
		String orderNo = null;
		String orderNoStatus = null;
		String payInfo = null;
		String payBank = null;
		String md5Str = null;
		String actualMoney = null;
		String moneyType = null;
		String remark1 = null;
		String remark2 = null;
		try {
			orderNo = new String( new String(request.getParameter("v_oid").getBytes("GBK"),"ISO-8859-1").getBytes("ISO-8859-1") ,"UTF-8");
			orderNoStatus = new String( new String(request.getParameter("v_pstatus").getBytes("GBK"),"ISO-8859-1").getBytes("ISO-8859-1") ,"UTF-8");
			payInfo = new String( new String(request.getParameter("v_pstring").getBytes("GBK"),"ISO-8859-1").getBytes("ISO-8859-1") ,"UTF-8");		//支付结果信息
			payBank = new String( new String(request.getParameter("v_pmode").getBytes("GBK"),"ISO-8859-1").getBytes("ISO-8859-1") ,"UTF-8");		//支付银行
			md5Str = new String( new String(request.getParameter("v_md5str").getBytes("GBK"),"ISO-8859-1").getBytes("ISO-8859-1") ,"UTF-8");
			actualMoney = new String( new String(request.getParameter("v_amount").getBytes("GBK"),"ISO-8859-1").getBytes("ISO-8859-1") ,"UTF-8");
			moneyType = new String( new String(request.getParameter("v_moneytype").getBytes("GBK"),"ISO-8859-1").getBytes("ISO-8859-1") ,"UTF-8");
			remark1 = new String( new String(request.getParameter("remark1").getBytes("GBK"),"ISO-8859-1").getBytes("ISO-8859-1") ,"UTF-8");
			remark2 = new String( new String(request.getParameter("remark2").getBytes("GBK"),"ISO-8859-1").getBytes("ISO-8859-1") ,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.info("京东网银支付平台转码校验失败,orderNo:"+orderNo);
			throw new ValidationException("京东网银支付平台转码校验失败");
		}
		logger.info("orderNo:"+orderNo+",orderNoStatus:"+orderNoStatus+",payInfo:"+payInfo+""
				+ ",payBank:"+payBank+",md5Str:"+md5Str+",actualMoney:"+actualMoney+",moneyType:"+moneyType+""
				+ ",remark1:"+remark1+",remark2:"+remark2);
		
		//valid md5 encode
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("data", orderNo + orderNoStatus + actualMoney + moneyType);
		dataMap.put("key", platform.getPayKey());
		String localSign = encode(dataMap);
		if( !StringUtil.equalsIgnoreCase(localSign, md5Str) ){
			if (logger.isInfoEnabled()) {
				logger.info("==============京东网银后台回调加密处理失败=================");
				logger.info("我方加密串：" + localSign + ",对方加密串：" + md5Str);
				logger.info("==============京东网银后台回调加密处理结束=================\n");
			}
			throw new ValidationException("京东网银支付平台加密校验失败");
		}
		
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "京东网银支付平台支付订单查询为空,orderNo:" + orderNo);
		
		Map<String, Object> returned = new HashMap<String, Object>();
		if( StringUtil.equals(orderNoStatus, PAY_SUCCESS) ){
			logger.info("京东网银支付返回支付成功，订单号："+orderNo);
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		}else{
			logger.info("京东网银支付返回支付失败，订单号："+orderNo);
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.ORDER_NO, orderNo);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, orderNo);
		returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(actualMoney)).multiply(new BigDecimal(100)).intValue()));
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		
		return returned;
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams, HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess)
			super.responseAndWrite(response, "ok");
		else
			super.responseAndWrite(response, "error");
	}

	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		String orderNo = paymentOrder.getOrderNo();							//订单号
		String merchantId = platform.getMerchantNo();				        //商户号
		String requestUrl = platform.getPayCheckUrl();						//接收返回的url地址
		String backendUrl = platform.getBehindUrl(paymentOrder.getMerchantId());
		String key = platform.getPayKey();							//key
		
		Map<String,Object> dataMap = new HashMap<String, Object>();
		dataMap.put("data", orderNo );
		dataMap.put("key", key);
		String md5info = encode(dataMap);
		logger.info("京东网银,订单验证MD5:"+md5info);
		
		httpPost(requestUrl,orderNo, merchantId, backendUrl,  md5info);
		
		String totalFee = paymentOrder.getMoney().toString();
		logger.info("京东网银验证订单接口数据返回,orderNo:"+orderNo+",订单状态:"+paymentOrder.getPaymentState()+",金额:"+totalFee);
		
		Map<String, Object> outParams = new HashMap<String, Object>();
		outParams.put(PaymentConstant.OPPOSITE_ORDERNO, orderNo); // 对方订单号
		outParams.put(PaymentConstant.ORDER_NO, orderNo ); // 对方订单号
		outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED); // 支付状态
		outParams.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(totalFee)).multiply(new BigDecimal(100)).intValue()));
		
		return outParams;
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("v_oid");
	}
	
	/**
	 * 到京东网银验证订单信息
	 * @param orderNo
	 * @param merchantId
	 * @param url
	 * @param md5info
	 * @param sendURL
	 * @return
	 */
	private String httpPost(String requestUrl,String orderNo,String merchantId,String backendUrl,String md5info) {
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		paramList.add( new BasicNameValuePair("v_oid", orderNo) );
		paramList.add( new BasicNameValuePair("v_mid", merchantId) );
		paramList.add( new BasicNameValuePair("billNo_md5", md5info) );
		paramList.add( new BasicNameValuePair("v_url", backendUrl) );
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(requestUrl);
		String responseStr;
		
		try {
			if(requestUrl.indexOf("https") >= 0){
				PayCheckUtils.supportHttps(httpClient);
			}
			httpPost.addHeader(HTTP.CONTENT_TYPE, "text/xml;charset=gb2312");
			httpPost.setEntity(new UrlEncodedFormEntity(paramList));
			
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			
			entity = new BufferedHttpEntity(entity);
			responseStr = EntityUtils.toString(entity);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("京东网银支付订单校验接口出错，请与客服联系", e);
		}finally{
			httpClient.getConnectionManager().shutdown();
		}
		
		return responseStr;
	}
	
}
