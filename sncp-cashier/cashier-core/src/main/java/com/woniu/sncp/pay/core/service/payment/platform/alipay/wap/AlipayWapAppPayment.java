package com.woniu.sncp.pay.core.service.payment.platform.alipay.wap;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.common.utils.RefundmentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.cbss.api.imprest.direct.request.DIOrderRefundQueryRequest;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundBackCallData;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundQueryData;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundQueryResponse;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptFactory;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptStringUtils;
import com.woniu.sncp.pay.common.utils.encrypt.Rsa;
import com.woniu.sncp.pay.common.utils.http.PayCheckUtils;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.AlipaySecurityRiskDetect;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.tools.AlipayHelper;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.pojo.refund.PayRefundBatch;

/**
 * 
 * <p>descrption: 新版支付宝wap接口,支持app</p>
 * 
 * @author fuzl
 * @date   2016年9月7日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@Service("alipayWapAppPayment")
public class AlipayWapAppPayment extends AbstractPayment {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	/**
	 * 支付宝支付编码
	 */
	protected final static String _charset_encode = "utf-8";
	
	protected final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	@Resource
	private PaymentConstant paymentConstant;
	
	@Resource
	private AlipaySecurityRiskDetect alipaySecurityRiskDetect;
	
	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		String source = (String) inParams.get("source");
		String priKey = (String) inParams.get("priKey");
		String sign = null;
		try {
			sign = EncryptFactory.getInstance(Rsa.NAME).sign(source, priKey,_charset_encode);
		} catch (Exception e) {
			throw new ValidationException("支付宝加密失败", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("支付宝加密源：" + source);
			logger.info("支付宝加密结果：" + sign);
		}
		return sign;
	}
	
	private Map<String, Object> putToTreeMap(HttpServletRequest request,
			Map<String, Object> requestParams) {
		Map<String, Object> treeMap = new TreeMap<String, Object>();
		for (Iterator<Entry<String, Object>> keyValuePairs = requestParams
				.entrySet().iterator(); keyValuePairs.hasNext();) {
			Map.Entry<String, Object> entry = keyValuePairs.next();
			String key = entry.getKey();
			String value = request.getParameter(key);
			
			treeMap.put(key, value);
		}
		return treeMap;
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		try {
			// 1.拼装参数
			PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
			Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

			String ext = ObjectUtils.toString(platform.getExtend());
			String appId = "";
			String aesKey = "";
			if(StringUtils.isNotBlank(ext)){
				JSONObject extend = JSONObject.parseObject(ext);
				appId = extend.getString("appId");
				aesKey = extend.getString("aesKey");
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
			
			Map<String, Object> reqParams = new HashMap<String, Object>();
			Map<String, Object> bodyParams = new HashMap<String, Object>();
			bodyParams.put("subject", StringUtils.trim((String) inParams.get("productName")));//商品的标题/交易标题/订单标题/订单关键字等。
			bodyParams.put("body", StringUtils.trim((String) inParams.get("productName")));
			bodyParams.put("out_trade_no", paymentOrder.getOrderNo());//商户网站唯一订单号
			if(StringUtils.isNotBlank(ObjectUtils.toString(platform.getTransTimeout()))){
				bodyParams.put("timeout_express", platform.getTransTimeout()+"m");//该笔订单允许的最晚付款时间，逾期将关闭交易。取值范围：1m～15d。m-分钟，h-小时，d-天，1c-当天（1c-当天的情况下，无论交易何时创建，都在0点关闭）。 该参数数值不接受小数点， 如 1.5h，可转换为 90m。
			}
			bodyParams.put("total_amount", paymentOrder.getMoney());//订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]	
			bodyParams.put("seller_id", platform.getMerchantNo());//收款支付宝用户ID。 如果该值为空，则默认为商户签约账号对应的支付宝用户ID	
			bodyParams.put("product_code", "QUICK_WAP_PAY");//销售产品码，商家和支付宝签约的产品码
			String reqData = AlipayWapAppPayment.createLinkStringWithKeyAndValue(bodyParams,null);
			logger.info("请求参数reqData:"+reqData);
			String bdParams = JsonUtils.toJson(bodyParams).replaceAll("\"","'");
//			String bizContent = this.aesEncrypt(aesKey, reqData);
			
			reqParams.put("app_id", appId);//支付宝分配给开发者的应用ID
			reqParams.put("method", "alipay.trade.wap.pay");//接口名称
			reqParams.put("charset", _charset_encode);//请求使用的编码格式，如utf-8,gbk,gb2312等
			reqParams.put("sign_type", "RSA");
			reqParams.put("timestamp", sdf.format(paymentOrder.getCreate()));//发送请求的时间，格式"yyyy-MM-dd HH:mm:ss"
			reqParams.put("version", "1.0");
			reqParams.put("return_url", platform.getFrontUrl(paymentOrder.getMerchantId()));
			reqParams.put("notify_url", platform.getBehindUrl(paymentOrder.getMerchantId()));
			reqParams.put("biz_content", ObjectUtils.toString(bdParams));//业务请求参数的集合，最大长度不限，除公共参数外所有请求参数都必须放在这个参数中传递，具体参照各产品快速接入文
//			reqParams.put("biz_content", bizContent);//业务请求参数的集合，最大长度不限，除公共参数外所有请求参数都必须放在这个参数中传递，具体参照各产品快速接入文
			
			// 2.加密
			String priKey = AlipayHelper.readText(platform.getPrivateUrl());
			LinkedHashMap<String, Object> linkedHashMap = AlipayHelper.sortMap(reqParams);
			String source = EncryptStringUtils.linkedHashMapToStringWithKey(linkedHashMap, true);
			Map<String, Object> encryptParams = new HashMap<String, Object>();
			encryptParams.put("source", source);
			encryptParams.put("priKey", priKey);
			String sign = this.encode(encryptParams);
			
			// 3.剩余需要传递参数
			reqParams.put("sign", sign);
			reqParams.put("payUrl", platform.getPayUrl()); // 提交给对方的支付地址
			
			// 4.风险检测
			alipaySecurityRiskDetect.riskDetect(inParams,alipaySecurityRiskDetect.TERMINAL_TYPE_WAP,_charset_encode);
		
			return reqParams;
		} catch (Exception e) {
			logger.error("支付宝请求支付加密请求异常,",e);
		}
		return null;
	}
	
	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException,
			DataAccessException, PaymentRedirectException {
		
		String ext = ObjectUtils.toString(platform.getExtend());
		String appId = "";
		String aesKey = "";
		if(StringUtils.isNotBlank(ext)){
			JSONObject extend = JSONObject.parseObject(ext);
			appId = extend.getString("appId");
			aesKey = extend.getString("aesKey");
		}
		
		//获取参数及解析业务数据
		String body = StringUtils.trim(request.getParameter("body"));
		String buyeId = StringUtils.trim(request.getParameter("buyer_id"));
		String charset = StringUtils.trim(request.getParameter("charset"));
		String notifyId = StringUtils.trim(request.getParameter("notify_id"));//通知校验ID
		String notifyTime = StringUtils.trim(request.getParameter("notify_time"));//通知的发送时间。格式为yyyy-MM-dd HH:mm:ss
		String notifyType = StringUtils.trim(request.getParameter("notify_type"));//通知的类型	trade_status_sync
		String gmtClose = StringUtils.trim(request.getParameter("gmt_close"));
		String gmtCreate = StringUtils.trim(request.getParameter("gmt_create"));
		String gmtPayment = StringUtils.trim(request.getParameter("gmt_payment"));
		String orderNo = StringUtils.trim(request.getParameter("out_trade_no"));//原支付请求的商户订单号
		String refundFee = StringUtils.trim(request.getParameter("refund_fee"));
		String sellerId = StringUtils.trim(request.getParameter("seller_id"));
		String subject = StringUtils.trim(request.getParameter("subject"));
		String paymentMoney = StringUtils.trim(request.getParameter("total_amount"));//本次交易支付的订单金额，单位为人民币（元）
		String oppositeOrderNo = StringUtils.trim(request.getParameter("trade_no"));//支付宝交易凭证号
		String tradeStatus = StringUtils.trim(request.getParameter("trade_status"));
		/*
		 *  WAIT_BUYER_PAY	交易创建，等待买家付款
			TRADE_CLOSED	未付款交易超时关闭，或支付完成后全额退款
			TRADE_SUCCESS	交易支付成功
			TRADE_FINISHED	交易结束，不可退款
		 */
		String version = StringUtils.trim(request.getParameter("version"));
		String _appId = StringUtils.trim(request.getParameter("app_id"));//支付宝分配给开发者的应用Id
		
		String signType = StringUtils.trim(request.getParameter("sign_type"));//签名算法类型，目前支持RSA
		String sign = StringUtils.trim(request.getParameter("sign"));
		
		//加密串校验
		Map<String, Object> params = new HashMap<String, Object>();
		params = putToTreeMap(request, request.getParameterMap());
		logger.info("通知参数params:"+params);
        
		String pubKey = AlipayHelper.readText(platform.getPublicUrl());
		String notifyData = AlipayWapAppPayment.createLinkStringWithKeyAndValue(params,null);
		logger.info("通知参数notifyData:"+notifyData);
		if(!EncryptFactory.getInstance(Rsa.NAME).verify(sign, notifyData, pubKey, "")){
			throw new ValidationException("支付宝异步回调加密串校验异常");
		}
		
		// 订单查询及验证
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "支付订单查询为空,orderNo:" + orderNo);
		
		//判断支付状态
		Map<String, Object> returned = new HashMap<String, Object>();
		if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) { // 支付成功
			logger.info("支付宝返回支付成功");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else { // 未支付
			logger.info("支付宝返回未支付");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}
		
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);
		returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(paymentMoney)).multiply(new BigDecimal(100)).intValue()));
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		
		return returned;
	}
	
	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess)
			super.responseAndWrite(response, "success");
		else
			super.responseAndWrite(response, "fail");
	}
	
	
	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		try {
			// 1.拼装地址
			PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
			Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
			
			String ext = ObjectUtils.toString(platform.getExtend());
			String appId = "";
			String aesKey = "";
			if(StringUtils.isNotBlank(ext)){
				JSONObject extend = JSONObject.parseObject(ext);
				appId = extend.getString("appId");
				aesKey = extend.getString("aesKey");
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
			
			Map<String, Object> reqParams = new HashMap<String, Object>();
			Map<String, Object> bodyParams = new HashMap<String, Object>();
			bodyParams.put("out_trade_no", paymentOrder.getOrderNo());//商户网站唯一订单号
//			bodyParams.put("trade_no", paymentOrder.getPartnerOrderNo());//该笔订单允许的最晚付款时间，逾期将关闭交易。取值范围：1m～15d。m-分钟，h-小时，d-天，1c-当天（1c-当天的情况下，无论交易何时创建，都在0点关闭）。 该参数数值不接受小数点， 如 1.5h，可转换为 90m。	
			
			String reqData = AlipayWapAppPayment.createLinkStringWithKeyAndValue(bodyParams,null);
			logger.info("请求参数reqData:"+reqData);
			String bdParams = JsonUtils.toJson(bodyParams).replaceAll("\"","'");
//			String bizContent = this.aesEncrypt(aesKey, reqData);
			
			reqParams.put("app_id", appId);//支付宝分配给开发者的应用ID
			reqParams.put("method", "alipay.trade.query");//接口名称
			reqParams.put("charset", _charset_encode);//请求使用的编码格式，如utf-8,gbk,gb2312等
			reqParams.put("sign_type", "RSA");
			reqParams.put("timestamp", sdf.format(paymentOrder.getCreate()));//发送请求的时间，格式"yyyy-MM-dd HH:mm:ss"
			reqParams.put("version", "1.0");
			reqParams.put("biz_content", ObjectUtils.toString(bdParams));//业务请求参数的集合，最大长度不限，除公共参数外所有请求参数都必须放在这个参数中传递，具体参照各产品快速接入文
//			reqParams.put("biz_content", bizContent);//业务请求参数的集合，最大长度不限，除公共参数外所有请求参数都必须放在这个参数中传递，具体参照各产品快速接入文
			
			// 2.加密
			String priKey = AlipayHelper.readText(platform.getPrivateUrl());
			LinkedHashMap<String, Object> linkedHashMap = AlipayHelper.sortMap(reqParams);
			String source = EncryptStringUtils.linkedHashMapToStringWithKey(linkedHashMap, true);
			Map<String, Object> encryptParams = new HashMap<String, Object>();
			encryptParams.put("source", source);
			encryptParams.put("priKey", priKey);
			String sign = this.encode(encryptParams);
			
			reqParams.put("sign", sign);
			
			// 1.查询交易
			String response = PayCheckUtils.postRequst(platform.getPayCheckUrl(), reqParams, 500, _charset_encode, "支付宝支付订单查询接口");
			Map<String, Object> outParams = new HashMap<String, Object>();
			if (StringUtils.isBlank(response)) {
				outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
				return outParams;
			}
			// 2.解析数据
			JSONObject respObj = JSONObject.parseObject(response);

			if (logger.isInfoEnabled()){
				logger.info("支付宝订单验证地址(直接访问)：" + platform.getPayCheckUrl());
				logger.info("支付宝订单验证返回responseBody:\n" + respObj);			
			}

			String alipayTradeQueryResponse = respObj.getString("alipay_trade_query_response");
			JSONObject tradeRespObj = JSONObject.parseObject(alipayTradeQueryResponse);

			String tradeStatus = tradeRespObj.getString("trade_status");
			/**
			 * 交易状态：
			 * WAIT_BUYER_PAY（交易创建，等待买家付款）、
			 * TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）、
			 * TRADE_SUCCESS（交易支付成功）、
			 * TRADE_FINISHED（交易结束，不可退款）
			 */
			// 3.结果验证A - is_success 验证，判断此次查询是否成功
			String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
			String total_fee = null;
			String oppositeOrderNo = "";
			if ("WAIT_BUYER_PAY".equals(tradeStatus) || "TRADE_CLOSED".equals(tradeStatus)) {
				throw new ValidationException("订单验证失败：支付宝返回：" + respObj);
			} else if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
				
				oppositeOrderNo = tradeRespObj.getString("trade_no");//支付宝订单号
				payState = PaymentConstant.PAYMENT_STATE_PAYED;
				total_fee = tradeRespObj.getString("total_amount");//交易的订单金额，单位为元，两位小数
			}

			outParams.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo); // 对方订单号
			outParams.put(PaymentConstant.PAYMENT_STATE, payState); // 支付状态
			outParams.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(total_fee)).multiply(new BigDecimal(100)).intValue()));
			return outParams;
		} catch (Exception e) {
			logger.error("支付宝验证订单加密请求异常,",e);
		}
		return null;
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("out_trade_no");
	}

	/** 
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
	public static String createLinkStringWithKeyAndValue(Map<String, Object> params,String[] signKeys) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        String prestr = "";
        for (int i = 0; i < keys.size(); i++) {
        	if(keys.get(i).equals("sign") || keys.get(i).equals("sign_type")){
        		continue;
        	}
            String key = keys.get(i);
            String value = params.get(key).toString();
            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }
        return prestr;
    }
	
	/**
	 * AES加密
	 * @param aesKey
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public String aesEncrypt(String aesKey,String content) throws Exception {
        String charset = _charset_encode;
        String fullAlg = "AES/CBC/PKCS5Padding";
  
        Cipher cipher = Cipher.getInstance(fullAlg);
        IvParameterSpec iv = new IvParameterSpec(initIv(fullAlg));
        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(Base64.decodeBase64(aesKey.getBytes()), "AES"),
                iv);
  
        byte[] encryptBytes = cipher.doFinal(content.getBytes(charset));
        return new String(Base64.encodeBase64(encryptBytes));
    }
   
    /**
     * 初始向量的方法, 全部为0. 这里的写法适合于其它算法,针对AES算法的话,IV值一定是128位的(16字节).
     *
     * @param fullAlg
     * @return
     * @throws GeneralSecurityException
     */
    private static byte[] initIv(String fullAlg) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(fullAlg);
        int blockSize = cipher.getBlockSize();
        byte[] iv = new byte[blockSize];
        for (int i = 0; i < blockSize; ++i) {
            iv[i] = 0;
        }
        return iv;
    }
    
    /**
     * 
     * @param content 密文
     * @param key aes密钥
     * @param charset 字符集
     * @return 原文
     * @throws EncryptException
     */
    public String decrypt(String content, String key, String charset) throws Exception {
        //反序列化AES密钥
        SecretKeySpec keySpec = new SecretKeySpec(Base64.decodeBase64(key.getBytes()), "AES");
        //128bit全零的IV向量
        byte[] iv = new byte[16];
        for (int i = 0; i < iv.length; i++) {
            iv[i] = 0;
        }
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        //初始化加密器并加密
        Cipher deCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        deCipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
        byte[] encryptedBytes = Base64.decodeBase64(content.getBytes());
        byte[] bytes = deCipher.doFinal(encryptedBytes);
        return new String(bytes);
          
    }
    
    /** 
     * 十六进制string转二进制byte[] 
     */  
    public static byte[] hexStringToByte(String s) {     
        byte[] baKeyword = new byte[s.length() / 2];     
        for (int i = 0; i < baKeyword.length; i++) {     
            try {     
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));     
            } catch (Exception e) {     
                System.out.println("十六进制转byte发生错误！！！");     
                e.printStackTrace();     
            }     
        }     
        return baKeyword;     
    }   
    
    /**
	 * 退款参数构造
	 * @param inParams
	 * @return
	 * @throws ValidationException
	 */
	public Map<String, Object> refundedParams(Map<String, Object> inParams) throws ValidationException{
		
		// 1.拼装参数
		PayRefundBatch payRefundBatch = (PayRefundBatch) inParams.get(RefundmentConstant.REFUNDMENT_BATCH);
		Platform platform = (Platform) inParams.get(RefundmentConstant.PAYMENT_PLATFORM);

		
		String out_batch_no = payRefundBatch.getBatchNo();//计费侧批次号
		Date now = Calendar.getInstance().getTime();
		
		String priKey = AlipayHelper.readText(platform.getRefundPrivateUrl());//退款证书
		
    	/**退款数据集*/
		List<String> dataDetailList = null;
		//请求支付系统的退款参数
		StringBuffer reqDataDetailBuff = new StringBuffer();
		String reqDataDetails = "";
		Long payMerchantId = 0L;
		if(!StringUtils.isBlank(ObjectUtils.toString(payRefundBatch.getDetails()))){
			dataDetailList = new ArrayList<String>();
			JSONArray details = JSONArray.parseArray(ObjectUtils.toString(payRefundBatch.getDetails()));
			for(Object detail :details){
    			
    			String dataStr = "";
    			JSONObject jsonDetail = JSONObject.parseObject(detail.toString());

    			PaymentOrder paymentOrder = paymentOrderService.queryOrderByPartnerOrderNo(ObjectUtils.toString(jsonDetail.get("orderno")),payRefundBatch.getPartnerId());
    			//单笔退款交易
    			dataStr = ObjectUtils.toString(paymentOrder.getOtherOrderNo()) + "^" + StringUtils.trim(jsonDetail.getString("money")) + "^" +
    					StringUtils.trim(jsonDetail.getString("refundnote"));
    			//退款交易结果集
    			dataDetailList.add(dataStr);
    			payMerchantId = paymentOrder.getMerchantId();//退款申请号id
    		}
    	}
		if((null!=dataDetailList) && (dataDetailList.size()>0)){
			Iterator<String> iter = dataDetailList.iterator();
			while (iter.hasNext()) {
				reqDataDetailBuff.append(iter.next() + "#");
			}
			if(reqDataDetailBuff.lastIndexOf("#") > 0){
				reqDataDetails = reqDataDetailBuff.substring(0, reqDataDetailBuff.lastIndexOf("#"));
			}
		}
		
		Assert.assertNotSame("非法退款申请号", 0L, payMerchantId);
		
		String extPayRefundUrl = platform.getPlatformExt();//渠道技术信息扩展
		String payRefundUrl = "";// 请求退款地址refundUrl
		String payRefunBackUrl = "";// 退款回调地址refundBackUrl
		if(StringUtils.isNotEmpty(extPayRefundUrl)){
			JSONObject jsonDetail = JSONObject.parseObject(extPayRefundUrl);
			payRefundUrl = jsonDetail.getString("refundUrl");
			payRefunBackUrl = jsonDetail.getString("refundBackUrl");
		}
		
		if(StringUtils.isBlank(payRefundUrl)){
			payRefundUrl = platform.getPayUrl();
		}
		if(StringUtils.isBlank(payRefunBackUrl)){
			payRefunBackUrl = platform.getPayRefundUrl();
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("service", "refund_fastpay_by_platform_nopwd");//即时到账批量退款无密接口
		params.put("partner", platform.getMerchantNo());//通过平台获取支付商户号
		params.put("_input_charset", _charset_encode);
		params.put("notify_url", String.format(payRefunBackUrl,payMerchantId,platform.getPlatformId()));//通过支付商户号获取回调地址
		params.put("batch_no", out_batch_no);
		params.put("refund_date", DateFormatUtils.format(now, "yyyy-MM-dd HH:mm:ss"));//格式为：yyyy-MM-dd hh:mm:ss。
		params.put("batch_num", ObjectUtils.toString(payRefundBatch.getBatchNum()));//退款总笔数
		params.put("detail_data", reqDataDetails);//退款数据集
		
		//params.put("seller_email", platform.getManageUser()); // 我方在支付宝的email		

		LinkedHashMap<String, Object> linkedHashMap = AlipayHelper.sortMap(params);

		String source = EncryptStringUtils.linkedHashMapToStringWithKey(linkedHashMap, true);
		// 2.加密
		Map<String, Object> encryptParams = new HashMap<String, Object>();
		encryptParams.put("source", source);
		encryptParams.put("priKey", priKey);
		String sign = this.encode(encryptParams);

		// 3.剩余需要传递参数
		params.put("sign", sign);
//		params.put("sign_type", "RSA");
		params.put("payRefundUrl", payRefundUrl); // 提交给对方的退款地址
		params.put("acceptCharset", _charset_encode); // 提交给对方的支付编码
		
		// 4.风险检测
		alipaySecurityRiskDetect.riskDetect(inParams,alipaySecurityRiskDetect.TERMINAL_TYPE_APP,_charset_encode);

		
		params.put(RefundmentConstant.PAYMENT_PLATFORM, platform);
		params.put(RefundmentConstant.REFUNDMENT_BATCH,payRefundBatch);
		return params;
	}
	
	/**
	 * 重写执行退款操作
	 */
	public Map<String, Object> executeRefund(Map<String, Object> inParams) {
		// 1.参数整理
		String sign_type = "RSA";
		Platform platform = (Platform) inParams.get(RefundmentConstant.PAYMENT_PLATFORM);
		
		List<DIOrderNoRefundQueryData> orderNoRefundQueryDataList = new ArrayList<DIOrderNoRefundQueryData>();
		
		String extPayRefundUrl = platform.getPlatformExt();//渠道技术信息扩展
		String payRefundUrl = "";// 请求退款地址refundUrl
		if(StringUtils.isNotEmpty(extPayRefundUrl)){
			JSONObject jsonDetail = JSONObject.parseObject(extPayRefundUrl);
			payRefundUrl = jsonDetail.getString("refundUrl");
		}
		// 支付宝退款私钥文件地址
		String priKey = AlipayHelper.readText(platform.getRefundPrivateUrl());//REFUND_PRIVATE_KEY_FILE
		
		// 支付宝订单退款url地址与订单验证和订单支付为同一url地址
		if(StringUtils.isBlank(payRefundUrl)){
			payRefundUrl = platform.getPayUrl();
		}
		String paygateway = payRefundUrl;
		paygateway = (paygateway.endsWith("&")||paygateway.endsWith("?")) ? paygateway : paygateway + "?";
		
		
		String service = ObjectUtils.toString(inParams.get("service"));
		String batch_no = ObjectUtils.toString(inParams.get("batch_no"));
		String partner = ObjectUtils.toString(inParams.get("partner"));
		String notify_url = ObjectUtils.toString(inParams.get("notify_url"));
		String refund_date = ObjectUtils.toString(inParams.get("refund_date"));
		String batch_num = ObjectUtils.toString(inParams.get("batch_num"));
		String detail_data = ObjectUtils.toString(inParams.get("detail_data"));
		
		String signURL = AlipayHelper.rsaRefundSign(paygateway, service, sign_type, batch_no, _charset_encode,
				partner,notify_url,refund_date,batch_num,detail_data, priKey);

		
		// 2.到支付宝去校验并获取结果
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(signURL);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		try {
			responseBody = new String(httpclient.execute(httpGet, responseHandler).getBytes(_charset_encode));
		} catch (Exception e) {
			throw new ValidationException("订单退款:访问支付宝平台异常", e);
		}
		httpclient.getConnectionManager().shutdown();
		
		if (logger.isInfoEnabled()){
			logger.info("支付宝订单退款地址(直接访问)：" + signURL);
			logger.info("支付宝订单退款返回responseBody:\n" + responseBody);			
		}

		Map<String, Object> outParams = new HashMap<String, Object>();
		if (StringUtils.isBlank(responseBody)) {
			outParams.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
			return outParams;
		}

		Document doc = null;
		try {
			doc = DocumentHelper.parseText(responseBody);
		} catch (DocumentException e) {
			throw new ValidationException("支付宝退款订单xml转换异常");
		}
		Node _is_success = doc.selectSingleNode("//alipay/is_success");
		String is_success = _is_success.getText();

		// 3.结果验证A - is_success 验证，判断此次查询是否成功
		String refundState = RefundmentConstant.PAYMENT_STATE_REFUND_INIT;
		
		PayRefundBatch payRefundBatch = (PayRefundBatch) inParams.get(RefundmentConstant.REFUNDMENT_BATCH);
		String partnerBatchNo = "";
		String result = "";
		if ("F".equals(is_success)) {
			refundState = RefundmentConstant.PAYMENT_STATE_REFUND_FAILED;
			result = "订单退款失败";
			throw new ValidationException("订单退款失败：支付宝返回：" + result);
		} else if ("T".equals(is_success)) {
			
			partnerBatchNo = payRefundBatch.getPartnerBatchNo();//业务方批次号
			refundState = RefundmentConstant.PAYMENT_STATE_REFUND_PRO;
			result = "订单退款申请成功";
			
			//支付宝退款申请成功,更新数据库状态为退款中
			if(!StringUtils.isBlank(ObjectUtils.toString(payRefundBatch.getDetails()))){
				
				JSONArray details = JSONArray.parseArray(ObjectUtils.toString(payRefundBatch.getDetails()));
				for(Object detail :details){
					// 查询响应
					DIOrderNoRefundQueryData orderNoRefundQueryData = new DIOrderNoRefundQueryData();
					
	    			JSONObject jsonDetail = JSONObject.parseObject(detail.toString());
	    			PaymentOrder paymentOrder = paymentOrderService.queryOrderByPartnerOrderNo(ObjectUtils.toString(jsonDetail.get("orderno")),payRefundBatch.getPartnerId());
	    			
	    			orderNoRefundQueryData.setResult("退款申请OK,支付宝订单号:"+paymentOrder.getOtherOrderNo()+",支付宝退款单号:"+paymentOrder.getOtherOrderNo()+",申请退款金额:"+paymentOrder.getMoney()+",退款渠道:"+",其他信息:"+is_success+","+result);
	    			orderNoRefundQueryData.setStatusCode(refundState);
	    			orderNoRefundQueryData.setPayplatformOrderNo(paymentOrder.getOtherOrderNo());// 对方支付单号
	    			orderNoRefundQueryData.setPayplatformBatchNo(payRefundBatch.getBatchNo()); // 计费退款单号
	    			orderNoRefundQueryData.setOrderNo(paymentOrder.getOrderNo());// 计费支付单号
	    			orderNoRefundQueryData.setMoney(StringUtils.trim(jsonDetail.getString("money")));// 退款金额
	    			orderNoRefundQueryDataList.add(orderNoRefundQueryData);
	    		}
	    	}
			
			
		}

		outParams.put(RefundmentConstant.REFUND_RESULT_DETAILS, orderNoRefundQueryDataList); // 退款明细
		outParams.put(RefundmentConstant.PARTNER_BATCHNO, partnerBatchNo); // 对方批次号
		outParams.put(RefundmentConstant.REFUNDMENT_STATE, refundState); // 退款状态
		outParams.put(ErrorCode.TIP_INFO,result);//退款返回结果
		
		return outParams;
	}
	
	/**
	 * 退款后台回调参数校验
	 * @param request
	 * @param platform
	 * @return
	 * @throws ValidationException
	 * @throws DataAccessException
	 * @throws PaymentRedirectException
	 */
	@Override
	public Map<String, Object> validateRefundBackParams(HttpServletRequest request, Platform platform)
			throws ValidationException, DataAccessException, PaymentRedirectException {
		// 1.到支付宝平台验证URL是否合法
		String notify_id = StringUtils.trim(request.getParameter("notify_id"));
		String sign = StringUtils.trim(request.getParameter("sign"));
		String partner = platform.getMerchantNo();
		
		String extPayRefundUrl = platform.getPlatformExt();//渠道技术信息扩展
		String refundQueryUrl = "";// 请求退款地址refundQueryUrl
		if(StringUtils.isNotEmpty(extPayRefundUrl)){
			JSONObject jsonDetail = JSONObject.parseObject(extPayRefundUrl);
			refundQueryUrl = jsonDetail.getString("refundQueryUrl");
		}
		
		String alipayNotifyURL = refundQueryUrl + "?service=notify_verify&partner=" + partner + "&notify_id=" + notify_id;
		String responseTxt = AlipayHelper.checkURL(alipayNotifyURL);

		if (!"true".equals(responseTxt)) {
			throw new ValidationException("支付宝后台验证加密异常：验证url异常,返回responseTxt=" + responseTxt);
		}

		String batchNo = getBatchNoFromRequest(request);//退款批次号
		
		String successNum = request.getParameter("success_num");//退款成功笔数
		
		// 2.退款批次单查询
		PayRefundBatch refundBatch = refundmentOrderService.queryRefundBatch(batchNo);
		Assert.notNull(refundBatch, "支付退款批次单查询为空,batchNo:" + batchNo);

		// 加密校验
		Map alipay = request.getParameterMap();
		Properties params = new Properties();
		for (Iterator<Entry<String, Object>> keyValuePairs = alipay.entrySet().iterator(); keyValuePairs.hasNext();) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) keyValuePairs.next();
			String key = entry.getKey();
			String value = request.getParameter(key);
			if (!"sign".equalsIgnoreCase(key) && !"sign_type".equalsIgnoreCase(key)) {
				try {
					params.put(key, URLDecoder.decode(value, _charset_encode));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}

		String pubKey = AlipayHelper.readText(platform.getRefundPublicUrl());
//		String pubKey = AlipayHelper.readText("C:\\Users\\fuzl\\Desktop\\o2o\\alipay_rsa_public_key.txt");
		boolean result = AlipayHelper.rsaCheck(params, pubKey, _charset_encode, sign);
		if (!result) {
			// 加密校验失败
			if (logger.isInfoEnabled()) {
				logger.info("==============支付宝后台加密处理失败=================");
				logger.info("我方加密参数：" + JsonUtils.toJson(params));
				logger.info("==============支付宝后台加密处理结束=================\n");
			}
			throw new ValidationException("支付平台加密校验失败");
		}

		// 处理结果详情
		String refundResultDetails = StringUtils.trim(request.getParameter("result_details"));
		
		//交易退款数据集格式:交易退款数据集$收费退款数据集|分润退款数据集|分润退款数据集|...|分润退款数据集$$退子交易
		// 获取交易退款数据集     
		String refundDetails = refundResultDetails;
		if("$".equals(refundResultDetails)){
			refundDetails = refundResultDetails.substring(0, refundResultDetails.indexOf("$"));
		}
		// 处理交易退款数据集 
		String[] detailStrs = refundDetails.split("#");
		String status="";
		
		List<DIOrderNoRefundBackCallData> orderNoRefundBackCallDataList = new ArrayList<DIOrderNoRefundBackCallData>();
		for(int i=0;i<detailStrs.length;i++){
			DIOrderNoRefundBackCallData orderNoRefundBackCallData = new DIOrderNoRefundBackCallData();
			//交易退款数据集格式为：原付款支付宝交易号^退款总金额^处理结果码
			String[] detailStr = detailStrs[i].toString().split("\\^");//java开发需要转换^为\\^
			logger.info("支付宝返回退款明细,returnDetail:"+detailStrs[i].toString());
			//处理每一笔交易退款
			JSONArray details = JSONArray.parseArray(ObjectUtils.toString(refundBatch.getDetails()));
    		JSONObject jsonDetail = JSONObject.parseObject(details.get(0).toString());
//			PaymentOrder paymentOrder = paymentOrderService.queyrOrderByOppositeOrderNo(ObjectUtils.toString(detailStr[0]));//原付款支付宝交易号
			PaymentOrder paymentOrder = paymentOrderService.queryOrderByPartnerOrderNo(ObjectUtils.toString(jsonDetail.get("orderno")),refundBatch.getPartnerId());//原付款业务单号

			orderNoRefundBackCallData.setPayplatformBatchNo(refundBatch.getBatchNo());//无退款单号,写我方
			orderNoRefundBackCallData.setMoney(String.valueOf((new BigDecimal(detailStr[1])).multiply(new BigDecimal(100)).intValue()));//退款总金额*100,转为分
			orderNoRefundBackCallData.setPayplatformOrderNo(detailStr[0]);//支付平台支付单号
			orderNoRefundBackCallData.setOrderNo(paymentOrder.getOrderNo());//我方支付单号
			
			status = detailStr[2];
			/*判断*/
			if("SUCCESS".equals(status)){
				orderNoRefundBackCallData.setStatusCode(RefundmentConstant.PAYMENT_STATE_REFUNDED);
				logger.info("支付宝返回退款提交成功");
			}else{
				orderNoRefundBackCallData.setStatusCode(RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
				logger.info("支付宝返回退款提交失败");
			}
			orderNoRefundBackCallDataList.add(orderNoRefundBackCallData);
		}
		
		Map<String, Object> returned = new HashMap<String, Object>();
		
		returned.put(RefundmentConstant.REFUNDMENT_BATCH, refundBatch);//退款批次单
		returned.put(RefundmentConstant.REFUND_SUCCESS_NUM, successNum);//退款成功笔数
		returned.put(RefundmentConstant.REFUND_RESULT_DETAILS, orderNoRefundBackCallDataList);
		return returned;
	}
	
	public void refundmentReturn(Map<String, Object> inParams, HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess)
			super.responseAndWrite(response, "success");
		else
			super.responseAndWrite(response, "fail");
	}
	
	/**
	 * 退款查询验证
	 * @param orderRefundQueryRequest
	 * @return
	 */
	public Map<String, Object> orderRefundQuery(DIOrderRefundQueryRequest orderRefundQueryRequest){
		Map<String, Object> outParams = ErrorCode.getErrorCode(1);
		DIOrderNoRefundQueryResponse response = new DIOrderNoRefundQueryResponse();
		List<DIOrderNoRefundQueryData> orderNoRefundQueryDataList = new ArrayList<DIOrderNoRefundQueryData>();
		response.setOrderNoRefundQueryDataList(orderNoRefundQueryDataList);
		
		outParams.put(RefundmentConstant.REFUND_RESULT_DETAILS, response.getOrderNoRefundQueryDataList());
		return outParams;
	}
}
