package com.woniu.sncp.pay.core.service.payment.platform.chinapay;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.http.PayCheckUtils;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;

/**
 * 银联电子支付
 * 
 * 对方无订单号
 * 我方订单号经过getChinaPayOrderId处理之后的
 * 
 * 货币单位为分，字段左补0
 * 
 * @author luzz
 *
 */
@Service("chinaPayPayment")
public class ChinaPayPayment extends AbstractPayment {
	
	
	private final static String DATE_FORMAT = "yyyyMMdd";
	protected final String _charset_encode = "UTF-8";
	

	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		String keyFilePath = ObjectUtils.toString(inParams.get("priKeyFilePath"));
		
		chinapay.PrivateKey key=new chinapay.PrivateKey();
		String merId = ObjectUtils.toString(inParams.get("MerId"));
		String ordId = ObjectUtils.toString(inParams.get("OrdId"));
		String transAmt = ObjectUtils.toString(inParams.get("TransAmt"));
		String curyId = ObjectUtils.toString(inParams.get("CuryId"));
		String transDate = ObjectUtils.toString(inParams.get("TransDate"));
		String transType = ObjectUtils.toString(inParams.get("TransType"));
		String priv1 = ObjectUtils.toString(inParams.get("Priv1"));
		
		if (!key.buildKey(merId,0,keyFilePath)){
			logger.error("银联电子生成校验串时证书异常，参数:{}",inParams);
			throw new ValidationException("银联电子支付加密异常");
		}
		
		chinapay.SecureLink secureLink = new chinapay.SecureLink (key);
		
