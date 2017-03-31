package com.woniu.sncp.pay.core.service.payment.platform.nbcb;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.infosec.NetSignServer;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pojo.payment.PaymentOrder;

/**
 * 宁波银行 电商盈 网银直连
 * 
 * @author luzz
 *
 */
@Service("nbcbDPPayment")
public class NbcbDPPayment extends AbstractPayment {

	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		String sourceMsg = ObjectUtils.toString(inParams.get("sourceMsg")); 
		
		String bankDN = ObjectUtils.toString(inParams.get("bankDN")); 
		String signMsg="";
		try {
			NetSignServer nss = new NetSignServer();
			nss.NSSetPlainText(sourceMsg.getBytes("UTF-8"));
			byte bSignMsg[] = nss.NSDetachedSign(bankDN);  
			int i = nss.getLastErrnum();
			logger.info("宁波银行直联加密,sourceMsg:"+sourceMsg+",bankDN:"+bankDN);
			logger.info("宁波银行直联加密,verifyCode:"+i);
			signMsg=new String(bSignMsg,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("宁波银行直联加密出错："+e.getMessage(),e);
		}
		
		//remove bankDN
		inParams.put("bankDN","");
		return signMsg;
	}

	private String genEncodeStr(Map<String, Object> inParams) {
		String notify_url              = ObjectUtils.toString(inParams.get("notify_url"));	//商户异步回调地址（商户传入），我行服务器会异步回调到商户服务器，供商户接受需要参数信息
		String return_url              = ObjectUtils.toString(inParams.get("return_url"));	//商户同步回调地址（商户传入），我行平台处理完交易后会根据此参数值页面直接跳转到商户地址，供商户接受需要参数信息
		String out_trade_no            = ObjectUtils.toString(inParams.get("out_trade_no"));	//商户的订单号，商户需保持此订单唯一
		String subject                 = ObjectUtils.toString(inParams.get("subject"));	//商品名称
		String payment_type            = ObjectUtils.toString(inParams.get("payment_type"));	//支付类型，商户只需写死为1
		String seller_email            = ObjectUtils.toString(inParams.get("seller_email"));	//alipay-test05@alipay.com可为空，后台已写死
		String buyer_email             = ObjectUtils.toString(inParams.get("buyer_email"));	//买家支付宝账号
		String seller_id               = ObjectUtils.toString(inParams.get("seller_id")); 
		String buyer_id                = ObjectUtils.toString(inParams.get("buyer_id")); 
		String seller_account_name     = ObjectUtils.toString(inParams.get("seller_account_name")); 
		String buyer_account_name      = ObjectUtils.toString(inParams.get("buyer_account_name")); 
		String price                   = ObjectUtils.toString(inParams.get("price")); 
		String total_fee               = ObjectUtils.toString(inParams.get("total_fee"));	//支付总金额，不为空
		String quantity                = ObjectUtils.toString(inParams.get("quantity")); 
		String body                    = ObjectUtils.toString(inParams.get("body"));	//商品描述
		String show_url                = ObjectUtils.toString(inParams.get("show_url"));	//商户展示的超链接
		String paymethod               = ObjectUtils.toString(inParams.get("paymethod"));	//directPay（快捷支付） bankPay（网银纯网关支付） expressGatewayDebit（借记卡快捷网关） expressGatewayCredit（信用卡快捷网关） 
		String defaultbank             = ObjectUtils.toString(inParams.get("defaultbank")); 
		String royalty_type            = ObjectUtils.toString(inParams.get("royalty_type")); 
		String anti_phishing_key       = ObjectUtils.toString(inParams.get("anti_phishing_key")); 
		String exter_invoke_ip         = ObjectUtils.toString(inParams.get("exter_invoke_ip")); 
		String extra_common_param      = ObjectUtils.toString(inParams.get("extra_common_param")); 
		String it_b_pay                = ObjectUtils.toString(inParams.get("it_b_pay")); 
		String reqCustomerId           = ObjectUtils.toString(inParams.get("reqCustomerId"));	//测试环境商户客户号，商户上生产的客户号在我行开户时会颁发给商户
		String reqTime                 = ObjectUtils.toString(inParams.get("reqTime")); 
		String reqFlowNo               = ObjectUtils.toString(inParams.get("reqFlowNo")); 
		
		String sourceMsg = 
				notify_url + "|" + 
				return_url + "|" + 
				out_trade_no + "|" + 
				subject + "|" + 
				payment_type + "|" + 
				seller_email + "|" + 
				buyer_email + "|" + 
				seller_id + "|" + 
				buyer_id + "|" + 
				seller_account_name + "|" + 
				buyer_account_name + "|" + 
				price + "|" + 
				total_fee + "|" + 
				quantity + "|" + 
				body + "|" + 
				show_url + "|" + 
				paymethod + "|" + 
				defaultbank + "|" + 
				royalty_type + "|" + 
				anti_phishing_key + "|" + 
				exter_invoke_ip + "|" + 
				extra_common_param + "|" + 
				it_b_pay + "|" + 
				reqCustomerId+ "|" + 
				reqTime+ "|" + 
				reqFlowNo;
		return sourceMsg;
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
        // 银行直连参数校验
        String defaultbank = (String)inParams.get("defaultbank");
        if (StringUtils.isBlank(defaultbank)){
            throw new ValidationException("宁波银行直连参数缺少支付银行参数");
        }
        
        // 1.拼装参数
        PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
        Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("reqCustomerId", platform.getMerchantNo());
        params.put("notify_url", platform.getBehindUrl(paymentOrder.getMerchantId()));
        params.put("return_url", platform.getFrontUrl(paymentOrder.getMerchantId()));
        params.put("out_trade_no", paymentOrder.getOrderNo());
        params.put("subject", StringUtils.trim((String) inParams.get("productName")));
        params.put("payment_type", "1"); // 固定值
        params.put("seller_email", ""); //可为空，后台已写死
        params.put("buyer_email", ""); 
        params.put("seller_id", ""); 
        params.put("buyer_id", ""); 
        params.put("seller_account_name", ""); 
        params.put("buyer_account_name", ""); 
        params.put("price", ""); 
        params.put("total_fee", ObjectUtils.toString(paymentOrder.getMoney())); 
        params.put("quantity", "");
        params.put("body", StringUtils.trim((String) inParams.get("productName"))); 
        params.put("show_url", "http://imprest.woniu.com"); 
        params.put("paymethod", "bankPay"); //directPay（快捷支付） bankPay（网银纯网关支付） expressGatewayDebit（借记卡快捷网关） expressGatewayCredit（信用卡快捷网关） 
        params.put("defaultbank", inParams.get("defaultbank")); 
        params.put("royalty_type", ""); 
        params.put("anti_phishing_key", ""); 
        params.put("exter_invoke_ip", ""); 
        params.put("extra_common_param", "");
        params.put("it_b_pay", ""); 
        params.put("reqTime", new SimpleDateFormat("yyyyMMddhhmmss").format(new Date())); 
        params.put("reqFlowNo", "");
        
        params.put("bankDN", platform.getPayKey());
        
        //2.加密
        String sourceMsg = genEncodeStr(params);
        params.put("sourceMsg", sourceMsg);
        String sign = this.encode(params);
        
        params.put("sign_msg", sign);
        
        params.put("payUrl", platform.getPayUrl()); // 提交给对方的支付地址
        params.put("acceptCharset", "GBK"); // 提交给对方的支付编码
		return params;
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException,
			DataAccessException, PaymentRedirectException {
		String notifyMsg = StringUtils.trim(request.getParameter("notifyMsg"));
		String[] notifyMsgs = notifyMsg.split("\\|");
		
		String tradeStatus = notifyMsgs[7];
		String outTradeNo = notifyMsgs[3];
		// 签名验证
		String sourceMsg = generateSourceMsg(request);
		if(!veriySign(sourceMsg,notifyMsgs[notifyMsgs.length-1])){
			throw new ValidationException("宁波银行支付平台加密校验失败");
		}
		
		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(outTradeNo);
		Assert.notNull(paymentOrder, "宁波银行支付订单查询为空,orderNo:" + outTradeNo);
		
		Map<String, Object> returned = new HashMap<String, Object>();
		if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) { // 支付成功
			logger.info("宁波银行返回支付成功");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else { // 未支付
			logger.info("宁波银行返回未支付");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}
		
		String oppositeOrderNo = notifyMsgs[6];
		String paymentMoney = notifyMsgs[18];
		
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);
//		returned.put(PaymentConstant.IMPREST_OPPOSITE_MONEY, String.valueOf(NumberUtils.toFloat(paymentMoney) * 100));
		returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(paymentMoney)).multiply(new BigDecimal(100)).intValue()));
		
		//不验证imprestMode,直接取订单中imprestMode
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		return returned;
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		//该接口不需要返回
	}

	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		String out_trade_no = paymentOrder.getOrderNo();
		String trade_no = out_trade_no.equals(paymentOrder.getOtherOrderNo())?"":paymentOrder.getOtherOrderNo();
		String reqCustomerId = platform.getMerchantNo();
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("outTradeNo", out_trade_no);
		params.put("tradeNo", trade_no);
		params.put("bankDN", platform.getPayKey());
		params.put("reqCustomerId", reqCustomerId);
		
		String sendMessage = genserateSendMsg(params);
		String responseMsg = verifyOrder(platform.getPayCheckUrl() ,sendMessage);  
		
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(responseMsg);
		} catch (DocumentException e) {
			throw new ValidationException("宁波银行订单校验xml转换异常");
		}
		
		//校验
		String return_sign_msg = doc.selectSingleNode("//root/sd").getText();
		
		int cdStartPos = responseMsg.indexOf("<cd>"); 
		int cdEndPos = responseMsg.indexOf("</cd>"); 
		String return_source_msg = responseMsg.substring( cdStartPos , cdEndPos + "</cd>".length());
		if(!veriySign(return_source_msg,return_sign_msg)){
			throw new ValidationException("宁波银行订单校验失败");
		}
		
		String return_error_code = doc.selectSingleNode("//root/ec").getText();
		if(!StringUtils.isEmpty(return_error_code) && !"0000".equals(return_error_code)){
			throw new ValidationException(doc.selectSingleNode("//root/em").getText());
		}
		
		Map<String, Object> outParams = new HashMap<String, Object>();
		
		String return_is_success = doc.selectSingleNode("//root/cd/is_success").getText();
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		if ("T".equals(return_is_success)) {
			String trade_status = doc.selectSingleNode("//root/cd/trade_status").getText(); // 支付状态
			String total_fee = doc.selectSingleNode("//root/cd/total_fee").getText();
//			String return_out_trade_no = doc.selectSingleNode("//root/cd/out_trade_no").getText();
			String return_trade_no = doc.selectSingleNode("//root/cd/trade_no").getText();
			
			if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {
				logger.info("结束 -- 宁波银行订单对账 -- 支付成功");
				payState = PaymentConstant.PAYMENT_STATE_PAYED;
			} else {
				logger.info("结束 -- 宁波银行订单对账 -- 未支付");
				payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
			}
			
			outParams.put(PaymentConstant.OPPOSITE_ORDERNO, return_trade_no); // 对方订单号
			outParams.put(PaymentConstant.PAYMENT_STATE, payState); // 支付状态
//			outParams.put(PaymentConstant.IMPREST_OPPOSITE_MONEY, String.valueOf(NumberUtils.toFloat(total_fee) * 100));
			outParams.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf((new BigDecimal(total_fee)).multiply(new BigDecimal(100)).intValue()));
		} else if("F".equals(return_is_success)) {
			throw new ValidationException("订单验证失败：宁波银行返回：" + doc.selectSingleNode("//root/cd/error").getText());
		}
		
		return outParams;
	}
	
	private String genserateSendMsg(Map<String, String> params){
		String tradeNo = ObjectUtils.toString(params.get("tradeNo"));
		String outTradeNo = ObjectUtils.toString(params.get("outTradeNo"));
		String reqCustomerId = ObjectUtils.toString(params.get("reqCustomerId"));
		String cdMsg =  "<cd><trade_no>" +
							tradeNo +
						"</trade_no>" +
						"<out_trade_no>" +
							outTradeNo +
						"</out_trade_no></cd>";
		
		Map<String,Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("sourceMsg", cdMsg);
		encodeParams.put("bankDN", params.get("bankDN"));
		
		String sdMsg = "<sd>" +
						    this.encode(encodeParams)+
				   	   "</sd>";
		//SML 带签名，xml不带签名
		String sendMsg = "<?xml version=\"1.0\" encoding=\"gbk\" ?>" + 
							"<root>" +
							"<reqServiceId>NCTR03Comm</reqServiceId>" + 
							"<reqTime>" +
								new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +
							"</reqTime>" +
							"<reqFlowNo></reqFlowNo>" +
							"<reqCustomerId>" + 
								reqCustomerId +
							"</reqCustomerId>" + 
							"<reqChannelId>NC</reqChannelId>" +
							"<reqDataView>SML</reqDataView>" +
							cdMsg +
							sdMsg +
							"</root>";
		
		return sendMsg;
	}

	private String verifyOrder(String verifyUrl,String sendMessage) {
		StringBuffer responseContent = new StringBuffer();
		
		try {
			URL url = new URL(verifyUrl);
			DataOutputStream out;
			BufferedReader in;
			String line;
			
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			
			// 请求参数设置
			urlConn.setRequestMethod("POST");
			urlConn.setRequestProperty( "content-type", "text/html" );
			urlConn.addRequestProperty( "Content-Length", String.valueOf( sendMessage.getBytes().length ) );
			System.setProperty( "sun.net.client.defaultConnectTimeout", "3000" );
			System.setProperty( "sun.net.client.defaultReadTimeout", "3000" );
			urlConn.setDoInput( true );
			urlConn.setDoOutput( true );
			urlConn.connect();
			if ( sendMessage != null ) {
				out = new DataOutputStream( urlConn.getOutputStream() );
				out.write( sendMessage.getBytes( "GBK" ) );
			}
			in = new BufferedReader( new InputStreamReader( urlConn.getInputStream(), "GBK" ) );
			while ( ( line = in.readLine() ) != null ) 
			{
				responseContent.append( line );
			}

		} catch (IOException e) {
			logger.error("宁波银行校验订单失败，"+e.getMessage(),e);
		}
		
		return responseContent.toString();
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		String notifyMsg = StringUtils.trim(request.getParameter("notifyMsg"));
		String[] notifyMsgs = notifyMsg.split("\\|");
		
		return notifyMsgs[1];
	}
	
	private boolean veriySign(String sourceMsg,String signMsg) {
		// 构造验签实例 
		NetSignServer nss = new NetSignServer(); 
		// 验签（signMsg为签名后的数据，sourceMsg为源数据） 
		try {
			nss.NSDetachedVerify(signMsg.toString().getBytes("GBK"), sourceMsg.toString().getBytes("GBK"));
		} catch (UnsupportedEncodingException e) {
			logger.error("宁波银行签名校验不正确,sourceMsg="+sourceMsg+",signMsg="+signMsg);
			logger.error("宁波银行签名校验编码错误,"+e.getMessage(),e);
			return false;
		} 
		//得到验签结果 
		int veriyCode = -1; 
		veriyCode = nss.getLastErrnum();
		
		if (veriyCode != 0) {
			logger.error("宁波银行签名校验不正确,sourceMsg="+sourceMsg+",signMsg="+signMsg+",veriyCode="+veriyCode);
			return false;
		}
		
		return true;
	}

	private String generateSourceMsg(HttpServletRequest request) {
		String notifyMsg = ObjectUtils.toString(request.getParameter("notifyMsg"));
		
		String sourceMsg = notifyMsg.substring(0,notifyMsg.lastIndexOf("|")+1);
		return sourceMsg;
	}
}
