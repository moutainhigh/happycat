package com.woniu.sncp.pay.core.service.payment.platform.icbc;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.icbc.helper.B2CReq;
import com.woniu.sncp.pay.core.service.payment.platform.icbc.helper.B2CRes;
import com.woniu.sncp.pay.core.service.payment.platform.icbc.helper.BankInfo;
import com.woniu.sncp.pay.core.service.payment.platform.icbc.helper.Custom;
import com.woniu.sncp.pay.core.service.payment.platform.icbc.helper.JaxbMapper;
import com.woniu.sncp.pay.core.service.payment.platform.icbc.helper.Message;
import com.woniu.sncp.pay.core.service.payment.platform.icbc.helper.OrderInfo;
import com.woniu.sncp.pay.core.service.payment.platform.icbc.helper.OrderInfo.SubOrderInfo;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.web.IpUtils;

import cn.com.infosec.icbc.ReturnValue;

@Component("icbcINBSEPayment")
public class ICBCINBSEPayment extends AbstractPayment {

	private final static String INTERFACE_NAME = "ICBC_PERBANK_B2C";
	private final static String INTERFACE_VER = "1.0.0.11";
	private final static Charset PAY_DFT_CHARSET = Charset.forName("GBK");
	//信用卡分期
	private final static int[] INS_TIMES_ARR = new int[] {1, 3, 6, 9, 12, 18, 24};
	private final static String XML_HEADER = "<?xml version=\"1.0\" encoding=\"GBK\" standalone=\"no\"?>";
	
	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		return "";
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		int installmentTimes = MapUtils.getIntValue(inParams, "stageNum");
		int index = Arrays.binarySearch(INS_TIMES_ARR, installmentTimes);
		if (index < 0) {
			throw new IllegalArgumentException("installmentTimes error,信用分期期数不被支持");
		}
		
		//0-仅允许使用借记卡支付  1-仅信用卡支付  2-ALL
		String creditType = "1";
		//直接付款
		if (installmentTimes == 1) {
			creditType = "2";
		}
		
		String tranDataXml = getTranDataXml(paymentOrder, platform,
				installmentTimes, creditType, MapUtils.getString(inParams, "productName"));
		
		byte[] tranDataBytes = tranDataXml.getBytes(PAY_DFT_CHARSET);
		String tranDataBase64 = new String(ReturnValue.base64enc(tranDataBytes));
		
		String merSignMsg = "";
		String merCert = "" ;
		try{
			byte[] bs = IOUtils.toByteArray(new FileInputStream(platform.getPrivateUrl()));
			byte[] signature = ReturnValue.base64enc(ReturnValue.sign(tranDataBytes,
					tranDataBytes.length, bs, platform.getPrivatePassword().toCharArray()));
			merSignMsg = new String(signature, CharEncoding.ISO_8859_1);
			
			byte[] bsc = IOUtils.toByteArray(new FileInputStream(platform.getPublicUrl()));
			merCert = new String(ReturnValue.base64enc(bsc));
			
		} catch(Exception e) {
			logger.error("中国工商银行-签名失败  " + e.getMessage());
			throw unchecked(e);
		}
		
        Map<String, Object> params = new HashMap<String, Object>(5);
        //接口名称
        params.put("interfaceName", INTERFACE_NAME);
        //接口版本号
        params.put("interfaceVersion", INTERFACE_VER); 
        //交易数据
        params.put("tranData", tranDataBase64);
        //订单签名数据
        params.put("merSignMsg", merSignMsg);
        //商城证书公钥
        params.put("merCert", merCert);
        //支付地址
        params.put("payUrl", platform.getPayUrl());
        params.put("acceptCharset", PAY_DFT_CHARSET.name());
		return params;
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException, DataAccessException,
			PaymentRedirectException {
		
		String notifyDataBase64 = request.getParameter("notifyData").trim();
		String signMsg = request.getParameter("signMsg").trim();
	    String merVAR = request.getParameter("merVAR");
	    String bankCert = platform.getQueryPublicUrl();
	    byte[] notifyDataB = ReturnValue.base64dec(notifyDataBase64.getBytes(PAY_DFT_CHARSET));
		String notifyData = new String(notifyDataB, PAY_DFT_CHARSET);
		logger.info("notifyData:" + notifyData);
		byte[] signature = ReturnValue.base64dec(signMsg.getBytes());
		boolean verify = false;
		try {
			byte[] bs = IOUtils.toByteArray(new FileInputStream(bankCert));
			int result = ReturnValue.verifySign(notifyDataB, notifyDataB.length, bs, signature);
			verify = result == 0;
		} catch(Exception e) {
			logger.error("中国工商银行-验证签名失败  " + e.getMessage());
			throw new ValidationException("中国工商银行信用卡分期支付签名验证失败");
		}
		if (!verify) {
			logger.info("verify failed.");
			throw new ValidationException("中国工商银行信用卡分期支付签名验证失败");
		}
		logger.info("verify success!");
		
		B2CRes res = JaxbMapper.fromXml(notifyData, B2CRes.class);
		Map<String, Object> returneMap = new HashMap<String, Object>();
		
		BankInfo bankInfo = res.getBank();
		//判断交易状态
		if ("1".equals(bankInfo.getTranStat())) {
			logger.info("中国工商银行信用卡分期支付返回支付成功");
			logger.info(bankInfo.getComment());
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else if ("3".equals(bankInfo.getTranStat())) {
			//可疑
			logger.info("中国工商银行信用卡分期支付返回未支付");
			logger.info(bankInfo.getComment());
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		} else {
			//失败
			logger.info("中国工商银行信用卡分期支付返回未支付");
			logger.info(bankInfo.getComment());
			returneMap.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_FAILED);
		}
		
		OrderInfo orderInfo = res.getOrderInfo();
		orderInfo.getMerID();
		List<SubOrderInfo> subOrderList = orderInfo.getSubOrderInfoList();
		if (subOrderList == null || subOrderList.size() <= 0) {
			logger.info("获取订单数据错误");
			throw new ValidationException("中国工商银行信用卡分期支付获取订单数据错误");
		}
		//目前只支持一个子订单
		SubOrderInfo subOrderInfo = subOrderList.get(0);
		
		String orderNo = subOrderInfo.getOrderid();
		String orderSeq = subOrderInfo.getTranSerialNo();
		String merAcct = subOrderInfo.getMerAcct();
		int amount = subOrderInfo.getAmount();
		
		if (!StringUtils.equals(merAcct, platform.getManageUser())) {
			logger.info("中国工商银行信用卡分期支付商家账户不匹配 expected[{}] actual[{}]",merAcct, platform.getMerchantNo());
			throw new ValidationException("中国工商银行信用卡分期支付商家账户不匹配");
		}
		
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		
		String sign = MD5Encrypt.encrypt(paymentOrder.getOrderNo() + platform.getBackendKey());
		if (!sign.equals(StringUtils.upperCase(merVAR))) {
			logger.info("notify param [merVAR={}],我方原文[{}]", merVAR, sign);
			throw new ValidationException("中国工商银行支付订单orderNo + key校验失败");
		}
		
		Assert.notNull(paymentOrder, "中国工商银行支付订单查询为空,orderNo:" + orderNo);
		
		returneMap.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returneMap.put(PaymentConstant.OPPOSITE_ORDERNO, orderSeq);
		returneMap.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf(amount));
		
