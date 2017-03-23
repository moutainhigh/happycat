package com.woniu.sncp.pay.core.service.payment.platform.alipay.app;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.cbss.api.imprest.direct.request.DIOrderRefundQueryRequest;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundBackCallData;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundQueryData;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundQueryResponse;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.lang.ObjectUtil;
import com.woniu.sncp.lang.StringUtil;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.PaymentConstant;
import com.woniu.sncp.pay.common.utils.RefundmentConstant;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptFactory;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptStringUtils;
import com.woniu.sncp.pay.common.utils.encrypt.Rsa;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.AlipaySecurityRiskDetect;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.tools.AlipayHelper;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.tools.AlipayNotify;
import com.woniu.sncp.pay.repository.pay.PaymentOrder;
import com.woniu.sncp.pojo.refund.PayRefundBatch;

/**
 * 支付宝App支付
 * 
 * PRIVATE_KEY_FILE = paymentConstant.getKeyPath() + "/alipay/woniu_app_rsa_pri.key";
 * PUBLIC_KEY_FILE = paymentConstant.getKeyPath() + "/alipay/alipay_app_rsa_pubkey.pem";
 * 
 * QUERY_PUBLIC_KEY_FILE = paymentConstant.getKeyPath() + "/alipay/alipay_app_rsa_pubkey.pem";
 * QUERY_PRIVATE_KEY_FILE = paymentConstant.getKeyPath() + "/alipay/woniu_app_rsa_pri.key";
 * 
 */
@Service("alipayAppPayment")
public class AlipayAppPayment extends AbstractPayment {

	/**
	 * 支付宝支付编码
	 */
	protected final String _charset_encode = "utf-8";
	
	@Resource
	private AlipaySecurityRiskDetect alipaySecurityRiskDetect;

	@Override
	public String encode(Map<String, Object> inParams) throws ValidationException {
		String source = (String) inParams.get("source");
		String priKey = (String) inParams.get("priKey");
		String sign = null;
		try {
			sign = EncryptFactory.getInstance(Rsa.NAME).sign(source, priKey, "");
		} catch (Exception e) {
			throw new ValidationException("RSASign fail!", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info(" >>> RSASign Source: " + source);
			logger.info(" >>> RSASign Result: " + sign);
		}
		return sign;
	}
	
	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams) throws ValidationException {
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		// 1.拼装参数
		String source = getNewOrderInfo(inParams); 
		// 2.加密
		String priKey = AlipayHelper.readText(platform.getPrivateUrl());//PRIVATE_KEY_FILE
		String sign;
		try {
			sign = EncryptFactory.getInstance(Rsa.NAME).sign(source, priKey, "");
			sign = URLEncoder.encode(sign,_charset_encode);
		} catch (Exception e) {
			throw new ValidationException("rsa 签名异常");
		}
		source += "&sign=\"" + sign + "\"&" + getSignType();
		// 3.剩余需要传递参数
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("source", source);
		logger.info("source:{}", source);
		
		// 4.风险检测
		alipaySecurityRiskDetect.riskDetect(inParams,alipaySecurityRiskDetect.TERMINAL_TYPE_APP,_charset_encode);
		return params;
	}
	
	
	private String getSignType() {
		return "sign_type=\"RSA\"";
	}
	
	private String getNewOrderInfo(Map<String, Object> inParams) {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		String productName = ObjectUtil.toString(inParams.get("productName"));
		String payExpired = ObjectUtil.toString(inParams.get("payexpired"));
		
		StringBuilder sb = new StringBuilder();
		sb.append("partner=\"");
		sb.append(platform.getMerchantNo());
		sb.append("\"&out_trade_no=\"");
		sb.append(paymentOrder.getOrderNo());
		sb.append("\"&subject=\"");
		sb.append(productName);
		sb.append("\"&body=\"");
		sb.append(productName);
		sb.append("\"&total_fee=\"");
		sb.append(ObjectUtils.toString(paymentOrder.getMoney()));
		if(StringUtil.isNotBlank(platform.getBehindUrl(paymentOrder.getMerchantId()))){
			sb.append("\"&notify_url=\"");
			// 网址需要做URL编码
			try {
				sb.append(URLEncoder.encode(platform.getBehindUrl(paymentOrder.getMerchantId()),_charset_encode));
			} catch (UnsupportedEncodingException e) {
				logger.error("bindUrl encode编码错误,"+e.getMessage(),e);
			}
		}
		sb.append("\"&service=\"mobile.securitypay.pay");
		sb.append("\"&_input_charset=\""+_charset_encode);
		sb.append("\"&payment_type=\"1");
		sb.append("\"&seller_id=\"");
		sb.append(platform.getMerchantNo());
		if(StringUtil.isNotBlank(platform.getFrontUrl(paymentOrder.getMerchantId()))){
			// 如果show_url值为空，可不传
			sb.append("\"&show_url=\"").append(platform.getFrontUrl(paymentOrder.getMerchantId()));
		}
//		if(StringUtils.isNotBlank(payExpired)){
//			sb.append("\"&it_b_pay=\""+payExpired);
//		}
		//表单号:31586 modified by fuzl@snail.com
		if(null != platform.getTransTimeout() && platform.getTransTimeout() >0 ){
			sb.append("\"&it_b_pay=\""+platform.getTransTimeout()+"m");//超时时间,分钟
		}
		sb.append("\"");

		return new String(sb);
	}

