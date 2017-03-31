package com.woniu.sncp.pay.core.service.payment.platform.alipay;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
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
import com.woniu.sncp.pay.common.utils.encrypt.Dsa;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptFactory;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptStringUtils;
import com.woniu.sncp.pay.common.utils.encrypt.Rsa;
import com.woniu.sncp.pay.common.utils.http.PayCheckUtils;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.tools.AlipayHelper;
import com.woniu.sncp.pay.core.transfer.model.TransferModel;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.pojo.payment.TransferOrder;
import com.woniu.sncp.pojo.refund.PayRefundBatch;

/**
 * <pre>
 * 支付平台-支付宝(支付宝重试策略重发通知) - 公网环境，无测试环境
 * 1.商户merchantPwd是用于MD5加密使用，这里用的是DSA证书加密，不需要merchantPwd
 * 2.证书路径由EAI后台配置
 * 3.总价和单价不可以同时出现
 * 4.加密串是按照key的<font color=red>升序排列</font>，DSA加密，无需额外增加MD5加密串去校验
 * 5.我方收到消息处理流程
 * 	a.向支付宝系统发送通知验证请求（URL验证）
 * 	b.通知参数的和我方是否一致
 * 	c.处理成功返回success给支付宝
 * 	d.返回消息有时限限制(1分钟)，超时未处理则消息验证失败，支付宝重发消息或我方通过订单查询接口去完成充值
 * 6.金额单位：元，需*100将精度设为分
 * </pre>
 * 
 */
@Service("alipayPayment")
public class AlipayPayment extends AbstractPayment {

	/**
	 * 支付宝支付编码
	 */
	protected final String _charset_encode = "utf-8";
	
	@Resource
	protected AlipaySecurityRiskDetect alipaySecurityRiskDetect;
	

	@Override
	public String encode(Map<String, Object> inParams) {
		String source = (String) inParams.get("source");
		String priKey = (String) inParams.get("priKey");
		String sign = null;
		try {
			sign = EncryptFactory.getInstance(Dsa.NAME).sign(source, priKey, _charset_encode);
		} catch (Exception e) {
			throw new ValidationException("支付宝加密失败", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("支付宝加密源：" + source);
			logger.info("支付宝加密结果：" + sign);
		}
		return sign;
	}
	
	
	public String encodeRsa(Map<String, Object> inParams) {
		String source = (String) inParams.get("source");
		String priKey = (String) inParams.get("priKey");
		String sign = null;
		try {
			sign = EncryptFactory.getInstance(Rsa.NAME).sign(source, priKey, _charset_encode);
		} catch (Exception e) {
			throw new ValidationException("支付宝加密失败", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("支付宝加密源：" + source);
			logger.info("支付宝加密结果：" + sign);
		}
		return sign;
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams) {
		// 1.拼装参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		String out_trade_no = paymentOrder.getOrderNo();
		String priKey = AlipayHelper.readText(platform.getPrivateUrl());

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("service", "create_direct_pay_by_user");
		params.put("partner", platform.getMerchantNo());
		params.put("notify_url", platform.getBehindUrl(paymentOrder.getMerchantId()));
		params.put("return_url", platform.getFrontUrl(paymentOrder.getMerchantId()));
		params.put("body", StringUtils.trim((String) inParams.get("productName")));// 商品具体描述
		params.put("out_trade_no", out_trade_no);
		params.put("payment_type", "1");
		params.put("seller_email", platform.getManageUser()); // 我方在支付宝的email
		params.put("subject", StringUtils.trim((String) inParams.get("productName")));
		params.put("total_fee", ObjectUtils.toString(paymentOrder.getMoney()));
		params.put("_input_charset", _charset_encode);
		//表单号:31586 modified by fuzl@snail.com
		if(null != platform.getTransTimeout() && platform.getTransTimeout() >0 ){
			params.put("it_b_pay", platform.getTransTimeout()+"m");//超时时间,分钟
		}

		LinkedHashMap<String, Object> linkedHashMap = AlipayHelper.sortMap(params);

		String source = EncryptStringUtils.linkedHashMapToStringWithKey(linkedHashMap, true);
		// 2.加密
		Map<String, Object> encryptParams = new HashMap<String, Object>();
		encryptParams.put("source", source);
		encryptParams.put("priKey", priKey);
		String sign = this.encode(encryptParams);

		// 3.剩余需要传递参数
		params.put("sign", sign);
		params.put("sign_type", "DSA");
		params.put("payUrl", platform.getPayUrl()); // 提交给对方的支付地址
		params.put("acceptCharset", _charset_encode); // 提交给对方的支付编码
		
		// 4.风险检测
		alipaySecurityRiskDetect.riskDetect(inParams,alipaySecurityRiskDetect.TERMINAL_TYPE_WEB,_charset_encode);

		return params;
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("out_trade_no");
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams, HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess)
			super.responseAndWrite(response, "success");
		else
			super.responseAndWrite(response, "fail");
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request, Platform platform)
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

		String orderNo = getOrderNoFromRequest(request);
		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "支付订单查询为空,orderNo:" + orderNo);

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

