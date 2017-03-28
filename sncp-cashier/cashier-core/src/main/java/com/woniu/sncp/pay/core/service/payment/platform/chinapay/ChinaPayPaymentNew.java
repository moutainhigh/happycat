package com.woniu.sncp.pay.core.service.payment.platform.chinapay;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
 * 新版银联电子支付 PAYMENT_1032
 * 
 * 对方无订单号
 * 我方订单号经过getChinaPayOrderId处理之后的
 * 
 * 货币单位为分，字段左补0
 * 
 * @author fuzl
 *
 */
@Service("chinaPayPaymentNew")
public class ChinaPayPaymentNew extends AbstractPayment {
	
	
	private final static String DATE_FORMAT = "yyyyMMdd";
	private final static String TIME_FORMAT = "HHmmss";
	
	protected KeyStore keyStore;
	
	protected X509Certificate verifyCert;
	
	protected PrivateKey priKey;
	
	protected PublicKey pubKey;
	
	private final String KEY_VALUE_CONNECT = "=";
	
	private final String MESSAGE_CONNECT = "&";
	
	private final String SIGN512_ALGNAME = "SHA512WithRSA";
	
	private final String SIGN_CERT_TYPE = "PKCS12";
	
	protected final String _charset_encode = "UTF-8";
	

	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		//对接银联电子新版提供的api
		//1.需要签名的域
		Map<String, Object> map = new HashMap<String, Object>();
		for(Map.Entry<String, Object> entry:inParams.entrySet()){
			if(ObjectUtils.equals(entry.getKey(), "Signature")||ObjectUtils.equals(entry.getKey(), PaymentConstant.PAYMENT_PLATFORM)){
				continue;
			}
			map.put(entry.getKey(), entry.getValue());
		}
		//2.不进行签名的域
		List<String> invalidFileds = new ArrayList<String>();
		invalidFileds.add(PaymentConstant.PAYMENT_PLATFORM);
		invalidFileds.add("Signature");
		
		String dataStr = getSignStr(map, invalidFileds, true);
		if(logger.isInfoEnabled()){
			logger.info("银联电子报文签名之前的字符串(不含signature域):{}",dataStr);
		}
		
