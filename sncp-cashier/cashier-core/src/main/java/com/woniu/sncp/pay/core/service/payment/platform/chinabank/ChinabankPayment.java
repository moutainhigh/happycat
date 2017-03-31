package com.woniu.sncp.pay.core.service.payment.platform.chinabank;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.bocnet.common.security.PKCS7Tool;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.date.DateUtils;
import com.woniu.sncp.pay.common.utils.http.PayCheckUtils;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;

/**
 * <pre>
 * <h1>中国银行信用卡分期支付</h1>
 * </pre>
 * @author sungs
 *
 */
@Service("chinabankPayment")
public class ChinabankPayment extends AbstractPayment {
	
	protected final String _charset_encode = "UTF-8";
	
	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		String sign = null;
		String orderNo = (String)inParams.get("orderNo");
		String orderTime = (String)inParams.get("orderTime");
		String curCode = (String)inParams.get("curCode");
		String orderAmount = (String)inParams.get("orderAmount");
		String merchantNo = (String)inParams.get("merchantNo");
		String planCode = (String)inParams.get("planCode");
		String planNumber = (String)inParams.get("planNumber");
		String prikeyPath = (String)inParams.get("prikeyPath");//证书路径
		String prikeyPwd = (String)inParams.get("prikeyPwd");//私钥证书密码
		String prikeyPayPwd = (String)inParams.get("prikeyPayPwd");//支付密码
		
		PKCS7Tool tool = null;
		try {
			tool = PKCS7Tool.getSigner(prikeyPath, prikeyPwd, prikeyPayPwd);
		} catch (GeneralSecurityException e) {
			logger.error("中国银行信用卡分期支付-生成签名失败：" + e.getMessage());
			throw new ValidationException("中国银行信用卡分期支付签名异常");
		} catch (IOException e) {
			logger.error("中国银行信用卡分期支付-读取证书失败：" + e.getMessage());
			throw new ValidationException("中国银行信用卡分期支付签名异常");
		}
		
		if(tool == null)
			throw new ValidationException("中国银行信用卡分期支付签名失败");
		
		String source = orderNo+"|"+orderTime+"|"+curCode+"|"+orderAmount+"|"+merchantNo+
				"|"+planCode+"|"+planNumber;
		
		try {
			sign = tool.sign(source.getBytes(_charset_encode));
		} catch (UnsupportedEncodingException e) {
			logger.error("中国银行信用卡分期支付-签名失败：" + e.getMessage());
			throw new ValidationException("中国银行信用卡分期支付签名失败");
		} catch (Exception e) {
			logger.error("中国银行信用卡分期支付-签名失败：" + e.getMessage());
			throw new ValidationException("中国银行信用卡分期支付签名失败");
		}
		