	private String trim(HttpServletRequest request, String field) {
		return StringUtils.trim(request.getParameter(field));
	}

	private Map<String, String> wrapNotifyIntoMap(HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		Map<String,Object> paramMap = request.getParameterMap();
		Map<String,String> returnMap = new HashMap<String,String>();
		Set<String> keySet = paramMap.keySet();
		for(String key : keySet){
			returnMap.put(key, trim(request,key));
		}
		return returnMap;
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request, Platform platform) throws ValidationException, DataAccessException , PaymentRedirectException{
		// 获取参数及解析业务数据
		Map<String, String> params = wrapNotifyIntoMap(request);
		final String outTradeNo = trim(request, "out_trade_no");// 我方的订单号
		final String tradeStatus = trim(request, "trade_status");
		final String tradeNo = trim(request, "trade_no");// 支付宝的订单号
		final String totalFee = trim(request, "total_fee");

		String payCheckUrl = platform.getPayCheckUrl();
		Assert.notNull(payCheckUrl, "payCheckUrl不能为空");
		payCheckUrl = payCheckUrl + "?service=notify_verify&";
		// 加密串校验
		boolean isCheck = AlipayNotify.verify(params, ObjectUtil.toString(params.get("seller_id")), AlipayHelper.readText(platform.getPublicUrl()), payCheckUrl);//PUBLIC_KEY_FILE
		if(!isCheck){
			throw new ValidationException("支付宝App异步回调加密串校验异常");
		}
		// 订单查询及验证
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(outTradeNo);
		Assert.notNull(paymentOrder, "支付宝App支付订单查询为空,orderNo:" + outTradeNo);

		// 判断支付状态
		Map<String, Object> returned = new HashMap<String, Object>();
		if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) { // 支付成功
			logger.info("支付宝App返回支付成功");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else { // 未支付
			logger.info("支付宝App返回未支付");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}

		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, tradeNo);
		returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(totalFee)).multiply(new BigDecimal(100)).intValue()));
		
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());

		return returned;
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams, HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess)
			super.responseAndWrite(response, "success");
		else
			super.responseAndWrite(response, "fail");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		// 1.拼装地址
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		String out_trade_no = paymentOrder.getOrderNo();
		String partner = platform.getMerchantNo();
		String priKey = AlipayHelper.readText(platform.getPrivateUrl());//QUERY_PRIVATE_KEY_FILE
		String sign_type = "RSA";
		String service = "single_trade_query";
		// 支付宝订单验证和订单支付为同一url地址
		String paygateway = platform.getPayCheckUrl();
		paygateway = (paygateway.endsWith("&")||paygateway.endsWith("?")) ? paygateway : paygateway + "?";

		String signURL = AlipayHelper.rsaQuerySign(paygateway, service, sign_type, out_trade_no, _charset_encode, partner, priKey);

		// 2.到支付宝去校验并获取结果
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(signURL);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		try {
			responseBody = new String(httpclient.execute(httpGet, responseHandler).getBytes(_charset_encode));
		} catch (Exception e) {
			throw new ValidationException("订单校验:访问支付宝App平台异常", e);
		}
		httpclient.getConnectionManager().shutdown();

		if (logger.isInfoEnabled()) {
			logger.info("支付宝App订单验证地址(直接访问)：" + signURL);
			logger.info("支付宝App订单验证返回responseBody:\n" + responseBody);
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
			throw new ValidationException("支付宝App订单校验xml转换异常");
		}
		Node _is_success = doc.selectSingleNode("//alipay/is_success");
		String is_success = _is_success.getText();

		// 3.结果验证A - is_success 验证，判断此次查询是否成功
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		String total_fee = null;
		String oppositeOrderNo = "";
		if ("F".equals(is_success)) {
			throw new ValidationException("订单验证失败：支付宝App返回：" + responseBody);
		} else if ("T".equals(is_success)) {
			Element trade = (Element) doc.selectSingleNode("//alipay/response/trade");
			Properties properties = new Properties();
			for (Iterator<Element> it = trade.elementIterator(); it.hasNext();) {
				Element el = it.next();// el.getText()
				properties.put(el.getName(), el.getText());
			}
			Node signNode = doc.selectSingleNode("//alipay/sign");
			String pubKey = AlipayHelper.readText(platform.getPublicUrl());//QUERY_PUBLIC_KEY_FILE

			// 4.DSA证书校验，判断返回信息是否一致
			boolean check = AlipayHelper.rsaCheck(properties, pubKey, _charset_encode, signNode.getText());
			if (!check) {
				logger.error("支付宝App订单校验参数校对失败，check=false");
				outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
				return outParams;
			} else {
				// 5.获取我方信息进行交易判断
				String trade_status = properties.getProperty("trade_status"); // 支付状态
				total_fee = properties.getProperty("total_fee");// 交易金额
				if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {
					logger.info("结束 -- 支付宝App订单对账 -- 支付成功");
					oppositeOrderNo = properties.getProperty("trade_no");
					payState = PaymentConstant.PAYMENT_STATE_PAYED;
				} else {
					logger.info("结束 -- 支付宝App订单对账 -- 未支付");
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
		
		// 支付宝退款私钥文件地址
		String priKey = AlipayHelper.readText(platform.getRefundPrivateUrl());//REFUND_PRIVATE_KEY_FILE
		
		String extPayRefundUrl = platform.getPlatformExt();//渠道技术信息扩展
		String payRefundUrl = "";// 请求退款地址refundUrl
		if(StringUtils.isNotEmpty(extPayRefundUrl)){
			JSONObject jsonDetail = JSONObject.parseObject(extPayRefundUrl);
			payRefundUrl = jsonDetail.getString("refundUrl");
		}
		
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
			partnerBatchNo = payRefundBatch.getPartnerBatchNo();//对方批次号
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
	
	
	@Override
	public void refundmentReturn(Map<String, Object> inParams, HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess)
			super.responseAndWrite(response, "success");
		else
			super.responseAndWrite(response, "fail");
	}
	
	public Map<String, Object> cancelOrder(Map<String, Object> inParams){
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		String clientIp = (String) inParams.get(PaymentConstant.CLIENT_IP);
		
		String outTradeNo = paymentOrder.getOrderNo();
		String priKey = AlipayHelper.readText(platform.getPrivateUrl());

		String service = "close_trade";
		String signType = Rsa.NAME;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("service", service);
		params.put("partner", platform.getMerchantNo());
		params.put("_input_charset", _charset_encode);
		params.put("out_order_no", outTradeNo);
		params.put("ip", clientIp);
		
		LinkedHashMap<String, Object> linkedHashMap = AlipayHelper.sortMap(params);
		String source = EncryptStringUtils.linkedHashMapToStringWithKey(linkedHashMap, true);
		// 2.加密
		Map<String, Object> encryptParams = new HashMap<String, Object>();
		encryptParams.put("source", source);
		encryptParams.put("priKey", priKey);
		String sign = this.encode(encryptParams);

		params.put("sign_type", signType);
		
		// 2.到支付宝去校验并获取结果
		String paygateway = platform.getPayCheckUrl() + "?";
		String responseBody = null;
		try {
			String signURL = AlipayHelper.getURLContent(params,sign,_charset_encode,signType);
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(paygateway+signURL);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			try {
				responseBody = new String(httpclient.execute(httpGet, responseHandler).getBytes(_charset_encode));
				logger.info("responseBody:"+responseBody);
			} catch (Exception e) {
				throw new ValidationException("订单取消:访问支付宝平台异常", e);
			}
			httpclient.getConnectionManager().shutdown();
			
		} catch (Exception e1) {
			throw new ValidationException("订单取消:生成请求URL异常", e1);
		}
		
		Map<String, Object> outParams = new HashMap<String, Object>();
		if (StringUtils.isBlank(responseBody)) {
			throw new ValidationException("订单取消:对方返回为空");
		}
		
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(responseBody);
		} catch (DocumentException e) {
			throw new ValidationException("订单取消校验xml转换异常");
		}
		Node _is_success = doc.selectSingleNode("//alipay/is_success");
		String is_success = _is_success.getText();
		
		if ("F".equals(is_success)) {
//			Node _is_error = doc.selectSingleNode("//alipay/error");
//			String is_error = _is_error.getText();
//			if("TRADE_NOT_EXIST".equals(is_error)){
//				outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_CANCEL);
//			} else {
				throw new ValidationException("订单取消：支付宝App返回：" + doc.selectSingleNode("//alipay/error").getText());
//			}
		} else if ("T".equals(is_success)){
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_CANCEL);
		} else {
			outParams.put(PaymentConstant.PAYMENT_STATE, paymentOrder.getPaymentState());
		}
		
		return outParams;
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