		//不验证imprestMode,直接取订单中imprestMode
		returneMap.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		return returneMap;
	}


	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		//对方平台没有验证
		return Collections.emptyMap();
	}
	
	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		if (isImprestedSuccess){
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/html;charset=GBK;");
			
			Long merchantid = MapUtils.getLong(inParams, "merchantid");
			String orderNo = MapUtils.getString(inParams, PaymentConstant.ORDER_NO);
			Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
			super.responseAndWrite(response, platform.getFrontUrl(merchantid) + "?orderNo=" + orderNo);
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//http code 非200 继续回调
		}
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return StringUtils.trim(request.getParameter("orderNo"));
	}
	
	/**
	 * B2C请求交易参数 
	 * @param params
	 * @param paymentOrder
	 * @param platform
	 * @return xml String
	 */
	private String getTranDataXml(PaymentOrder paymentOrder, Platform platform,
			int installmentTimes, String creditType, String goodsName) {
		B2CReq req = new B2CReq();
		req.setInterfaceName(INTERFACE_NAME);
		req.setInterfaceVersion(INTERFACE_VER);
		
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setCurType("001");
		orderInfo.setOrderDate(DateFormatUtils.format(paymentOrder.getCreate(), "yyyyMMddHHmmss"));
		orderInfo.setMerID(platform.getMerchantNo());
		
		SubOrderInfo subOrderInfo = new SubOrderInfo();
		subOrderInfo.setOrderid(paymentOrder.getOrderNo());
		//单位 分
		subOrderInfo.setAmount(NumberUtils.createBigDecimal(paymentOrder.getMoney() + "")
				.multiply(NumberUtils.createBigDecimal("100")).intValue());
		//分期数
		subOrderInfo.setInstallmentTimes(installmentTimes);
		subOrderInfo.setMerAcct(platform.getManageUser());
		subOrderInfo.setGoodsID("");
		subOrderInfo.setGoodsName(goodsName);
		subOrderInfo.setGoodsNum(paymentOrder.getAmount());
		subOrderInfo.setCarriageAmt("");
		orderInfo.addSubOrderInfo(subOrderInfo);
		
		Custom custom = new Custom();

		Message message = new Message();
		message.setCreditType(creditType);
		message.setNotifyType("HS");
		message.setResultType(1);
		message.setMerURL(platform.getBehindUrl(paymentOrder.getMerchantId()));
		String merReference = "";
		/*try {
			URL behindUrl = new URL(platform.getBehindUrl());
			merReference = behindUrl.getHost();
		} catch (MalformedURLException e) {
			logger.error("get URL ["+ platform.getBehindUrl() +"] host error:" + e.getMessage());
			throw unchecked(e); 
		}*/
		message.setMerReference(merReference);
		message.setMerCustomIp(IpUtils.longToIp(paymentOrder.getIp()));
		
		message.setMerCustomID(StringUtils.defaultIfBlank(paymentOrder.getUserName(), ""));
		message.setMerCustomPhone("");
		message.setGoodsAddress("");
		message.setMerOrderRemark("");
		message.setMerHint("");
		//我方验证 支付平台原样返回
		message.setMerVAR(MD5Encrypt.encrypt(paymentOrder.getOrderNo() + platform.getBackendKey()));
		
		req.setOrderInfo(orderInfo);
		req.setCustom(custom);
		req.setMessage(message);
		return XML_HEADER + JaxbMapper.toXml(req, PAY_DFT_CHARSET.name(), Boolean.FALSE);
	}
	
	protected static RuntimeException unchecked(Throwable ex) {
		if (ex instanceof RuntimeException) {
			return (RuntimeException) ex;
		} else {
			return new RuntimeException(ex);
		}
	}

}