		String source = merId + ordId + transAmt + curyId + transDate + transType + priv1;
		logger.info("银联电子支付订单号:{} 加密前串:{}",priv1,source);
		String chkValue = secureLink.Sign(source) ;
		return chkValue;
	}
	
	public boolean verify(Map<String, String> inParams)
			throws ValidationException {
		String publicKeyFilePath = inParams.get("pubKeyFilePath");
		
		chinapay.PrivateKey key=new chinapay.PrivateKey();
		logger.info("inParams:"+inParams);
		String merId = inParams.get("merId");
		String orderno = inParams.get("orderno");
		String amount = inParams.get("amount");
		String currencycode = inParams.get("currencycode");
		String transdate = inParams.get("transdate");
		String transtype = inParams.get("transtype");
		String status = inParams.get("status");
		String priv1 = inParams.get("Priv1");

		String chkValue = inParams.get("chkValue");
		if (!key.buildKey("999999999999999",0,publicKeyFilePath)){
			logger.error("银联电子校验串验证时证书异常，参数:{}",inParams);
			throw new ValidationException("银联电子支付加密异常");
		}
		
		chinapay.SecureLink secureLink = new chinapay.SecureLink (key);
		
		return secureLink.verifyTransResponse(merId, orderno, amount, currencycode, transdate, transtype, status, chkValue);
	}
	
	public boolean verifyResponse(Map<String, String> inParams,Platform platform)
			throws ValidationException {
		chinapay.PrivateKey key=new chinapay.PrivateKey();
		logger.info("inParams:"+inParams);
		String merId = inParams.get("merid");
		String orderno = inParams.get("orderno");
		String amount = inParams.get("amount");
		String currencycode = inParams.get("currencycode");
		String transdate = inParams.get("transdate");
		String transtype = inParams.get("transtype");
		String status = inParams.get("status");
		String priv1 = inParams.get("Priv1");

		String chkValue = inParams.get("checkvalue");
		if (!key.buildKey("999999999999999",0,platform.getPublicUrl())){
			logger.error("银联电子校验串验证时证书异常，参数:{}",inParams);
			throw new ValidationException("银联电子支付加密异常");
		}
		
		chinapay.SecureLink secureLink = new chinapay.SecureLink (key);
		
		return secureLink.verifyTransResponse(merId, orderno, amount, currencycode, transdate, transtype, status, chkValue);
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		Map<String, Object> params = new HashMap<String, Object>();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		
		// 1.拼装参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		params.put("MerId", platform.getMerchantNo());	
		params.put("OrdId", getChinaPayOrderId(paymentOrder.getOrderNo()));//16 位长度 ,我们最少27位，订单号放Priv1 对账问题
		
		params.put("TransAmt", String.format("%012d", (new BigDecimal(paymentOrder.getMoney().toString())).multiply(new BigDecimal(100)).intValue()));//交易金额  长度为12个字节的数字串  单位为分
		params.put("CuryId", "156");
		
		params.put("TransDate", sdf.format(paymentOrder.getCreateDate()));//订单交易日期，8 位长度，必填
		params.put("TransType", "0001");//交易类型，4 位长度，必填
		params.put("Version", "20070129");//支付接入版本号，必填
		params.put("BgRetUrl", platform.getBehindUrl(paymentOrder.getMerchantId()));
		params.put("PageRetUrl", platform.getFrontUrl(paymentOrder.getMerchantId()));
		params.put("Priv1", paymentOrder.getOrderNo());//商户私有域 放订单号
		
		params.put("priKeyFilePath", platform.getPrivateUrl());
		params.put("ChkValue", this.encode(params));
		
		params.put("payUrl", platform.getPayUrl()); // 提交给对方的支付地址
		
		return params;
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException,
			DataAccessException, PaymentRedirectException {

		String version = request.getParameter("version");
		String merid = request.getParameter("merid");
		String orderno = request.getParameter("orderno");
		String amount = request.getParameter("amount");
		String curyId = request.getParameter("currencycode");
		String transdate = request.getParameter("transdate");
		String transtype = request.getParameter("transtype");
		String status = request.getParameter("status");//只有"1001"的时候才为交易成功，其他均为失败
		String checkvalue = request.getParameter("checkvalue");

		String priv1 = StringUtils.trim(request.getParameter("Priv1"));
		
		Map<String, String> inParams = new HashMap<String, String>();
		inParams.put("version", version);
		inParams.put("merId", merid);
		inParams.put("orderno", orderno);
		inParams.put("amount", amount);
		inParams.put("currencycode", curyId);
		inParams.put("transdate", transdate);
		inParams.put("transtype", transtype);
		inParams.put("status", status);
		inParams.put("priv1", priv1);
		inParams.put("chkValue", checkvalue);
		inParams.put("pubKeyFilePath", platform.getPublicUrl());
		
		if(!verify(inParams)){
			logger.error("银联电子支付密钥校验失败,订单号:"+priv1);
			throw new ValidationException("银联电子支付密钥校验失败");
		}
		
		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(priv1);
		Assert.notNull(paymentOrder, "支付订单查询为空,orderNo:" + priv1);
		
		Map<String, Object> returned = new HashMap<String, Object>();
		if("1001".equals(status)){
			logger.info("银联电子异步通知返回支付成功,订单号:{}",priv1);
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		}else { // 未支付
			logger.info("银联电子异步通知返回未支付,订单号:{}",priv1);
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}
		
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, orderno);
		returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf(NumberUtils.toInt(amount)));
		
		return returned;
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		HttpServletRequest request = (HttpServletRequest) inParams.get("request");
		if (isImprestedSuccess){
			response.setStatus(HttpServletResponse.SC_OK);
			super.responseAndWrite(response, "");
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//http code 非200 继续回调
			super.responseAndWrite(response, request.getParameter("respMsg"));
		}
	}

	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		Map<String, Object> outParams = new HashMap<String, Object>();
		// 封装请求数据
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("Version", "20060831");	
		params.put("MerId", platform.getMerchantNo());
		params.put("TransType", "0001");
		params.put("OrdId", getChinaPayOrderId(paymentOrder.getOrderNo()));
		params.put("TransDate", sdf.format(paymentOrder.getCreateDate()));
		params.put("Resv", paymentOrder.getOrderNo());
		params.put("ChkValue", checkOrderIsPayedEncode(params,platform));
		
		// 查询交易
		String response = PayCheckUtils.postRequst(platform.getPayCheckUrl(), params, 5000, _charset_encode, "银联电子支付订单查询接口");
		
		// 解析数据
		Map<String,String> respMap = convertStr2Map(response);
		
		// 数据校验
		String checkvalue = respMap.get("checkvalue");
		if(StringUtils.isEmpty(checkvalue)){
			logger.info("交易查询签名字段为空，请求参数:{},返回数据:{}",params,response);
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return outParams;
		}
		
		respMap.put("pubKeyFilePath", platform.getPublicUrl());
		if(!verifyResponse(respMap,platform)){
			logger.info("交易查询数字签名不正确，请求参数:{},返回数据:{}",params,response);
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return outParams;
		}
		
		// 验证状态并返回数据
		String status = respMap.get("status");
		if("1001".equals(status)){
			String oppositeOrderNo = String.valueOf(respMap.get("orderno"));
			String orderAmount = String.valueOf(NumberUtils.toInt(respMap.get("amount")));
			outParams.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo); // 对方订单号
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
			outParams.put(PaymentConstant.OPPOSITE_MONEY, orderAmount); // 总金额，对方传回的单位已经是分
		} else {
			logger.info("交易查询应答未支付 ,请求参数:{},返回数据:{}",params,response);
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}
		
		return outParams;
	}
	
	private String checkOrderIsPayedEncode(Map<String, Object> inParams,Platform platform)
			throws ValidationException {
		chinapay.PrivateKey key=new chinapay.PrivateKey();
		String merId = ObjectUtils.toString(inParams.get("MerId"));
		String ordId = ObjectUtils.toString(inParams.get("OrdId"));
		String transDate = ObjectUtils.toString(inParams.get("TransDate"));
		String transType = ObjectUtils.toString(inParams.get("TransType"));
		String priv1 = ObjectUtils.toString(inParams.get("Priv1"));
		
		if (!key.buildKey(merId,0,platform.getPrivateUrl())){
			logger.error("银联电子生成校验串时证书异常，参数:{}",inParams);
			throw new ValidationException("银联电子支付加密异常");
		}
		
		chinapay.SecureLink secureLink = new chinapay.SecureLink (key);
		
		String source = merId + transDate + ordId + transType ;
		logger.info("银联电子支付订单号:{} 加密前串:{}",priv1,source);
		String chkValue = secureLink.Sign(source) ;
		return chkValue;
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return StringUtils.trim(request.getParameter("Priv1"));
	}

	//20140527-213-007-0000357401
	//1405270000357401
	public String getChinaPayOrderId(String orderNo){
		String [] arrStr = orderNo.split("-");
		String chinaPayOrder = "";
		if(arrStr.length == 4){
			chinaPayOrder = arrStr[0].substring(2)+arrStr[3];
		}
		
		if(chinaPayOrder.length()>16){
			chinaPayOrder = chinaPayOrder.substring(0, 16);
		}
		logger.info("我方订单号:{},转换为银联电子订单号:{}",orderNo,chinaPayOrder);
		return chinaPayOrder;
	}
	
	public static Map<String,String> convertStr2Map(String str){
		String [] items = null;
		if(str.contains("<body>")){
			items = str.split("<body>")[1].split("&");
		} else {
			items = str.split("&");
		}
		Map<String,String> p = new HashMap<String,String>();
		for (String item : items) {
			String [] pair = item.split("=");
			if(pair.length == 2) {
				p.put(pair[0], pair[1]);
			}
		}
		
		return p;
	}

}