		String pubKey = AlipayHelper.readText(platform.getPublicUrl());
		boolean result = AlipayHelper.dsaCheck(params, pubKey, _charset_encode, sign);
		if (!result) {
			// 加密校验失败
			if (logger.isInfoEnabled()) {
				logger.info("==============支付宝后台加密处理失败=================");
				logger.info("我方加密参数：" + JsonUtils.toJson(params));
				logger.info("==============支付宝后台加密处理结束=================\n");
			}
			throw new ValidationException("支付平台加密校验失败");
		}

		String payResult = StringUtils.trim(request.getParameter("trade_status"));
		
		Map<String, Object> returned = new HashMap<String, Object>();
		if ("TRADE_SUCCESS".equals(payResult) || "TRADE_FINISHED".equals(payResult)) { // 支付成功
			logger.info("支付宝返回支付成功");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else { // 未支付
			logger.info("支付宝返回未支付");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}

		String oppositeOrderNo = StringUtils.trim(request.getParameter("trade_no"));
		String paymentMoney = StringUtils.trim(request.getParameter("total_fee"));

		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);
//		returned.put(PaymentConstant.IMPREST_OPPOSITE_MONEY, String.valueOf(NumberUtils.toFloat(paymentMoney) * 100));
		returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(paymentMoney)).multiply(new BigDecimal(100)).intValue()));
		
		//不验证imprestMode,直接取订单中imprestMode
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		return returned;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		// 1.拼装地址
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		String out_trade_no = paymentOrder.getOrderNo();
		String partner = platform.getMerchantNo();
		String priKey = AlipayHelper.readText(platform.getPrivateUrl());
		String sign_type = "DSA";
		String service = "single_trade_query";
		// 支付宝订单验证和订单支付为同一url地址
		String paygateway = platform.getPayCheckUrl() + "?";

		String signURL = AlipayHelper.dsaQuerySign(paygateway, service, sign_type, out_trade_no, _charset_encode,
				partner, priKey);

		// 2.到支付宝去校验并获取结果
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(signURL);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		try {
			responseBody = new String(httpclient.execute(httpGet, responseHandler).getBytes(_charset_encode));
		} catch (Exception e) {
			throw new ValidationException("订单校验:访问支付宝平台异常", e);
		}
		httpclient.getConnectionManager().shutdown();
		
		if (logger.isInfoEnabled()){
			logger.info("支付宝订单验证地址(直接访问)：" + signURL);
			logger.info("支付宝订单验证返回responseBody:\n" + responseBody);			
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
			throw new ValidationException("支付宝订单校验xml转换异常");
		}
		Node _is_success = doc.selectSingleNode("//alipay/is_success");
		String is_success = _is_success.getText();

		// 3.结果验证A - is_success 验证，判断此次查询是否成功
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		String total_fee = null;
		String oppositeOrderNo = "";
		if ("F".equals(is_success)) {
			throw new ValidationException("订单验证失败：支付宝返回：" + responseBody);
		} else if ("T".equals(is_success)) {
			Element trade = (Element) doc.selectSingleNode("//alipay/response/trade");
			Properties properties = new Properties();
			for (Iterator<Element> it = trade.elementIterator(); it.hasNext();) {
				Element el = it.next();// el.getText()
				properties.put(el.getName(), el.getText());
			}
			Node signNode = doc.selectSingleNode("//alipay/sign");
			String pubKey = AlipayHelper.readText(platform.getPublicUrl());

			// 4.DSA证书校验，判断返回信息是否一致
			boolean check = AlipayHelper.dsaCheck(properties, pubKey, _charset_encode, signNode.getText());
			if (!check) {
				logger.error("支付宝订单校验参数校对失败，check=false");
				outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
				return outParams;
			} else {
				// 5.获取我方信息进行交易判断
				String trade_status = properties.getProperty("trade_status"); // 支付状态
				total_fee = properties.getProperty("total_fee");// 交易金额
				if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {
					logger.info("结束 -- 支付宝订单对账 -- 支付成功");
					oppositeOrderNo = properties.getProperty("trade_no");
					payState = PaymentConstant.PAYMENT_STATE_PAYED;
				} else {
					logger.info("结束 -- 支付宝订单对账 -- 未支付");
					payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
				}
			}
		}

		outParams.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo); // 对方订单号
		outParams.put(PaymentConstant.PAYMENT_STATE, payState); // 支付状态
