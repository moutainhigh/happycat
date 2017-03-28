package com.woniu.sncp.pay.core.service.payment.platform.alipay.wap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
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
import com.woniu.sncp.lang.ObjectUtil;
import com.woniu.sncp.net.NetServiceException;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptFactory;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptStringUtils;
import com.woniu.sncp.pay.common.utils.encrypt.Rsa;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.AlipaySecurityRiskDetect;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.tools.AlipayHelper;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.pojo.refund.PayRefundBatch;

/**
 * 支付宝手机wap支付渠道
 * 
 * @author luzz
 *
 */
@Service("alipayWapPayment")
public class AlipayWapPayment extends AbstractPayment {

//	/**
//	 * RSA私钥
//	 */
//	protected String PRIVATE_KEY_FILE;
//	/**
//	 * RSA公钥
//	 */
//	protected String PUBLIC_KEY_FILE;
//	
//	/**
//	 * DSA私钥
//	 */
//	protected String QUERY_PRIVATE_KEY_FILE;
//	/**
//	 * DSA公钥
//	 */
//	protected String QUERY_PUBLIC_KEY_FILE;

	/**
	 * 支付宝支付编码
	 */
	protected final String _charset_encode = "utf-8";
	
	@Resource
	private PaymentConstant paymentConstant;
	
	@Resource
	private AlipaySecurityRiskDetect alipaySecurityRiskDetect;
	
//	@PostConstruct
//	public void init() {
//        PRIVATE_KEY_FILE = paymentConstant.getKeyPath() + "/alipay/woniu_rsa_pri.key";
//        PUBLIC_KEY_FILE = paymentConstant.getKeyPath() + "/alipay/alipay_rsa_pubkey.pem";
//        
//		QUERY_PUBLIC_KEY_FILE = paymentConstant.getKeyPath() + "/alipay/alipay_app_rsa_pubkey.pem";
//		QUERY_PRIVATE_KEY_FILE = paymentConstant.getKeyPath() + "/alipay/woniu_app_rsa_pri.key";
//	}
	
	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		String source = (String) inParams.get("source");
		String priKey = (String) inParams.get("priKey");
		String sign = null;
		try {
			sign = EncryptFactory.getInstance(Rsa.NAME).sign(source, priKey,_charset_encode);
		} catch (Exception e) {
			throw new ValidationException("手机支付宝加密失败", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("手机支付宝加密源：" + source);
			logger.info("手机支付宝加密结果：" + sign);
		}
		return sign;
	}
	
	private Map<String, String> putToTreeMap(HttpServletRequest request,
			Map<String, Object> requestParams) {
		Map<String, String> treeMap = new TreeMap<String, String>();
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
		// 1.拼装参数
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("service", "alipay.wap.auth.authAndExecute");
		params.put("format", "xml");
		params.put("v", "2.0");
		params.put("partner", platform.getMerchantNo());
		params.put("sec_id", "0001");//0001：RSA签名算法,MD5：MD5签名算法
		//params.put("_input_charset", _charset_encode); // 提交给对方的支付编码
		params.put("req_data", getReqData(this.getRequestToken(inParams)));
		
		// 2.加密
		String priKey = AlipayHelper.readText(platform.getPrivateUrl());
		LinkedHashMap<String, Object> linkedHashMap = AlipayHelper.sortMap(params);
		String source = EncryptStringUtils.linkedHashMapToStringWithKey(linkedHashMap, true);
		Map<String, Object> encryptParams = new HashMap<String, Object>();
		encryptParams.put("source", source);
		encryptParams.put("priKey", priKey);
		String sign = this.encode(encryptParams);
		
		// 3.剩余需要传递参数
		params.put("sign", sign);
		params.put("payUrl", platform.getPayUrl()); // 提交给对方的支付地址
		params.put("method", "get");
		
		// 4.风险检测
		alipaySecurityRiskDetect.riskDetect(inParams,alipaySecurityRiskDetect.TERMINAL_TYPE_WAP,_charset_encode);

		return params;
	}
	
	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException,
			DataAccessException, PaymentRedirectException {
		//获取参数及解析业务数据
		String service = StringUtils.trim(request.getParameter("service"));
		String v = StringUtils.trim(request.getParameter("v"));
		String secId = StringUtils.trim(request.getParameter("sec_id"));
		String sign = StringUtils.trim(request.getParameter("sign"));
		String notifyData = StringUtils.trim(request.getParameter("notify_data"));
		
		logger.info("params:"+putToTreeMap(request, request.getParameterMap()));
		
		//加密串校验
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("service", service);
		params.put("sec_id", secId);
		params.put("v", v);
		
        try {
            //对返回的notify_data数据用商户私钥解密
        	String priKey = AlipayHelper.readText(platform.getPrivateUrl());
            String notify_data=EncryptFactory.getInstance(Rsa.NAME).decrypt(notifyData, priKey,"");
            params.put("notify_data", notify_data);
        } catch (Exception e) {
            logger.error("手机支付宝解密notify_data数据失败");
        }
        
		String pubKey = AlipayHelper.readText(platform.getPublicUrl());
		if(!EncryptFactory.getInstance(Rsa.NAME).verify(sign, getVerifyData(params), pubKey, "")){
			throw new ValidationException("手机支付宝异步回调加密串校验异常");
		}
		
		//解析业务数据
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(ObjectUtil.toString(params.get("notify_data")));
		} catch (DocumentException e) {
			throw new ValidationException("手机支付宝notify_data xml转换异常");
		}
		Element notify = (Element)doc.selectSingleNode("//notify");
		Properties properties = new Properties();
		for (Iterator<Element> it = notify.elementIterator(); it.hasNext();) {
			Element el = it.next();// el.getText()
			properties.put(el.getName(), el.getText());
		}
		