		return sign;
	}
	
	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		// 1.拼装参数
		Map<String, Object> params = new LinkedHashMap<String, Object>();
		params.put("merchantNo", platform.getMerchantNo());
		params.put("payType", "1");//1:网上购物
		params.put("orderNo", paymentOrder.getOrderNo());
		params.put("curCode", "001");//固定值  001：人民币
		//格式化订单金额
		DecimalFormat decimalFormat = new DecimalFormat("0.00");
		String money = decimalFormat.format(paymentOrder.getMoney());
		params.put("orderAmount", money);//订单金额
		params.put("planCode", (String)inParams.get("stagePlan"));
		params.put("planNumber", (String)inParams.get("stageNum"));
		String orderTime = DateUtils.format(paymentOrder.getCreate(), DateUtils.DATE_FORMAT_DATETIME_COMPACT);
		params.put("orderTime", orderTime);
		params.put("orderNote", StringUtils.trim((String) inParams.get("productName")));
		params.put("orderUrl", platform.getBehindUrl());
		String orderTimeoutDate = DateUtils.format(DateUtils.getNextDay(new Date()), DateUtils.DATE_FORMAT_DATETIME_COMPACT);
		params.put("orderTimeoutDate", orderTimeoutDate);
		
		Map<String, Object> encodeParams = new LinkedHashMap<String, Object>();
		encodeParams.put("orderNo", paymentOrder.getOrderNo());
		encodeParams.put("orderTime", orderTime);
		encodeParams.put("curCode", "001");
		encodeParams.put("orderAmount", money);
		encodeParams.put("merchantNo", platform.getMerchantNo());
		encodeParams.put("planCode", (String)inParams.get("stagePlan"));
		encodeParams.put("planNumber", (String)inParams.get("stageNum"));
		encodeParams.put("prikeyPath", platform.getPrivateUrl());//证书路径
		encodeParams.put("prikeyPwd", platform.getPrivatePassword());//私钥证书密码
		encodeParams.put("prikeyPayPwd", platform.getPayKey());//支付密码
		
		params.put("signData", this.encode(encodeParams));//签名数据
		params.put("payUrl", platform.getPayUrl());//支付地址
		return params;
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException, DataAccessException,
			PaymentRedirectException {
		String merchantNo = request.getParameter("merchantNo");//商户号
		String orderNo = request.getParameter("orderNo");//商户订单号
		String orderSeq = request.getParameter("orderSeq");//银行订单流水号
		String cardTyp = request.getParameter("cardTyp");//银行卡类别
		String payTime = request.getParameter("payTime");//支付时间
		String orderStatus = request.getParameter("orderStatus");//订单状态
		String payAmount = request.getParameter("payAmount");//支付金额
		String acctNo = request.getParameter("payAmount");//支付卡号
		String holderName = request.getParameter("holderName");//持卡人姓名
		String ibknum = request.getParameter("ibknum");//支付卡省行联行号
		String orderIp = request.getParameter("orderIp");//客户支付IP地址
		String orderRefer = request.getParameter("orderRefer");//客户浏览器Refer信息
		String bankTranSeq = request.getParameter("bankTranSeq");//银行交易流水号
		String returnActFlag = request.getParameter("returnActFlag");//返回操作类型
		String phoneNum = request.getParameter("phoneNum");//电话号码
		String signData = request.getParameter("signData");//中行签名数据
		
		//1.签名校验，验证失败抛出异常
		String rootCertificatePath = platform.getPublicUrl();
		String source = merchantNo+"|"+orderNo+"|"+orderSeq+"|"+cardTyp+"|"+payTime+"|"+orderStatus+"|"+payAmount;
		try {
			PKCS7Tool tool = PKCS7Tool.getVerifier(rootCertificatePath);
			tool.verify(signData, source.getBytes(_charset_encode), null);
		} catch (GeneralSecurityException e) {
			logger.error("中国银行信用卡分期支付签名验证失败：" + e.getMessage());
			throw new ValidationException("中国银行信用卡分期支付签名验证失败");
		} catch (IOException e) {
			logger.error("中国银行信用卡分期支付公钥证书未找到：" + e.getMessage());
			throw new ValidationException("中国银行信用卡分期支付签名验证失败");
		} catch (Exception e) {
			logger.error("中国银行信用卡分期支付签名验证失败：" + e.getMessage());
			throw new ValidationException("中国银行信用卡分期支付签名验证失败");
		}
		
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "中国银行信用卡分期支付订单查询为空,orderNo:" + orderNo);
		
		Map<String, Object> returned = new HashMap<String, Object>();
		if ("1".equals(orderStatus)) { // 支付成功
			logger.info("中国银行信用卡分期支付返回支付成功");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else { // 未支付
			logger.info("中国银行信用卡分期支付返回未支付");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_FAILED);
		}
		
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, orderSeq);
		returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(payAmount)).multiply(new BigDecimal(100)).intValue()));
		
		//不验证imprestMode,直接取订单中imprestMode
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		return returned;
	}

	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		String merchantNo = platform.getMerchantNo();
		String orderNos = paymentOrder.getOrderNo();
		String source = merchantNo+":"+orderNos;
		
		PKCS7Tool tool = null;
		try {
			String prikeyPath = platform.getPrivateUrl();//证书路径
			String prikeyPwd = platform.getPrivatePassword();//私钥证书密码
			String prikeyPayPwd = platform.getPayKey();//支付密码
			tool = PKCS7Tool.getSigner(prikeyPath, prikeyPwd, prikeyPayPwd);
		} catch (GeneralSecurityException e) {
			logger.error("中国银行信用卡分期支付-生成签名失败：" + e.getMessage());
			throw new ValidationException("中国银行信用卡分期支付签名异常");
		} catch (IOException e) {
			logger.error("中国银行信用卡分期支付-读取证书失败：" + e.getMessage());
			throw new ValidationException("中国银行信用卡分期支付签名异常");
		}
		
		if(tool == null)
			throw new ValidationException("中国银行信用卡分期支付签名失败");
		
		String sign = null;
		try {
			sign = tool.sign(source.getBytes(_charset_encode));
		} catch (UnsupportedEncodingException e) {
			logger.error("中国银行信用卡分期支付-签名失败：" + e.getMessage());
			throw new ValidationException("中国银行信用卡分期支付签名失败");
		} catch (Exception e) {
			logger.error("中国银行信用卡分期支付-签名失败：" + e.getMessage());
			throw new ValidationException("中国银行信用卡分期支付签名失败");
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("merchantNo", merchantNo);
		params.put("orderNos", orderNos);
		params.put("signData", sign);
		
		String response = PayCheckUtils.postRequst(platform.getPayCheckUrl(), params, 5000, _charset_encode, "中国银行信用卡分期支付订单查询接口");
		
		Map<String, Object> outParams = new HashMap<String, Object>();
		if(StringUtils.isBlank(response)){
			logger.error("中国银行信用卡分期支付订单查询-返回空");
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return outParams;
		}
		
		if(logger.isInfoEnabled())
			logger.info("中国银行信用卡分期支付订单查询返回：" + response);
		
		//转换xml
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(response);
		} catch (DocumentException e) {
			throw new ValidationException("中国银行信用卡分期支付订单查询返回xml转换异常");
		}
		
		Node handleStatus = doc.selectSingleNode("//res/header/hdlSts");//处理状态 A-成功  B-失败  K-未明
		String handleStatusText = handleStatus.getText();
		Node bodyFlag = doc.selectSingleNode("//res/header/bdFlg");//有无包体 0-有包体 1-无包体
		String bodyFlagText = bodyFlag.getText();
		if(!("A".equals(handleStatusText) && "0".equals(bodyFlagText))){
			throw new ValidationException("中国银行信用卡分期支付订单查询失败-查询状态不对或无包体");
		}
		
		Node orderNoNode = doc.selectSingleNode("//res/body/orderTrans/orderNo");
		Node orderStatusNode = doc.selectSingleNode("//res/body/orderTrans/orderStatus");
		String orderStatus = orderStatusNode.getText();
		String reOrderNo = orderNoNode.getText();
		if(!orderNos.equals(reOrderNo)){
			throw new ValidationException("对方平台未查询到订单信息");
		}
		
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		String payMoney = "";
		String oppositeOrderNo = "";
		if("1".equals(orderStatus)){
			//成功
			payState = PaymentConstant.PAYMENT_STATE_PAYED;
			Node orderSeqNode = doc.selectSingleNode("//res/body/orderTrans/orderSeq");
			String orderSeq = orderSeqNode.getText();
			oppositeOrderNo = StringUtils.trim(orderSeq);
			Node payAmountNode = doc.selectSingleNode("//res/body/orderTrans/payAmount");
			String payAmount = payAmountNode.getText();
			payMoney = StringUtils.trim(payAmount);
		}else if("0".equals(orderStatus)){
			//未处理
			payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		}else{
			//失败
			payState = PaymentConstant.PAYMENT_STATE_FAILED;
		}
		outParams.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo); // 对方订单号
		outParams.put(PaymentConstant.PAYMENT_STATE, payState); // 支付状态
		outParams.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(payMoney)).multiply(new BigDecimal(100)).intValue()));
		return outParams;
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return StringUtils.trim(request.getParameter("orderNo"));
	}

}