		String sign = "";
		try {
			byte[] signBytes = null;
			//3.获取签名
			signBytes = sign(dataStr.getBytes(_charset_encode), getPriKey(platform.getPrivateUrl(),platform.getPrivatePassword()), SIGN512_ALGNAME);
	        //4.签名进行Base64编码
			sign = new String(Base64.encodeBase64(signBytes), _charset_encode);
		} catch (Exception e) {
			throw new ValidationException("银联电子支付加密异常"+e);
		}
        if(logger.isInfoEnabled()){
			logger.info("银联电子报文签名之后的字符串:{}" , sign);
		}
		return sign;
	}
	
	/**
	 * 构造签名源串
	 * @param map  需要进行签名map域
	 * @param invalidFields 签名排除的域
	 * @param isSort  是否排序
	 * @return
	 * @throws SecurityException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String getSignStr(Map map, List invalidFields, boolean isSort)
			throws ValidationException {
		StringBuffer param = null;
		List msgList = null;
		if ((map == null) || (map.size() == 0))
			throw new ValidationException("银联电子支付加密异常,map为null");
		try {
			param = new StringBuffer();
			msgList = new ArrayList();
			for (Iterator it = map.keySet().iterator(); it.hasNext();) {
				String key = (String) it.next();
				if ((invalidFields != null) && (invalidFields.contains(key))) {
					continue;
				}
				String value = (String) map.get(key);
				msgList.add(key + KEY_VALUE_CONNECT + value);
			}
			//是否排序
			if (isSort) {
				Collections.sort(msgList);
			}

			for (int i = 0; i < msgList.size(); i++) {
				String msg = (String) msgList.get(i);
				if (i > 0) {
					param.append(MESSAGE_CONNECT);
				}
				param.append(msg);
			}
			return param.toString();
		} catch (Exception e) {
			throw new ValidationException("银联电子支付加密异常,sign error");
		}
	}
	
	/**
	 * 获取私钥
	 * @return
	 * @throws SecurityException
	 */
	@SuppressWarnings("rawtypes")
	public PrivateKey getPriKey(String privFilePath,String merchantPwd) throws ValidationException {
		try {
			// 组装keyStore  交易证书路径,交易证书密码,交易证书的密钥容器格式
			keyStore = getKeyStore(privFilePath, merchantPwd, SIGN_CERT_TYPE);
			
			Enumeration aliasenum = keyStore.aliases();
			String keyAlias = null;
			if (aliasenum.hasMoreElements()) {
				keyAlias = (String) aliasenum.nextElement();
			}
			priKey = (PrivateKey) keyStore.getKey(keyAlias, merchantPwd.toCharArray());
			return priKey;
		} catch (Exception e) {
			throw new ValidationException("银联电子支付获取私钥异常");
		}
	}
	
	/**
	 * 获取keyStore
	 * @param signFile
	 * @param merchantPwd
	 * @param signFileType
	 * @return
	 */
	private KeyStore getKeyStore(String signFile, String merchantPwd,
			String signFileType) {
		KeyStore ks = null;
		try {
			if ("JKS".equals(signFileType)) {
				ks = KeyStore.getInstance(signFileType, "SUN");
			} else if ("PKCS12".equals(signFileType)) {
				Security.addProvider(new BouncyCastleProvider());
				ks = KeyStore.getInstance(signFileType);
			} else {
				throw new ValidationException("银联电子支付getKeyStore异常");
			}
			FileInputStream fis = new FileInputStream(signFile);
			char[] nPassword = null;
			nPassword = StringUtils.isEmpty(merchantPwd) ? null : merchantPwd.toCharArray();
			ks.load(fis, nPassword);
			fis.close();
		} catch (Exception e) {
			if (((e instanceof KeyStoreException))
					&& ("PKCS12".equals(signFileType)))
				Security.removeProvider("BC");
		}
		return ks;
	}
	
	/**
	 * 私钥签名
	 * @param dataBytes
	 * @param priKey
	 * @param algName
	 * @return
	 */
	private byte[] sign(byte[] dataBytes, PrivateKey priKey,
			String algName) throws Exception{
		Signature signature = Signature.getInstance(algName);
	    signature.initSign(priKey);
	    signature.update(dataBytes);
	    byte[] signByte = signature.sign();
	    return signByte;
	}
	
	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		
		Map<String, Object> params = new HashMap<String, Object>();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		SimpleDateFormat _sdf = new SimpleDateFormat(TIME_FORMAT);
		
		// 1.拼装参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		params.put("Version", "20140728");//固定值：20140728
		params.put("MerId", platform.getMerchantNo());//由ChinaPay分配的15位定长数字，用于确认商户身份
		params.put("MerOrderNo", paymentOrder.getOrderNo().replaceAll("-", ""));//必填，变长 32位，同一商户同一交易日期内不可重复
		
		params.put("TranDate", sdf.format(paymentOrder.getCreateDate()));//商户提交交易的日期，例如交易日期为2015年1月2日，则值为20150102，必填
		params.put("TranTime", _sdf.format(paymentOrder.getCreateDate()));//商户提交交易的时间，例如交易时间10点11分22秒，则值为101122，必填
		
		params.put("OrderAmt", String.valueOf((new BigDecimal(paymentOrder.getMoney().toString())).multiply(new BigDecimal(100)).intValue()));//订单金额 单位：分
		params.put("BusiType", "0001");//业务类型，固定值：0001，必填
		
		params.put("MerBgUrl", platform.getBehindUrl(paymentOrder.getMerchantId()));//商户后台通知地址
		if(StringUtils.isNotEmpty(platform.getFrontUrl(paymentOrder.getMerchantId()))){
			params.put("MerPageUrl", platform.getFrontUrl(paymentOrder.getMerchantId()));//商户前台通知地址
		}
		
		//超时时间配置才传
		if(null != platform.getTransTimeout() && platform.getTransTimeout() >0 ){
			params.put("PayTimeOut", platform.getTransTimeout());//超时时间,分钟
		}
		
		params.put("MerResv", paymentOrder.getOrderNo());//商户私有域，我方订单号
		params.put(PaymentConstant.PAYMENT_PLATFORM, platform);//签名使用 
		params.put("Signature", this.encode(params));//签名 
		params.remove(PaymentConstant.PAYMENT_PLATFORM);//使用完删除
		
		params.put("payUrl", platform.getPayUrl()); // 提交给对方的支付地址
		return params;
	}
	
	/**
	 * 异步回调校验进入
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException,
			DataAccessException, PaymentRedirectException {
		
		Map<String, Object> returned = new HashMap<String, Object>();
		try {
			request.setCharacterEncoding("utf-8");
			//解析 返回报文
			Enumeration<String> requestNames = request.getParameterNames();
			Map<String, Object> resultMap = new HashMap<String, Object>();
			while(requestNames.hasMoreElements()){
				String name = requestNames.nextElement();
				String value = request.getParameter(name);
				value = URLDecoder.decode(value, "UTF-8");
				resultMap.put(name, value);
			}

			String merOrderNo = URLDecoder.decode(request.getParameter("MerOrderNo"), "utf-8");// 商户订单号
			String amount = URLDecoder.decode(request.getParameter("OrderAmt"), "utf-8");//订单金额	单位：分
			String orderstatus = URLDecoder.decode(request.getParameter("OrderStatus"), "utf-8");//订单支付状态  0000为支付成功状态，0001为未支付，其它为失败状态，响应码列表参见附录B
			String merresv = URLDecoder.decode(request.getParameter("MerResv"), "utf-8");//商户自定义信息，json格式
			
			resultMap.put(PaymentConstant.PAYMENT_PLATFORM, platform);//签名使用 
			if(!verify(resultMap)){
				logger.error("银联电子支付密钥校验失败,订单号:"+merresv);
				throw new ValidationException("银联电子支付密钥校验失败");
			}
			
			String wnOrderNo = "";
			if (StringUtils.isNotEmpty(merresv)) {
				wnOrderNo = merresv;
			}
			// 订单查询
			PaymentOrder paymentOrder = paymentOrderService.queryOrder(wnOrderNo);
			Assert.notNull(paymentOrder, "支付订单查询为空,orderNo:" + wnOrderNo);
			
			//金额、状态判断
			BigDecimal _bamount = new BigDecimal(amount);//渠道金额,单位分
			BigDecimal _cpamount = new BigDecimal((new BigDecimal(paymentOrder.getMoney().toString())).multiply(new BigDecimal(100)).intValue());//我方金额,单位元,转换为分
			
			if(_bamount.compareTo(_cpamount) != 0){
				logger.info("银联电子支付校验失败,金额不匹配,订单号:"+wnOrderNo +",我方:" + _cpamount + ",对方:" + _bamount);
				throw new ValidationException("银联电子支付校验失败");
			}
			
			if("0000".equals(orderstatus)){
				logger.info("银联电子异步通知返回支付成功,订单号:{}",wnOrderNo);
				returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
			}else { // 未支付
				logger.info("银联电子异步通知返回未支付,订单号:{}",wnOrderNo);
				returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
			}
			
			returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
			returned.put(PaymentConstant.OPPOSITE_ORDERNO, merOrderNo);
			returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf(NumberUtils.toInt(amount)));
			
		} catch (UnsupportedEncodingException e) {
			logger.error("银联电子支付校验失败"+e);
			throw new ValidationException("银联电子支付校验失败");
		}
		return returned;
	}
	
	/**
	 * 校验回调签名
	 * @param inParams
	 * @return
	 */
	private boolean verify(Map<String, Object> inParams) {
		boolean result = false;
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		//1.需要签名的域
		Map<String, Object> map = new HashMap<String, Object>();
		for(Map.Entry<String, Object> entry:inParams.entrySet()){
			if(ObjectUtils.equals(entry.getKey(), "Signature") || ObjectUtils.equals(entry.getKey(), PaymentConstant.PAYMENT_PLATFORM)){
				continue;
			}
			map.put(entry.getKey(), entry.getValue());
		}
		
		String signField = ObjectUtils.toString(inParams.get("Signature"));
		if(logger.isInfoEnabled()){
			logger.info("银联电子报文验签之前的签名串:{}", signField);
		}
		//2.不进行签名的域
		List<String> invalidFileds = new ArrayList<String>();
		invalidFileds.add("Signature");
		
		String dataStr = getSignStr(map, invalidFileds, true);
		if(logger.isInfoEnabled()){
			logger.info("银联电子报文验签之前的字符串(不含signature域):{}", dataStr);
		}
		
		PublicKey pubKey = getPubKey(platform.getPublicUrl());
		if (pubKey == null) {
	        throw new ValidationException("获取到的验签公钥证书为空");
	    }
		
		try {
			byte[] signBytes = Base64.decodeBase64(signField.getBytes(_charset_encode));
			result = verify(dataStr.getBytes(_charset_encode), signBytes,  pubKey, SIGN512_ALGNAME);
		} catch (Exception e) {
			throw new ValidationException("银联电子支付加密异常"+e);
		}
        
		return result;
	}
	
	/**
	 * 公钥验签
	 * @param bytes
	 * @param dataBytes
	 * @param pubKey
	 * @param sigAlgName
	 * @return
	 * @throws Exception
	 */
	private boolean verify(byte[] bytes, byte[] dataBytes, PublicKey pubKey,
			String sigAlgName) throws Exception {
		java.security.Signature signature = java.security.Signature.getInstance(sigAlgName);
	    signature.initVerify(pubKey);
	    signature.update(bytes);
	    return signature.verify(dataBytes);
	}


	/**
	 * 获取验签证书
	 * @return
	 */
	private PublicKey getPubKey(String pubFilePath) {
		if (StringUtils.isEmpty(pubFilePath)) {
			throw new ValidationException("银联电子支付验签异常");
		}
		CertificateFactory cf = null;
		FileInputStream in = null;
		try {
			cf = CertificateFactory.getInstance("X.509");
			in = new FileInputStream(pubFilePath);
			verifyCert = (X509Certificate) cf.generateCertificate(in);
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		} catch (Exception e) {
			e.printStackTrace();
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			throw new ValidationException("银联电子支付验签异常");
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		if (pubKey != null) {
	      return pubKey;
	    }
	    if (verifyCert == null) {
	      return null;
	    }
		
		pubKey = verifyCert.getPublicKey();
	    return pubKey;
	}
	
	/**
	 * 订单查询验证进入
	 */
	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		Map<String, Object> outParams = new HashMap<String, Object>();
		// 封装请求数据
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("Version", "20140728");//固定值：20140728
		params.put("MerId", platform.getMerchantNo());//由ChinaPay分配的15位定长数字，用于确认商户身份
		params.put("MerOrderNo",  paymentOrder.getOrderNo().replaceAll("-", ""));//商户订单号,替换掉-
		
		params.put("TranDate", sdf.format(paymentOrder.getCreateDate()));//格式：yyyyMMdd
		params.put("TranType", "0502");//交易类型
		params.put("BusiType", "0001");//业务类型
		
		params.put(PaymentConstant.PAYMENT_PLATFORM, platform);//签名 
		params.put("Signature", this.encode(params));//签名 
		params.remove(PaymentConstant.PAYMENT_PLATFORM);//使用完删除
		
		// 查询交易
		String response = PayCheckUtils.postRequst(platform.getPayCheckUrl(), params, 2000, _charset_encode, "银联电子支付订单查询接口");
		
		// 解析数据
		Map<String,Object> respMap = convertStr2Map(response);
		
		// 数据校验
		String respCode = ObjectUtils.toString(respMap.get("respCode"));
		if(StringUtils.isEmpty(respCode)){
			logger.info("交易查询签名字段为空，请求参数:{},返回数据:{}",params,response);
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return outParams;
		}
		
		// 查询返回签名校验
		respMap.put(PaymentConstant.PAYMENT_PLATFORM, platform);//签名使用 
		if(!verify(respMap)){
			logger.info("交易查询数字签名不正确，请求参数:{},返回数据:{}",params,response);
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return outParams;
		}
		
		// 验证状态并返回数据
		String status = ObjectUtils.toString(respMap.get("OrderStatus"));
		String orderAmount = String.valueOf(NumberUtils.toInt(String.valueOf(respMap.get("OrderAmt"))));// 订单金额
		
		// 订单包含退款金额
		Integer refundSumAmount = NumberUtils.toInt(String.valueOf(respMap.get("RefundSumAmount")));// 已退款金额
		if(refundSumAmount>0){
			logger.info("交易查询参数不正确，请求参数:{},返回数据:{}",params,response);
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return outParams;
		}
		
		if("0000".equals(status)){
			String oppositeOrderNo = String.valueOf(respMap.get("MerOrderNo"));
			outParams.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo); // 对方订单号
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
			outParams.put(PaymentConstant.OPPOSITE_MONEY, orderAmount); // 总金额，对方传回的单位已经是分
		} else {
			logger.info("交易查询应答未支付 ,请求参数:{},返回数据:{}",params,response);
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}
		
		return outParams;
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
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return StringUtils.trim(request.getParameter("MerResv"));
	}

	/**
	 * 响应内容格式转换
	 * @param str
	 * @return
	 */
	public static Map<String,Object> convertStr2Map(String str){
		String [] items = null;
		if(str.contains("<body>")){
			items = str.split("<body>")[1].split("&");
		} else {
			items = str.split("&");
		}
		Map<String,Object> p = new HashMap<String,Object>();
		for (String item : items) {
			String [] pair = item.split("=");
			if(pair.length == 2) {
				p.put(pair[0], pair[1]);
			}
		}
		
		return p;
	}

}
