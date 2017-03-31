package com.woniu.sncp.pay.core.service.payment.platform.unionpay;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.date.DateUtils;
import com.woniu.sncp.pay.common.utils.http.PayCheckUtils;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.tools.IpUtils;

/**
 * 银联在线支付 PC端
 * 
 *  支付接口      
 *  异步通知      
 *  前台跳转      
 *  支付验证      
 * 
 * @author luzz
 *
 */
@Service("unionPayPayment")
public class UnionPayPayment extends AbstractPayment {

	protected final String _charset_encode = "UTF-8";
	private final static String DATE_FORMAT = "yyyyMMddHHmmss";
	
	private final static String CURRENCY_RMB = "156";
	private final static String TRANS_TYPE_CONSUME = "01";//消费交易类型
	
	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		String priKey = (String) inParams.get("priKey");
		
		TreeMap<String, Object> treeMap = new TreeMap<String, Object>(inParams);
		Iterator<String> iter = treeMap.keySet().iterator();
		StringBuffer sb = new StringBuffer();
		while (iter.hasNext()) {
			String name = (String) iter.next();
			
			if("signMethod".equalsIgnoreCase(name) || "signature".equalsIgnoreCase(name) || "priKey".equalsIgnoreCase(name)){
				continue;
			}
			
			sb.append(name).append("=").append(String.valueOf(treeMap.get(name))).append("&");
		}
		String beforeEncode = sb.toString()+MD5Encrypt.encrypt(priKey, _charset_encode).toLowerCase();
		String sign = MD5Encrypt.encrypt(beforeEncode, _charset_encode);
		inParams.remove("priKey");
		logger.info("source:{},sign:{}",beforeEncode,sign);
		return sign;
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		Map<String, Object> params = new HashMap<String, Object>();
		
		// 1.拼装参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		params.put("version", "1.0.0");	
		params.put("charset", _charset_encode);
		params.put("signMethod", "MD5");
		params.put("transType", TRANS_TYPE_CONSUME);//交易类型
		params.put("merAbbr", "苏州蜗牛数字科技股份有限公司");//商户名称
		params.put("merId", platform.getMerchantNo());//商户代码
		params.put("backEndUrl", platform.getBehindUrl(paymentOrder.getMerchantId()));
		params.put("frontEndUrl", platform.getFrontUrl(paymentOrder.getMerchantId()));
		params.put("orderTime", sdf.format(paymentOrder.getCreate()));
		params.put("orderNumber", paymentOrder.getOrderNo());
		params.put("commodityName", StringUtils.trim((String) inParams.get("productName")));
		BigDecimal money = new BigDecimal(paymentOrder.getMoney().toString());
		params.put("orderAmount", money.multiply(new BigDecimal(100)).intValue());
		params.put("orderCurrency", CURRENCY_RMB);//人民币 156
//		params.put("transTimeout", 3600000);//1小时超时,单位毫秒
		////表单号:31586 modified by fuzl@snail.com
		Map<String, Object> merReserved = null;
		if(null != platform.getTransTimeout() && platform.getTransTimeout() >0 ){
			merReserved = new HashMap<String, Object>();
			String payTimeout = DateUtils.format(
					org.apache.commons.lang.time.DateUtils.addMinutes(
							paymentOrder.getCreate(), platform.getTransTimeout().intValue()), DATE_FORMAT);
			merReserved.put("orderTimeoutDate", payTimeout);
			params.put("merReserved", merReserved);//单位YYYYMMDDhhmmss
		}
		
		params.put("customerIp", IpUtils.longToIp(paymentOrder.getIp()));
		
		params.put("priKey", platform.getPayKey());
		params.put("signature", this.encode(params));
		
		params.put("payUrl", platform.getPayUrl()); // 提交给对方的支付地址
		
		return params;
	}
	
	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException,
			DataAccessException, PaymentRedirectException {
		
		Map<String, Object> source = new HashMap<String, Object>();
		Map<String, Object> requestParams = request.getParameterMap();
		for (Iterator<Entry<String, Object>> keyValuePairs = requestParams.entrySet().iterator(); keyValuePairs
				.hasNext();) {
			Map.Entry<String, Object> entry = keyValuePairs.next();
			String key = entry.getKey();
			String value = request.getParameter(key);
			source.put(key,value);
		}
		
		logger.info("银联在线支付后台回调返回："+JsonUtils.toJson(source.toString()));
		
		source.put("priKey", platform.getPayKey());
		
		String uniPaySign = request.getParameter("signature");
		String woniuSign = this.encode(source);
		
		if(!woniuSign.equalsIgnoreCase(uniPaySign)){
			if (logger.isInfoEnabled()) {
				logger.info("==============银联在线支付后台回调加密处理失败=================");
				logger.info("我方加密串：" + woniuSign + ",对方加密串：" + uniPaySign);
				logger.info("==============银联在线支付后台回调加密处理结束=================\n");
			}
			throw new ValidationException("银联在线支付回调接口加密校验失败");
		}
		
		// 订单查询及验证
		String orderNo = String.valueOf(source.get("orderNumber"));
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "银联在线支付订单查询为空,orderNo:" + String.valueOf(source.get("orderNumber")));
		
		Map<String, Object> returned = new HashMap<String, Object>();
		//判断支付状态
		String respCode = String.valueOf(source.get("respCode"));
		String oppositeOrderNo = String.valueOf(source.get("qid"));
		if("00".equalsIgnoreCase(respCode)){ 
			logger.info("银联在线支付返回支付成功，订单号："+orderNo+",对方订单号："+oppositeOrderNo);
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else {
			String respMsg = String.valueOf(source.get("respMsg"));
			logger.info("银联在线支付返回支付失败，订单号："+orderNo+",对方订单号："+oppositeOrderNo+",respCode"+respCode+",respMsg:"+respMsg);
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}
		
		//封装返回数据
		String orderAmount = String.valueOf(source.get("orderAmount"));
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);
		returned.put(PaymentConstant.OPPOSITE_MONEY, orderAmount);
		
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
				
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
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
		params.put("version", "1.0.0");	
		params.put("charset", _charset_encode);
		params.put("signMethod", "MD5");
		params.put("transType", TRANS_TYPE_CONSUME);//交易类型
		params.put("merId", platform.getMerchantNo());//商户代码
		params.put("orderNumber", paymentOrder.getOrderNo());
		params.put("orderTime", sdf.format(paymentOrder.getCreate()));
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
		
		Map<String, Object> respMap = getRespMap(response);
		
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
		
		return outParams;
	}
	
	private Map<String, Object> getRespMap(String str) {
		String regex = "(.*?cupReserved\\=)(\\{[^}]+\\})(.*)";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(str);

		String reserved = "";
		if (matcher.find()) {
			reserved = matcher.group(2);
		}

		String result = str.replaceFirst(regex, "$1$3");
		String[] resArr = result.split("&");
		for (int i = 0; i < resArr.length; i++) {
			if ("cupReserved=".equals(resArr[i])) {
				resArr[i] += reserved;
			}
		}
		
		Map<String, Object> map = new TreeMap<String, Object>();
		for (int i = 0; i < resArr.length; i++) {
			String[] keyValue = resArr[i].split("=");
			map.put(keyValue[0], keyValue.length >= 2 ? resArr[i].substring(keyValue[0].length() + 1) : "");
		}
		return map;
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return request.getParameter("orderNumber");
	}

}