//		outParams.put(PaymentConstant.IMPREST_OPPOSITE_MONEY, String.valueOf(NumberUtils.toFloat(total_fee) * 100));
		outParams.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(total_fee)).multiply(new BigDecimal(100)).intValue()));
		return outParams;
	}
	
	
	@Override
	public boolean transferRequest(Platform platform,TransferModel transferModel, Map<String, Object> extParams) {
		try{
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("service", "batch_trans_notify_no_pwd");
			params.put("partner", platform.getMerchantNo());
			params.put("_input_charset", _charset_encode);
			
			params.put("notify_url", platform.getBehindUrl(transferModel.getMerchantId()));
			params.put("account_name", transferModel.getAccountInfo());
			String detailData = transferModel.getReceiveOrderNo()+"^"+transferModel.getAccount()
					+"^"+transferModel.getAccountInfo()+"^"+transferModel.getMoney()
					+"^"+transferModel.getReason();
			params.put("detail_data", detailData);
			params.put("batch_no", transferModel.getOrderNo());
			params.put("batch_num", "1");
			params.put("batch_fee", transferModel.getMoney());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			params.put("pay_date", sdf.format(new Date()));
			params.put("email", transferModel.getAccountInfo());
			
			params.put("sign", requestParamsSign(platform,params));
			params.put("sign_type", "DSA");
			
			String postResponse = PayCheckUtils.postRequst(platform.getTransferUrl(), params, 3000, _charset_encode, "AlipayTransfer");
			Document doc = null;
			try {
				doc = DocumentHelper.parseText(postResponse);
			} catch (DocumentException e) {
				throw new ValidationException("支付宝转账请求返回内容解析异常");
			}
			Node isSuccessNode = doc.selectSingleNode("//alipay/is_success");
			String isSuccess = isSuccessNode.getText();
			
			return "T".equalsIgnoreCase(isSuccess);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return false;
		}
	}
	
	@Override
	public String requestParamsSign(Platform platform,Map<String, Object> inParams)
			throws ValidationException {
		
		LinkedHashMap<String, Object> linkedHashMap = AlipayHelper.sortMap(inParams);
		String source = EncryptStringUtils.linkedHashMapToStringWithKey(linkedHashMap, true);
		String priKey = AlipayHelper.readText(platform.getPrivateUrl());
		
		String sign = null;
		try {
			sign = EncryptFactory.getInstance(Dsa.NAME).sign(source, priKey, _charset_encode);
		} catch (Exception e) {
			throw new ValidationException("支付宝加密失败", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("支付宝加密源：" + source);
			logger.info("支付宝加密结果：" + sign);
		}
		return sign;
	}

	@Override
	public Map<String, Object> backendParamsValidate(
			HttpServletRequest request, Platform platform)
			throws ValidationException, DataAccessException{
		//验证notify_url
		String notifyId = StringUtils.trim(request.getParameter("notify_id"));
		String partner = platform.getMerchantNo();
		String alipayNotifyURL = platform.getPayCheckUrl() + "?service=notify_verify&partner=" + partner + "&notify_id=" + notifyId;
		String responseTxt = AlipayHelper.checkURL(alipayNotifyURL);
		if (!"true".equals(responseTxt)) {
			logger.error("支付宝后台通知url验证异常,返回responseTxt=" + responseTxt+",notifyUrl:"+alipayNotifyURL);
			throw new ValidationException("支付宝后台通知url验证异常,返回responseTxt=" + responseTxt);
		}
		
		//订单验证
		String batchNo = StringUtils.trim(request.getParameter("batch_no"));
		TransferOrder queryOrder = null;//transferOrderService.queryOrder(batchNo);
		Assert.notNull(queryOrder, "转账订单查询为空,orderNo:" + batchNo);
		
		//获取数据
		String sign = StringUtils.trim(request.getParameter("sign"));
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
					logger.error(e.getMessage(),e);
				}
			}
		}
		
		//验证签名
		String pubKey = AlipayHelper.readText(platform.getPublicUrl());
		boolean result = AlipayHelper.dsaCheck(params, pubKey, _charset_encode, sign);
		if (!result) {
			// 加密校验失败
			if (logger.isInfoEnabled()) {
				logger.info("==============支付宝后台回调参数加密处理失败=================");
				logger.info("我方加密参数：" + JsonUtils.toJson(params));
				logger.info("==============支付宝后台回调参数加密处理结束=================\n");
			}
			throw new ValidationException("支付平台回调参数加密校验失败");
		}
		
		Map<String, Object> returned = new HashMap<String, Object>();
		//判断状态  组装数据
		//success_details 流水号^收款方账号^收款账号姓名^付款金额^成功标识(S)^成功原因(null)^支付宝内部流水号^完成时间
		//fail_details 流水号^收款方账号^收款账号姓名^付款金额^失败标识(F)^失败原因^支付宝内部流水号^完成时间。
		String successDetails = StringUtils.trim(request.getParameter("success_details"));
		if(StringUtils.isBlank(successDetails)){
			String failDetails = StringUtils.trim(request.getParameter("fail_details"));
			String[] failDtlArr = failDetails.split("^");
			returned.put(PaymentConstant.OPPOSITE_MONEY,failDtlArr[3]);
			returned.put(PaymentConstant.TRANSFER_STATE,TransferOrder.TRANSFER_STATE_FAILED);
			returned.put(PaymentConstant.TRANSFER_STATE_MESSAGE, failDtlArr[5]);
			returned.put(PaymentConstant.OPPOSITE_ORDERNO,failDtlArr[6]);
			returned.put(PaymentConstant.TRANSFER_ACCOUNT, failDtlArr[1]);
		} else {
			String[] successDtlArr = successDetails.split("^");
			returned.put(PaymentConstant.OPPOSITE_MONEY,successDtlArr[3]);
			returned.put(PaymentConstant.TRANSFER_STATE,"S".equalsIgnoreCase(successDtlArr[4])?TransferOrder.TRANSFER_STATE_COMPLETED:TransferOrder.TRANSFER_STATE_NOT_COMPLETED);
			returned.put(PaymentConstant.TRANSFER_STATE_MESSAGE, successDtlArr[5]);
			returned.put(PaymentConstant.OPPOSITE_ORDERNO,successDtlArr[6]);
			returned.put(PaymentConstant.TRANSFER_ACCOUNT, successDtlArr[1]);
		}
	
		return returned;
	}

	@Override
	public void backendResponse(Map<String, Object> params,
			HttpServletResponse response, boolean isSccess) {
		if (isSccess)
			super.responseAndWrite(response, "success");
		else
			super.responseAndWrite(response, "fail");
	}

	@Override
	public boolean transferQuery(Platform platform,
			TransferOrder order, Map<String, Object> extParams) {
		
		TransferModel transferModel = new TransferModel();
		transferModel.setMerchantId(order.getMerchantId());
		transferModel.setOrderNo(order.getOrderNo());
		transferModel.setReceiveOrderNo(order.getReceiveOrderNo());
		transferModel.setAccount(order.getReceiveAccount());
		transferModel.setAccountInfo(order.getReceiveAccountInfo());
		transferModel.setReason(order.getReason());
		transferModel.setMoney(String.valueOf(order.getMoney()));
		
		boolean transferRequest = this.transferRequest(platform, transferModel, null);
		return transferRequest;
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
		String sign = this.encodeRsa(encryptParams);

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
		String alipayNotifyURL = platform.getPayCheckUrl() + "?service=notify_verify&partner=" + partner + "&notify_id=" + notify_id;
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
			PaymentOrder paymentOrder = paymentOrderService.queyrOrderByOppositeOrderNo(ObjectUtils.toString(detailStr[0]));//原付款支付宝交易号
			
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