		String tradeStatus = properties.getProperty("trade_status");
		String orderNo = properties.getProperty("out_trade_no");
		String oppositeOrderNo = properties.getProperty("trade_no");
		// 订单查询及验证
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "手机支付订单查询为空,orderNo:" + orderNo);
		
		
		//判断支付状态
		Map<String, Object> returned = new HashMap<String, Object>();
		if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) { // 支付成功
			logger.info("手机支付宝返回支付成功");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else { // 未支付
			logger.info("手机支付宝返回未支付");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}
		
		String paymentMoney = properties.getProperty("total_fee");
		
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);
		returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(paymentMoney)).multiply(new BigDecimal(100)).intValue()));
		
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		
		return returned;
	}
	
	private String getVerifyData(Map<String, Object> map) {
        String service = ObjectUtils.toString(map.get("service"));
        String v = (String) ObjectUtils.toString(map.get("v"));
        String sec_id = ObjectUtils.toString(map.get("sec_id"));
        String notify_data = ObjectUtils.toString(map.get("notify_data"));

        logger.info("通知参数为："+"service=" + service + "&v=" + v + "&sec_id=" + sec_id + "&notify_data="+ notify_data);
        return "service=" + service + "&v=" + v + "&sec_id=" + sec_id + "&notify_data=" + notify_data;
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
		// 1.拼装地址
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		String out_trade_no = paymentOrder.getOrderNo();
		String partner = platform.getMerchantNo();
		String priKey = AlipayHelper.readText(platform.getQueryPrivateUrl());
		String sign_type = "RSA";
		String service = "single_trade_query";
		// 支付宝订单验证和订单支付为同一url地址
		String paygateway = platform.getPayCheckUrl() + "?";

		String signURL = AlipayHelper.rsaQuerySign(paygateway, service, sign_type, out_trade_no, _charset_encode,
				partner, priKey);

		// 2.到支付宝去校验并获取结果
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(signURL);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		try {
			responseBody = new String(httpclient.execute(httpGet, responseHandler).getBytes(_charset_encode));
		} catch (Exception e) {
			throw new ValidationException("订单校验:访问手机支付宝平台异常", e);
		}
		httpclient.getConnectionManager().shutdown();
		
		if (logger.isInfoEnabled()){
			logger.info("手机支付宝订单验证地址(直接访问)：" + signURL);
			logger.info("手机支付宝订单验证返回responseBody:\n" + responseBody);			
		}

		Map<String, Object> outParams = new HashMap<String, Object>();
		if (StringUtils.isBlank(responseBody)) {
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return outParams;
		}

		Document doc = null;
		try {
			doc = DocumentHelper.parseText(responseBody);
		} catch (DocumentException e) {
			throw new ValidationException("手机支付宝订单校验xml转换异常");
		}
		Node _is_success = doc.selectSingleNode("//alipay/is_success");
		String is_success = _is_success.getText();

		// 3.结果验证A - is_success 验证，判断此次查询是否成功
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		String total_fee = null;
		String oppositeOrderNo = "";
		if ("F".equals(is_success)) {
			throw new ValidationException("订单验证失败：手机支付宝返回：" + responseBody);
		} else if ("T".equals(is_success)) {
			Element trade = (Element) doc.selectSingleNode("//alipay/response/trade");
			Properties properties = new Properties();
			for (Iterator<Element> it = trade.elementIterator(); it.hasNext();) {
				Element el = it.next();// el.getText()
				properties.put(el.getName(), el.getText());
			}
			Node signNode = doc.selectSingleNode("//alipay/sign");
			String pubKey = AlipayHelper.readText(platform.getQueryPublicUrl());

			// 4.DSA证书校验，判断返回信息是否一致
			boolean check = AlipayHelper.rsaCheck(properties, pubKey, _charset_encode, signNode.getText());
			if (!check) {
				logger.error("手机支付宝订单校验参数校对失败，check=false");
				outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
				return outParams;
			} else {
				// 5.获取我方信息进行交易判断
				String trade_status = properties.getProperty("trade_status"); // 支付状态
				total_fee = properties.getProperty("total_fee");// 交易金额
				if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {
					logger.info("结束 -- 手机支付宝订单对账 -- 支付成功");
					oppositeOrderNo = properties.getProperty("trade_no");
					payState = PaymentConstant.PAYMENT_STATE_PAYED;
				} else {
					logger.info("结束 -- 手机支付宝订单对账 -- 未支付");
					payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
				}
			}
		}

		outParams.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo); // 对方订单号
		outParams.put(PaymentConstant.PAYMENT_STATE, payState); // 支付状态
		outParams.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(total_fee)).multiply(new BigDecimal(100)).intValue()));
		return outParams;
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("out_trade_no");
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

    			PaymentOrder paymentOrder = null;//refundmentOrderService.queryOrderByPartnerOrderNo(ObjectUtils.toString(jsonDetail.get("orderno")));
    			//单笔退款交易
    			dataStr = ObjectUtils.toString(paymentOrder.getPayPlatformOrderId()) + "^" + StringUtils.trim(jsonDetail.getString("money")) + "^" +
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
	    			PaymentOrder paymentOrder = null;//refundmentOrderService.queryOrderByPartnerOrderNo(ObjectUtils.toString(jsonDetail.get("orderno")));
	    			
	    			orderNoRefundQueryData.setResult("退款申请OK,支付宝订单号:"+paymentOrder.getPayPlatformOrderId()+",支付宝退款单号:"+paymentOrder.getPayPlatformOrderId()+",申请退款金额:"+paymentOrder.getMoney()+",退款渠道:"+",其他信息:"+is_success+","+result);
	    			orderNoRefundQueryData.setStatusCode(refundState);
	    			orderNoRefundQueryData.setPayplatformOrderNo(paymentOrder.getPayPlatformOrderId());// 对方支付单号
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
		String alipayNotifyURL = platform.getPayCheckUrl() + "?service=notify_verify&partner=" + partner + "&notify_id=" + notify_id;
		String responseTxt = AlipayHelper.checkURL(alipayNotifyURL);

		if (!"true".equals(responseTxt)) {
			throw new ValidationException("支付宝后台验证加密异常：验证url异常,返回responseTxt=" + responseTxt);
		}

		String batchNo = getBatchNoFromRequest(request);//退款批次号
		
		String successNum = request.getParameter("success_num");//退款成功笔数
		
		// 2.退款批次单查询
		PayRefundBatch refundBatch = null;//refundmentOrderService.queryRefundBatch(batchNo);
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
			//根据支付宝交易号，查询业务方订单
			PaymentOrder paymentOrder = null;//refundmentOrderService.queyrOrderByOppositeOrderNo(ObjectUtils.toString(detailStr[0]));//原付款支付宝交易号
			
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
	/**
	 * 用于关联请求与响应，防止请求重播。支付宝限制来自同一个partner的请求号必须唯一。长度最大32位
	 * @return
	 */
	private String getReqId(){
		return String.valueOf(System.currentTimeMillis());
	}
	
	private String getReqData(String token){
		String reqData = "<auth_and_execute_req>" +
							"<request_token>" +
									token +
							"</request_token>" +
						 "</auth_and_execute_req>";
		
		return reqData;
	}
	
	/**
	 * @param subject 商品名称
	 * @param outTradeNo 商户网站唯一订单号
	 * @param totalFee 交易金额
	 * @param sellerAccountName 卖家支付宝账号
	 * @param notifyUrl 服务器异步通知页面路径
	 * @param callbackUrl 支付成功跳转页面路径
	 * @param merchantUrl 商品展示网址
	 * @return
	 */
	private String getReqTokenData(String subject
							,String outTradeNo
							,String totalFee
							,String sellerAccountName
							,String notifyUrl
							,String callbackUrl
							,String cashierCode){
		String reqData = "<direct_trade_create_req>" + "<subject>" + subject
				+ "</subject><out_trade_no>" + outTradeNo
				+ "</out_trade_no><total_fee>" + totalFee
				+ "</total_fee><seller_account_name>" + sellerAccountName
				+ "</seller_account_name>" 
				+ (StringUtils.isEmpty(cashierCode)?"":("<cashier_code>" + cashierCode + "</cashier_code>"))
				+ "<notify_url>" + notifyUrl
				+ "</notify_url><call_back_url>"+callbackUrl+"</call_back_url>";
		        reqData = reqData + "</direct_trade_create_req>";
		return reqData;      
	}
	
	private String getRequestToken(Map<String, Object> inParams){
		// 1.拼装参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		String outTradeNo = paymentOrder.getOrderNo();
		String priKey = AlipayHelper.readText(platform.getPrivateUrl());

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("service", "alipay.wap.trade.create.direct");
		params.put("format", "xml");
		params.put("v", "2.0");
		params.put("partner", platform.getMerchantNo());
		String reqId = getReqId();
		params.put("req_id", reqId);//用于关联请求与响应，防止请求重播。
		params.put("sec_id", "0001");//0001：RSA签名算法,MD5：MD5签名算法
		
		String subject = StringUtils.trim((String) inParams.get("productName"));
		String cashierCode = StringUtils.trim((String) inParams.get("cashierCode"));
		String totalFee = ObjectUtils.toString(paymentOrder.getMoney());
//		String sellerAccountName = "snail.account10@snailgame.net";//卖家的支付宝账号。交易成功后，买家资金会转移到该账户中。snail.account4@snailgame.net
		String sellerAccountName = platform.getManageUser();//卖家的支付宝账号
		String notifyUrl = platform.getBehindUrl(paymentOrder.getMerchantId());
		String callbackUrl = platform.getFrontUrl(paymentOrder.getMerchantId());
		String reqData = getReqTokenData(subject, outTradeNo, totalFee, sellerAccountName, notifyUrl, callbackUrl,cashierCode);
		params.put("req_data", reqData);

		LinkedHashMap<String, Object> linkedHashMap = AlipayHelper.sortMap(params);
		String source = EncryptStringUtils.linkedHashMapToStringWithKey(linkedHashMap, true);
		// 2.加密
		Map<String, Object> encryptParams = new HashMap<String, Object>();
		encryptParams.put("source", source);
		encryptParams.put("priKey", priKey);
		String sign = this.encode(encryptParams);
		
		// 3.请求支付宝获取token
		String response = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;
		String reqTokenUrl = null;
		try {
			httpclient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"utf-8");
			//连接超时
			httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
			//读取超时 
			httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
			
			List<BasicNameValuePair> formparams = new ArrayList<BasicNameValuePair>();
			formparams.add(new BasicNameValuePair("req_data", reqData));
			formparams.add(new BasicNameValuePair("service", "alipay.wap.trade.create.direct"));
			formparams.add(new BasicNameValuePair("sec_id", "0001"));
			formparams.add(new BasicNameValuePair("partner", platform.getMerchantNo()));
			formparams.add(new BasicNameValuePair("req_id", reqId));
			formparams.add(new BasicNameValuePair("sign", sign));
			formparams.add(new BasicNameValuePair("format", "xml"));
			formparams.add(new BasicNameValuePair("v", "2.0"));
			
			reqTokenUrl = platform.getPayUrl();

			
			httppost = new HttpPost(reqTokenUrl);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
			httppost.setEntity(entity);
			
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			response = httpclient.execute(httppost,responseHandler);
			response = URLDecoder.decode(response, "utf-8");
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("支付宝手机支付出错，请与客服联系", e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("支付宝手机支付出错，请与客服联系", e);
		}  finally {
			logger.info("手机支付获取token url:{}",reqTokenUrl);
			logger.info("手机支付返回:{}",response);
			abortConnection(httppost, httpclient);
		}
		
		// 调用成功
		HashMap<String, Object> resMap = new HashMap<String, Object>();
		String v = this.getParameter(response, "v");
		String service = this.getParameter(response, "service");
		String partner = this.getParameter(response, "partner");
		String return_sign = this.getParameter(response, "sign");
		String return_reqId = this.getParameter(response, "req_id");
		String return_secId = this.getParameter(response, "sec_id");
		resMap.put("v", v);
		resMap.put("service", service);
		resMap.put("partner", partner);
		resMap.put("sec_id", return_secId);
		resMap.put("req_id", return_reqId);

		String resData = "";
		
		if (response.contains("<err>")) {
			String businessResult = "";
			businessResult = this.getParameter(response, "res_error");
			logger.error("手机支付宝后台获取Token返回错误信息:{}",businessResult);
			throw new ValidationException("手机支付宝后台获取Token返回错误");
		} else {
            //对返回的res_data数据先用商户私钥解密
			try {
				resData = EncryptFactory.getInstance(Rsa.NAME).decrypt(this.getParameter(response, "res_data"), priKey, "");
			} catch (Exception e) {
				logger.error("手机支付宝后台获取Token解密异常");
				throw new ValidationException("手机支付宝后台获取Token验证加密异常");
			}
		}
		resMap.put("res_data", resData);
		LinkedHashMap<String, Object> return_linkedHashMap = AlipayHelper.sortMap(resMap);
		String return_source = EncryptStringUtils.linkedHashMapToStringWithKey(return_linkedHashMap, true);
		String pubKey = AlipayHelper.readText(platform.getPublicUrl());
		if(!EncryptFactory.getInstance(Rsa.NAME).verify(return_sign, return_source, pubKey, "")){
			throw new ValidationException("手机支付宝后台获取Token验证加密异常");
		}
		
		String requestToken = readXmlNode(resData,"//direct_trade_create_res/request_token");
		return requestToken;
	}

	private String readXmlNode(String resData,String nodePath) {
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(resData);
		} catch (DocumentException e) {
			throw new ValidationException("手机支付宝订单校验xml转换异常");
		}
		Node _requestToken = doc.selectSingleNode(nodePath);
		String requestToken = _requestToken.getText();
		return requestToken;
	}
	
    /**
     * 取得URL中的参数值。
     * <p>如不存在，返回空值。</p>
     * 
     * @param url
     * @param name
     * @return
     */
    private String getParameter(String url, String name) {
        if (name == null || name.equals("")) {
            return null;
        }
        name = name + "=";
        int start = url.indexOf(name);
        if (start < 0) {
            return null;
        }
        start += name.length();
        int end = url.indexOf("&", start);
        if (end == -1) {
            end = url.length();
        }
        return url.substring(start, end);
    }
	
	/**
	 * 释放HttpClient连接
	 * 
	 * @param hrb
	 *            请求对象
	 * @param httpclient
	 * 			  client对象
	 */
	private static void abortConnection(final HttpRequestBase hrb, final HttpClient httpclient){
		if (hrb != null) {
			hrb.abort();
		}
		if (httpclient != null) {
			httpclient.getConnectionManager().shutdown();
		}
	}
}
